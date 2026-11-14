package com.example.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SystemAppRepository(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    suspend fun loadSystemApps(): List<SystemAppItem> = withContext(Dispatchers.IO) {
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(
                    (PackageManager.MATCH_UNINSTALLED_PACKAGES or PackageManager.GET_META_DATA).toLong()
                )
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(
                PackageManager.MATCH_UNINSTALLED_PACKAGES or PackageManager.GET_META_DATA
            )
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(
                PackageManager.GET_UNINSTALLED_PACKAGES or PackageManager.GET_META_DATA
            )
        }

        packages.mapNotNull { appInfo ->
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            if (!isSystem) return@mapNotNull null

            val appName = try {
                pm.getApplicationLabel(appInfo).toString().ifBlank { appInfo.packageName }
            } catch (_: Throwable) {
                appInfo.packageName
            }

            val isInstalled = (appInfo.flags and ApplicationInfo.FLAG_INSTALLED) != 0
            val status = when {
                !isInstalled -> AppStatus.UNINSTALLED
                !appInfo.enabled -> AppStatus.DISABLED
                else -> AppStatus.INSTALLED
            }

            val versionName = try {
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    PackageManager.MATCH_UNINSTALLED_PACKAGES
                } else {
                    @Suppress("DEPRECATION")
                    PackageManager.GET_UNINSTALLED_PACKAGES
                }
                val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageInfo(
                        appInfo.packageName,
                        PackageManager.PackageInfoFlags.of(flags.toLong())
                    )
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(appInfo.packageName, flags)
                }
                pkgInfo.versionName ?: "—"
            } catch (_: Throwable) {
                "—"
            }

            val advice = BloatwareCatalog.analyze(appInfo.packageName)

            SystemAppItem(
                name = appName,
                packageName = appInfo.packageName,
                versionName = versionName,
                sourceDir = appInfo.sourceDir ?: "/system/app",
                status = status,
                safetyLevel = advice.level,
                safetyNoteBn = advice.noteBn
            )
        }.sortedWith(
            compareBy<SystemAppItem> { it.name.lowercase() }
                .thenBy { it.packageName.lowercase() }
        )
    }

    suspend fun loadAppIcon(packageName: String): Drawable? = withContext(Dispatchers.IO) {
        try {
            pm.getApplicationIcon(packageName)
        } catch (_: Throwable) {
            null
        }
    }
}
