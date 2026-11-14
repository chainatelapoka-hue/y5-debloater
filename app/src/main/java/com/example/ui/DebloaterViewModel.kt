package com.example.ui

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppFilter
import com.example.data.AppStatus
import com.example.data.CommandLogEntry
import com.example.data.SafetyLevel
import com.example.data.SystemAppItem
import com.example.data.SystemAppRepository
import com.example.shizuku.CommandResult
import com.example.shizuku.ShizukuManager
import com.example.shizuku.ShizukuState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DebloaterUiState(
    val shizukuState: ShizukuState = ShizukuState.Checking,
    val allApps: List<SystemAppItem> = emptyList(),
    val filteredApps: List<SystemAppItem> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: AppFilter = AppFilter.ALL,
    val isLoadingApps: Boolean = true,
    val isExecutingCommand: Boolean = false,
    val selectedApp: SystemAppItem? = null,
    val showAdbGuide: Boolean = false,
    val showLogsSheet: Boolean = false,
    val commandLogs: List<CommandLogEntry> = emptyList(),
    val filterCounts: Map<AppFilter, Int> = emptyMap()
)

class DebloaterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SystemAppRepository(application.applicationContext)
    private val shizukuManager = ShizukuManager(application.applicationContext)

    private val _uiState = MutableStateFlow(DebloaterUiState())
    val uiState: StateFlow<DebloaterUiState> = _uiState.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    private var logSequence = 0L

    init {
        shizukuManager.startListening()
        viewModelScope.launch {
            shizukuManager.state.collect { shizukuState ->
                _uiState.update { it.copy(shizukuState = shizukuState) }
            }
        }
        refreshApps()
    }

    override fun onCleared() {
        super.onCleared()
        shizukuManager.stopListening()
    }

    fun refreshShizuku() {
        shizukuManager.refreshStatus()
    }

    fun requestShizukuPermission() {
        shizukuManager.requestPermission()
    }

    fun refreshApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingApps = true) }
            val loaded = repository.loadSystemApps()
            _uiState.update { current ->
                val counts = computeCounts(loaded)
                val filtered = applyFilterAndSearch(loaded, current.searchQuery, current.activeFilter)
                val updatedSelected = current.selectedApp?.let { selected ->
                    loaded.find { it.packageName == selected.packageName }
                }
                current.copy(
                    allApps = loaded,
                    filteredApps = filtered,
                    filterCounts = counts,
                    selectedApp = updatedSelected,
                    isLoadingApps = false
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                filteredApps = applyFilterAndSearch(current.allApps, query, current.activeFilter)
            )
        }
    }

    fun selectFilter(filter: AppFilter) {
        _uiState.update { current ->
            current.copy(
                activeFilter = filter,
                filteredApps = applyFilterAndSearch(current.allApps, current.searchQuery, filter)
            )
        }
    }

    fun openAppDetail(app: SystemAppItem) {
        _uiState.update { it.copy(selectedApp = app) }
    }

    fun closeAppDetail() {
        _uiState.update { it.copy(selectedApp = null) }
    }

    fun setShowAdbGuide(show: Boolean) {
        _uiState.update { it.copy(showAdbGuide = show) }
    }

    fun setShowLogsSheet(show: Boolean) {
        _uiState.update { it.copy(showLogsSheet = show) }
    }

    fun clearLogs() {
        _uiState.update { it.copy(commandLogs = emptyList()) }
    }

    suspend fun loadAppIcon(packageName: String): Drawable? = repository.loadAppIcon(packageName)

    fun uninstallApp(app: SystemAppItem, keepData: Boolean) {
        executePackageAction(app) {
            shizukuManager.uninstallForUser0(app.packageName, keepData)
        }
    }

    fun restoreApp(app: SystemAppItem) {
        executePackageAction(app) {
            shizukuManager.restoreForUser0(app.packageName)
        }
    }

    fun disableApp(app: SystemAppItem) {
        executePackageAction(app) {
            shizukuManager.disableForUser0(app.packageName)
        }
    }

    fun enableApp(app: SystemAppItem) {
        executePackageAction(app) {
            shizukuManager.enablePackage(app.packageName)
        }
    }

    private fun executePackageAction(
        app: SystemAppItem,
        action: suspend () -> CommandResult
    ) {
        if (_uiState.value.isExecutingCommand) return
        viewModelScope.launch {
            _uiState.update { it.copy(isExecutingCommand = true) }
            val result = action()
            val timeLabel = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val logEntry = CommandLogEntry(
                id = ++logSequence,
                timeLabel = timeLabel,
                appName = app.name,
                packageName = app.packageName,
                command = result.command,
                exitCode = result.exitCode,
                output = result.output,
                isSuccess = result.isSuccess
            )
            _uiState.update { current ->
                current.copy(
                    isExecutingCommand = false,
                    commandLogs = listOf(logEntry) + current.commandLogs,
                    selectedApp = if (result.isSuccess) null else current.selectedApp
                )
            }
            if (result.isSuccess) {
                _snackbarEvents.tryEmit("সফল হয়েছে: ${app.name}")
                refreshApps()
            } else {
                _snackbarEvents.tryEmit("ব্যর্থ হয়েছে: ${result.output}")
            }
        }
    }

    private fun computeCounts(apps: List<SystemAppItem>): Map<AppFilter, Int> {
        return mapOf(
            AppFilter.ALL to apps.size,
            AppFilter.INSTALLED to apps.count { it.status == AppStatus.INSTALLED },
            AppFilter.UNINSTALLED to apps.count { it.status == AppStatus.UNINSTALLED },
            AppFilter.DISABLED to apps.count { it.status == AppStatus.DISABLED },
            AppFilter.SAFE_BLOAT to apps.count { it.safetyLevel == SafetyLevel.SAFE_BLOAT }
        )
    }

    private fun applyFilterAndSearch(
        apps: List<SystemAppItem>,
        query: String,
        filter: AppFilter
    ): List<SystemAppItem> {
        val normalizedQuery = query.trim().lowercase()
        return apps.filter { item ->
            val matchesFilter = when (filter) {
                AppFilter.ALL -> true
                AppFilter.INSTALLED -> item.status == AppStatus.INSTALLED
                AppFilter.UNINSTALLED -> item.status == AppStatus.UNINSTALLED
                AppFilter.DISABLED -> item.status == AppStatus.DISABLED
                AppFilter.SAFE_BLOAT -> item.safetyLevel == SafetyLevel.SAFE_BLOAT
            }
            val matchesQuery = normalizedQuery.isEmpty() ||
                item.name.lowercase().contains(normalizedQuery) ||
                item.packageName.lowercase().contains(normalizedQuery)
            matchesFilter && matchesQuery
        }
    }
}
