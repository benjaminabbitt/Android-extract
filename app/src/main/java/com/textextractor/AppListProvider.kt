package com.textextractor

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import timber.log.Timber

/**
 * AppInfo - Data class representing an installed application
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean
) {
    override fun toString(): String = appName
}

/**
 * AppListProvider - Provides lists of installed and running applications
 *
 * Implements IoC pattern with production and test constructors
 * Provides functionality to:
 * - Get installed applications
 * - Get running applications
 * - Get recently used apps from extraction history
 * - Filter system apps
 * - Sort by app name
 *
 * Implementation follows TDD - tests written first in AppListProviderTest
 */
class AppListProvider(
    private val context: Context,
    private val dataRepository: ITextDataRepository = TextDataRepository
) {
    // Production constructor
    constructor(context: Context) : this(context, TextDataRepository)

    private val packageManager: PackageManager by lazy {
        context.packageManager
    }

    /**
     * Get list of installed applications
     *
     * @param includeSystemApps Include system apps in the list
     * @return List of AppInfo sorted by app name
     */
    fun getInstalledApps(includeSystemApps: Boolean = false): List<AppInfo> {
        return try {
            val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

            packages
                .filter { appInfo ->
                    includeSystemApps || !isSystemApp(appInfo)
                }
                .map { appInfo ->
                    AppInfo(
                        packageName = appInfo.packageName,
                        appName = getAppName(appInfo),
                        isSystemApp = isSystemApp(appInfo)
                    )
                }
                .sortedBy { it.appName.lowercase() }
        } catch (e: Exception) {
            Timber.e(e, "Error getting installed apps")
            emptyList()
        }
    }

    /**
     * Get list of currently running applications
     *
     * Note: This may not work on newer Android versions due to privacy restrictions
     * @return List of AppInfo for running apps
     */
    fun getRunningApps(): List<AppInfo> {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningApps = activityManager.runningAppProcesses ?: emptyList()

            val packageNames = runningApps.map { it.processName }.toSet()
            getAppsByPackageNames(packageNames)
        } catch (e: Exception) {
            Timber.e(e, "Error getting running apps")
            emptyList()
        }
    }

    /**
     * Get apps by package names
     *
     * @param packageNames Set of package names to retrieve
     * @return List of AppInfo for matching packages
     */
    fun getAppsByPackageNames(packageNames: Set<String>): List<AppInfo> {
        return packageNames.mapNotNull { packageName ->
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                AppInfo(
                    packageName = appInfo.packageName,
                    appName = getAppName(appInfo),
                    isSystemApp = isSystemApp(appInfo)
                )
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }.sortedBy { it.appName.lowercase() }
    }

    /**
     * Get apps that have recently had text extracted
     * Based on data in the repository
     *
     * @return List of unique AppInfo from extraction history
     */
    fun getRecentlyUsedApps(): List<AppInfo> {
        return try {
            val extractedData = dataRepository.getAllData()

            extractedData
                .map { it.packageName to it.appName }
                .distinct()
                .map { (packageName, appName) ->
                    AppInfo(
                        packageName = packageName,
                        appName = appName,
                        isSystemApp = false  // We don't track this in extracted data
                    )
                }
                .sortedBy { it.appName.lowercase() }
        } catch (e: Exception) {
            Timber.e(e, "Error getting recently used apps")
            emptyList()
        }
    }

    /**
     * Get combined list of installed and recently used apps
     * Useful for dropdown that shows both
     *
     * @return List of unique AppInfo
     */
    fun getCombinedAppList(includeSystemApps: Boolean = false): List<AppInfo> {
        val installedApps = getInstalledApps(includeSystemApps)
        val recentApps = getRecentlyUsedApps()

        // Combine and deduplicate by package name
        return (installedApps + recentApps)
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }

    /**
     * Check if an app is a system app
     */
    private fun isSystemApp(appInfo: ApplicationInfo): Boolean {
        return (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }

    /**
     * Get human-readable app name
     */
    private fun getAppName(appInfo: ApplicationInfo): String {
        return try {
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            appInfo.packageName
        }
    }
}
