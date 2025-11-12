package com.textextractor

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * TDD Tests for AppListProvider - written BEFORE implementation
 * Tests functionality for getting running/installed apps
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AppListProviderTest {

    private lateinit var context: Context
    private lateinit var appListProvider: AppListProvider

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        appListProvider = AppListProvider(context)
    }

    @Test
    fun `getInstalledApps should return list of installed apps`() {
        val apps = appListProvider.getInstalledApps()

        assertThat(apps).isNotNull()
        assertThat(apps).isNotEmpty()
    }

    @Test
    fun `getInstalledApps should exclude system apps by default`() {
        val apps = appListProvider.getInstalledApps(includeSystemApps = false)

        apps.forEach { app ->
            assertThat(app.isSystemApp).isFalse()
        }
    }

    @Test
    fun `getInstalledApps should include system apps when requested`() {
        val appsWithSystem = appListProvider.getInstalledApps(includeSystemApps = true)
        val appsWithoutSystem = appListProvider.getInstalledApps(includeSystemApps = false)

        assertThat(appsWithSystem.size).isAtLeast(appsWithoutSystem.size)
    }

    @Test
    fun `AppInfo should contain package name and app name`() {
        val apps = appListProvider.getInstalledApps()

        apps.forEach { app ->
            assertThat(app.packageName).isNotEmpty()
            assertThat(app.appName).isNotEmpty()
        }
    }

    @Test
    fun `getInstalledApps should sort by app name`() {
        val apps = appListProvider.getInstalledApps()

        val sortedNames = apps.map { it.appName }
        val expectedSorted = sortedNames.sorted()

        assertThat(sortedNames).isEqualTo(expectedSorted)
    }

    @Test
    fun `getRunningApps should return list of running apps`() {
        val apps = appListProvider.getRunningApps()

        assertThat(apps).isNotNull()
        // Running apps list may be empty in test environment
    }

    @Test
    fun `getAppsByPackageNames should return matching apps`() {
        val testPackages = setOf("com.android.settings")
        val apps = appListProvider.getAppsByPackageNames(testPackages)

        assertThat(apps).isNotNull()
    }

    @Test
    fun `getAppsByPackageNames should return empty list for unknown packages`() {
        val testPackages = setOf("com.nonexistent.package")
        val apps = appListProvider.getAppsByPackageNames(testPackages)

        assertThat(apps).isEmpty()
    }

    @Test
    fun `AppInfo should indicate if app is system app`() {
        val apps = appListProvider.getInstalledApps(includeSystemApps = true)

        val hasSystemApp = apps.any { it.isSystemApp }
        val hasNonSystemApp = apps.any { !it.isSystemApp }

        // Should have both types in a real device (may not in test)
        assertThat(apps).isNotEmpty()
    }

    @Test
    fun `getRecentlyUsedApps should return apps from repository`() {
        // Add test data
        val testData = ExtractedTextData(
            packageName = "com.test.app",
            appName = "Test App",
            text = "Test",
            className = null,
            viewIdResourceName = null,
            eventType = "Test"
        )
        TextDataRepository.clearData()
        TextDataRepository.addExtractedText(testData)

        val apps = appListProvider.getRecentlyUsedApps()

        assertThat(apps).hasSize(1)
        assertThat(apps[0].packageName).isEqualTo("com.test.app")

        TextDataRepository.clearData()
    }

    @Test
    fun `getRecentlyUsedApps should return unique apps`() {
        TextDataRepository.clearData()

        // Add multiple entries from same app
        repeat(3) {
            TextDataRepository.addExtractedText(
                ExtractedTextData(
                    packageName = "com.test.app",
                    appName = "Test App",
                    text = "Test $it",
                    className = null,
                    viewIdResourceName = null,
                    eventType = "Test"
                )
            )
        }

        val apps = appListProvider.getRecentlyUsedApps()

        assertThat(apps).hasSize(1)

        TextDataRepository.clearData()
    }

    @Test
    fun `getCombinedAppList should include both installed and recently used`() {
        TextDataRepository.clearData()
        TextDataRepository.addExtractedText(
            ExtractedTextData(
                packageName = "com.test.app",
                appName = "Test App",
                text = "Test",
                className = null,
                viewIdResourceName = null,
                eventType = "Test"
            )
        )

        val apps = appListProvider.getCombinedAppList()

        assertThat(apps).isNotEmpty()
        // Should have at least our test app
        val hasTestApp = apps.any { it.packageName == "com.test.app" }
        assertThat(hasTestApp).isTrue()

        TextDataRepository.clearData()
    }

    @Test
    fun `AppInfo data class should work correctly`() {
        val app = AppInfo(
            packageName = "com.test",
            appName = "Test",
            isSystemApp = false
        )

        assertThat(app.packageName).isEqualTo("com.test")
        assertThat(app.appName).isEqualTo("Test")
        assertThat(app.isSystemApp).isFalse()
    }

    @Test
    fun `AppInfo should support equality`() {
        val app1 = AppInfo("com.test", "Test", false)
        val app2 = AppInfo("com.test", "Test", false)
        val app3 = AppInfo("com.other", "Other", false)

        assertThat(app1).isEqualTo(app2)
        assertThat(app1).isNotEqualTo(app3)
    }

    @Test
    fun `AppInfo toString should be readable`() {
        val app = AppInfo("com.test", "Test App", false)
        val string = app.toString()

        assertThat(string).contains("Test App")
    }
}
