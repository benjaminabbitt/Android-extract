package com.textextractor

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * TDD Tests for TextMerger - written BEFORE implementation
 * These tests define the expected behavior of text merging/appending
 */
class TextMergerTest {

    private lateinit var merger: TextMerger
    private lateinit var testData1: ExtractedTextData
    private lateinit var testData2: ExtractedTextData
    private lateinit var testData3: ExtractedTextData

    @Before
    fun setup() {
        merger = TextMerger()

        testData1 = ExtractedTextData(
            packageName = "com.app1",
            appName = "App 1",
            text = "First text",
            className = "TextView",
            viewIdResourceName = "id1",
            timestamp = 1000L,
            eventType = "TextChanged"
        )

        testData2 = ExtractedTextData(
            packageName = "com.app1",
            appName = "App 1",
            text = "Second text",
            className = "TextView",
            viewIdResourceName = "id2",
            timestamp = 2000L,
            eventType = "TextChanged"
        )

        testData3 = ExtractedTextData(
            packageName = "com.app2",
            appName = "App 2",
            text = "Third text",
            className = "EditText",
            viewIdResourceName = "id3",
            timestamp = 3000L,
            eventType = "ViewFocused"
        )
    }

    @Test
    fun `merge should concatenate text with newline separator`() {
        val result = merger.merge(listOf(testData1, testData2))

        assertThat(result).isEqualTo("First text\nSecond text")
    }

    @Test
    fun `merge should handle single item`() {
        val result = merger.merge(listOf(testData1))

        assertThat(result).isEqualTo("First text")
    }

    @Test
    fun `merge should handle empty list`() {
        val result = merger.merge(emptyList())

        assertThat(result).isEmpty()
    }

    @Test
    fun `merge should preserve order`() {
        val result = merger.merge(listOf(testData2, testData1, testData3))

        assertThat(result).isEqualTo("Second text\nFirst text\nThird text")
    }

    @Test
    fun `mergeWithTimestamps should include timestamps`() {
        val result = merger.mergeWithTimestamps(listOf(testData1, testData2))

        assertThat(result).contains("First text")
        assertThat(result).contains("Second text")
        assertThat(result).matches(".*\\d{2}:\\d{2}:\\d{2}\\.\\d{3}.*")
    }

    @Test
    fun `mergeWithAppInfo should include app names`() {
        val result = merger.mergeWithAppInfo(listOf(testData1, testData2, testData3))

        assertThat(result).contains("App 1")
        assertThat(result).contains("App 2")
        assertThat(result).contains("First text")
        assertThat(result).contains("Second text")
        assertThat(result).contains("Third text")
    }

    @Test
    fun `mergeByApp should group by package name`() {
        val result = merger.mergeByApp(listOf(testData1, testData2, testData3))

        assertThat(result).hasSize(2)
        assertThat(result).containsKey("com.app1")
        assertThat(result).containsKey("com.app2")
        assertThat(result["com.app1"]).isEqualTo("First text\nSecond text")
        assertThat(result["com.app2"]).isEqualTo("Third text")
    }

    @Test
    fun `mergeWithCustomSeparator should use custom separator`() {
        val result = merger.merge(listOf(testData1, testData2), separator = " | ")

        assertThat(result).isEqualTo("First text | Second text")
    }

    @Test
    fun `merge should trim whitespace from each text`() {
        val data = testData1.copy(text = "  Trimmed text  ")
        val result = merger.merge(listOf(data))

        assertThat(result).isEqualTo("Trimmed text")
    }

    @Test
    fun `merge should filter out empty strings`() {
        val emptyData = testData1.copy(text = "   ")
        val result = merger.merge(listOf(testData1, emptyData, testData2))

        assertThat(result).isEqualTo("First text\nSecond text")
    }

    @Test
    fun `merge should remove duplicate consecutive texts`() {
        val duplicate = testData1.copy(timestamp = 1500L)
        val result = merger.merge(
            listOf(testData1, duplicate, testData2),
            removeDuplicates = true
        )

        assertThat(result).isEqualTo("First text\nSecond text")
    }

    @Test
    fun `append should add text to existing selection`() {
        merger.setSelection(listOf(testData1))
        merger.append(testData2)

        val result = merger.getSelectedText()
        assertThat(result).hasSize(2)
        assertThat(result).containsExactly(testData1, testData2).inOrder()
    }

    @Test
    fun `clearSelection should remove all selected items`() {
        merger.setSelection(listOf(testData1, testData2))
        merger.clearSelection()

        val result = merger.getSelectedText()
        assertThat(result).isEmpty()
    }

    @Test
    fun `isSelected should return true for selected items`() {
        merger.setSelection(listOf(testData1))

        assertThat(merger.isSelected(testData1)).isTrue()
        assertThat(merger.isSelected(testData2)).isFalse()
    }

    @Test
    fun `getMergedSelection should merge selected items`() {
        merger.setSelection(listOf(testData1, testData2))

        val result = merger.getMergedSelection()
        assertThat(result).isEqualTo("First text\nSecond text")
    }

    @Test
    fun `toggleSelection should add if not selected`() {
        merger.toggleSelection(testData1)

        assertThat(merger.isSelected(testData1)).isTrue()
    }

    @Test
    fun `toggleSelection should remove if already selected`() {
        merger.setSelection(listOf(testData1))
        merger.toggleSelection(testData1)

        assertThat(merger.isSelected(testData1)).isFalse()
    }

    @Test
    fun `getSelectionCount should return correct count`() {
        assertThat(merger.getSelectionCount()).isEqualTo(0)

        merger.setSelection(listOf(testData1, testData2))
        assertThat(merger.getSelectionCount()).isEqualTo(2)
    }
}
