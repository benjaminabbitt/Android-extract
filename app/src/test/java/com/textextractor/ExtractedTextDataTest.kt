package com.textextractor

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.Before
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for ExtractedTextData
 * Following TDD principles - tests written first
 */
class ExtractedTextDataTest {

    private lateinit var sampleData: ExtractedTextData

    @Before
    fun setup() {
        sampleData = ExtractedTextData(
            packageName = "com.example.test",
            appName = "Test App",
            text = "Sample extracted text",
            className = "android.widget.TextView",
            viewIdResourceName = "com.example.test:id/text_view",
            timestamp = 1699564800000L, // Fixed timestamp for testing
            eventType = "TextChanged"
        )
    }

    @Test
    fun `data class should hold all fields correctly`() {
        assertThat(sampleData.packageName).isEqualTo("com.example.test")
        assertThat(sampleData.appName).isEqualTo("Test App")
        assertThat(sampleData.text).isEqualTo("Sample extracted text")
        assertThat(sampleData.className).isEqualTo("android.widget.TextView")
        assertThat(sampleData.viewIdResourceName).isEqualTo("com.example.test:id/text_view")
        assertThat(sampleData.timestamp).isEqualTo(1699564800000L)
        assertThat(sampleData.eventType).isEqualTo("TextChanged")
    }

    @Test
    fun `getFormattedTimestamp should return formatted time string`() {
        val formatted = sampleData.getFormattedTimestamp()
        assertNotNull(formatted)
        assertThat(formatted).matches("\\d{2}:\\d{2}:\\d{2}\\.\\d{3}")
    }

    @Test
    fun `toLogString should contain all relevant information`() {
        val logString = sampleData.toLogString()

        assertThat(logString).contains("Test App")
        assertThat(logString).contains("com.example.test")
        assertThat(logString).contains("Sample extracted text")
        assertThat(logString).contains("TextChanged")
        assertThat(logString).contains("android.widget.TextView")
        assertThat(logString).contains("com.example.test:id/text_view")
    }

    @Test
    fun `toLogString should handle null className`() {
        val dataWithoutClass = sampleData.copy(className = null)
        val logString = dataWithoutClass.toLogString()

        assertThat(logString).doesNotContain("Class: null")
        assertThat(logString).contains("Sample extracted text")
    }

    @Test
    fun `toLogString should handle null viewIdResourceName`() {
        val dataWithoutViewId = sampleData.copy(viewIdResourceName = null)
        val logString = dataWithoutViewId.toLogString()

        assertThat(logString).doesNotContain("ViewID: null")
        assertThat(logString).contains("Sample extracted text")
    }

    @Test
    fun `data class should support copy for immutability`() {
        val modified = sampleData.copy(text = "Modified text")

        assertThat(sampleData.text).isEqualTo("Sample extracted text")
        assertThat(modified.text).isEqualTo("Modified text")
        assertThat(modified.packageName).isEqualTo(sampleData.packageName)
    }

    @Test
    fun `equals should work correctly for same data`() {
        val duplicate = sampleData.copy()
        assertThat(sampleData).isEqualTo(duplicate)
    }

    @Test
    fun `equals should fail for different data`() {
        val different = sampleData.copy(text = "Different text")
        assertThat(sampleData).isNotEqualTo(different)
    }

    @Test
    fun `hashCode should be consistent`() {
        val duplicate = sampleData.copy()
        assertThat(sampleData.hashCode()).isEqualTo(duplicate.hashCode())
    }
}
