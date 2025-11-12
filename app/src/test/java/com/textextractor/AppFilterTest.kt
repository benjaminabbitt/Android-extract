package com.textextractor

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * TDD Tests for app filtering functionality
 */
class AppFilterTest {

    private lateinit var testData: List<ExtractedTextData>

    @Before
    fun setup() {
        testData = listOf(
            ExtractedTextData(
                packageName = "com.app1",
                appName = "App 1",
                text = "Text from app 1",
                className = null,
                viewIdResourceName = null,
                eventType = "Test"
            ),
            ExtractedTextData(
                packageName = "com.app2",
                appName = "App 2",
                text = "Text from app 2",
                className = null,
                viewIdResourceName = null,
                eventType = "Test"
            ),
            ExtractedTextData(
                packageName = "com.app1",
                appName = "App 1",
                text = "More text from app 1",
                className = null,
                viewIdResourceName = null,
                eventType = "Test"
            )
        )
    }

    @Test
    fun `filterByApp should return all data when package is null`() {
        val filtered = AppFilter.filterByApp(testData, null)

        assertThat(filtered).hasSize(3)
        assertThat(filtered).isEqualTo(testData)
    }

    @Test
    fun `filterByApp should return only matching package data`() {
        val filtered = AppFilter.filterByApp(testData, "com.app1")

        assertThat(filtered).hasSize(2)
        filtered.forEach {
            assertThat(it.packageName).isEqualTo("com.app1")
        }
    }

    @Test
    fun `filterByApp should return empty list for unknown package`() {
        val filtered = AppFilter.filterByApp(testData, "com.unknown")

        assertThat(filtered).isEmpty()
    }

    @Test
    fun `filterByApp should handle empty data list`() {
        val filtered = AppFilter.filterByApp(emptyList(), "com.app1")

        assertThat(filtered).isEmpty()
    }

    @Test
    fun `getUniquePackages should return distinct packages`() {
        val packages = AppFilter.getUniquePackages(testData)

        assertThat(packages).hasSize(2)
        assertThat(packages).containsExactly("com.app1", "com.app2")
    }

    @Test
    fun `getUniquePackages should return empty set for empty data`() {
        val packages = AppFilter.getUniquePackages(emptyList())

        assertThat(packages).isEmpty()
    }

    @Test
    fun `getAppNamesMap should map package to app name`() {
        val map = AppFilter.getAppNamesMap(testData)

        assertThat(map).hasSize(2)
        assertThat(map["com.app1"]).isEqualTo("App 1")
        assertThat(map["com.app2"]).isEqualTo("App 2")
    }
}
