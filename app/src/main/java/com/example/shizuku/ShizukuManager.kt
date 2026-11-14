package com.example.shizuku

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import com.example.BuildConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import rikka.shizuku.Shizuku

sealed interface ShizukuState {
    data object Checking : ShizukuState
    data object NotInstalled : ShizukuState
    data object NotRunning : ShizukuState
    data object PermissionRequired : ShizukuState
    data class PermissionDenied(val shouldShowRationale: Boolean) : ShizukuState
    data class Connected(
        val uid: Int,
        val apiVersion: Int,
        val serviceBound: Boolean
    ) : ShizukuState
}

data class CommandResult(
    val command: String,
    val exitCode: Int,
    val output: String,
    val isSuccess: Boolean
)

class ShizukuManager(private val context: Context) {

    companion object {
        private const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
        private const val REQUEST_CODE_PERMISSION = 1001
        private val VALID_PACKAGE_REGEX = Regex("^[a-zA-Z0-9_.]+$")
    }

    private val _state = MutableStateFlow<ShizukuState>(ShizukuState.Checking)
    val state: StateFlow<ShizukuState> = _state.asStateFlow()

    @Volatile
    private var shellService: IShellService? = null
    private var pendingServiceDeferred: CompletableDeferred<IShellService?>? = null
    private var listenersRegistered = false

    private val userServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(context.packageName, ShellUserService::class.java.name)
    )
        .daemon(false)
        .processNameSuffix("shell_service")
        .debuggable(BuildConfig.DEBUG)
        .version(BuildConfig.VERSION_CODE)

    private val userServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val bound = if (service != null && service.pingBinder()) {
                IShellService.Stub.asInterface(service)
            } else {
                null
            }
            shellService = bound
            pendingServiceDeferred?.complete(bound)
            refreshStatus()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            shellService = null
            refreshStatus()
        }
    }

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        refreshStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        shellService = null
        refreshStatus()
    }

    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == REQUEST_CODE_PERMISSION) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    bindShellService()
                    refreshStatus()
                } else {
                    val rationale = try {
                        Shizuku.shouldShowRequestPermissionRationale()
                    } catch (_: Throwable) {
                        false
                    }
                    _state.value = ShizukuState.PermissionDenied(rationale)
                }
            }
        }

    fun startListening() {
        if (!listenersRegistered) {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
            listenersRegistered = true
        }
        refreshStatus()
    }

    fun stopListening() {
        if (listenersRegistered) {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
            listenersRegistered = false
        }
        try {
            if (shellService != null && Shizuku.pingBinder()) {
                Shizuku.unbindUserService(userServiceArgs, userServiceConnection, true)
            }
        } catch (_: Throwable) {
        }
        shellService = null
    }

    fun refreshStatus() {
        val binderAlive = try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }

        if (!binderAlive) {
            shellService = null
            _state.value = if (isShizukuAppInstalled()) {
                ShizukuState.NotRunning
            } else {
                ShizukuState.NotInstalled
            }
            return
        }

        val hasPermission = try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }

        if (!hasPermission) {
            _state.value = ShizukuState.PermissionRequired
            return
        }

        if (shellService == null) {
            bindShellService()
        }

        val uid = try {
            Shizuku.getUid()
        } catch (_: Throwable) {
            -1
        }
        val version = try {
            Shizuku.getVersion()
        } catch (_: Throwable) {
            -1
        }

        _state.value = ShizukuState.Connected(
            uid = uid,
            apiVersion = version,
            serviceBound = shellService?.asBinder()?.pingBinder() == true
        )
    }

    fun requestPermission() {
        try {
            if (!Shizuku.pingBinder()) {
                refreshStatus()
                return
            }
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                bindShellService()
                refreshStatus()
            } else {
                Shizuku.requestPermission(REQUEST_CODE_PERMISSION)
            }
        } catch (_: Throwable) {
            refreshStatus()
        }
    }

    private fun bindShellService() {
        try {
            if (Shizuku.pingBinder() &&
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            ) {
                Shizuku.bindUserService(userServiceArgs, userServiceConnection)
            }
        } catch (_: Throwable) {
        }
    }

    private suspend fun ensureService(): IShellService? {
        val existing = shellService
        if (existing != null && existing.asBinder().pingBinder()) {
            return existing
        }
        val deferred = CompletableDeferred<IShellService?>()
        pendingServiceDeferred = deferred
        bindShellService()
        val resolved = withTimeoutOrNull(4000L) {
            deferred.await()
        }
        pendingServiceDeferred = null
        return resolved ?: shellService
    }

    suspend fun uninstallForUser0(packageName: String, keepData: Boolean): CommandResult {
        if (!VALID_PACKAGE_REGEX.matches(packageName)) {
            return CommandResult("", -1, "Invalid package name", false)
        }
        val cmd = if (keepData) {
            "pm uninstall -k --user 0 $packageName"
        } else {
            "pm uninstall --user 0 $packageName"
        }
        return executeShell(cmd)
    }

    suspend fun restoreForUser0(packageName: String): CommandResult {
        if (!VALID_PACKAGE_REGEX.matches(packageName)) {
            return CommandResult("", -1, "Invalid package name", false)
        }
        val primaryCmd = "cmd package install-existing $packageName"
        val result = executeShell(primaryCmd)
        if (!result.isSuccess && result.output.contains("Unknown command", ignoreCase = true)) {
            return executeShell("pm install-existing $packageName")
        }
        return result
    }

    suspend fun disableForUser0(packageName: String): CommandResult {
        if (!VALID_PACKAGE_REGEX.matches(packageName)) {
            return CommandResult("", -1, "Invalid package name", false)
        }
        return executeShell("pm disable-user --user 0 $packageName")
    }

    suspend fun enablePackage(packageName: String): CommandResult {
        if (!VALID_PACKAGE_REGEX.matches(packageName)) {
            return CommandResult("", -1, "Invalid package name", false)
        }
        return executeShell("pm enable $packageName")
    }

    private suspend fun executeShell(command: String): CommandResult = withContext(Dispatchers.IO) {
        if (!tryPing()) {
            refreshStatus()
            return@withContext CommandResult(
                command = command,
                exitCode = -1,
                output = "Shizuku চালু নেই! আগে ADB দিয়ে Shizuku স্টার্ট করুন।",
                isSuccess = false
            )
        }
        val hasPerm = try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }
        if (!hasPerm) {
            refreshStatus()
            return@withContext CommandResult(
                command = command,
                exitCode = -1,
                output = "Shizuku পারমিশন দেওয়া হয়নি।",
                isSuccess = false
            )
        }

        val service = ensureService()
        if (service == null) {
            return@withContext CommandResult(
                command = command,
                exitCode = -1,
                output = "Shizuku UserService কানেক্ট করা যায়নি। পুনরায় চেষ্টা করুন।",
                isSuccess = false
            )
        }

        return@withContext try {
            val raw = service.runCommand(command)
            val splitIndex = raw.indexOf('\n')
            val exitCode = if (splitIndex >= 0) {
                raw.substring(0, splitIndex).toIntOrNull() ?: -1
            } else {
                raw.toIntOrNull() ?: -1
            }
            val output = if (splitIndex >= 0 && splitIndex + 1 < raw.length) {
                raw.substring(splitIndex + 1).trim()
            } else {
                ""
            }
            val success = exitCode == 0 &&
                !output.contains("Failure", ignoreCase = true) &&
                !output.contains("Exception", ignoreCase = true)
            CommandResult(
                command = command,
                exitCode = exitCode,
                output = output.ifEmpty { if (success) "Success" else "Exit code $exitCode" },
                isSuccess = success
            )
        } catch (e: Throwable) {
            shellService = null
            refreshStatus()
            CommandResult(
                command = command,
                exitCode = -1,
                output = e.message ?: "Binder IPC error",
                isSuccess = false
            )
        }
    }

    private fun tryPing(): Boolean = try {
        Shizuku.pingBinder()
    } catch (_: Throwable) {
        false
    }

    private fun isShizukuAppInstalled(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    SHIZUKU_PACKAGE,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Throwable) {
            false
        }
    }
}
