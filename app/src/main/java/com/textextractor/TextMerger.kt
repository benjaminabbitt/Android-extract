package com.textextractor

/**
 * TextMerger - Handles merging and appending of extracted text data
 *
 * This class provides functionality to:
 * - Merge multiple text extractions into a single string
 * - Support various formatting options (timestamps, app info)
 * - Group text by application
 * - Manage selection state for multi-select merge operations
 * - Remove duplicates and filter empty entries
 *
 * Implementation follows TDD - tests written first in TextMergerTest
 */
class TextMerger {

    private val selectedItems = mutableSetOf<ExtractedTextData>()

    /**
     * Merge a list of extracted text into a single string
     *
     * @param items List of text to merge
     * @param separator Separator between items (default: newline)
     * @param removeDuplicates Remove consecutive duplicate texts (default: false)
     * @return Merged text string
     */
    fun merge(
        items: List<ExtractedTextData>,
        separator: String = "\n",
        removeDuplicates: Boolean = false
    ): String {
        var processedItems = items
            .map { it.text.trim() }
            .filter { it.isNotBlank() }

        if (removeDuplicates) {
            processedItems = processedItems.distinct()
        }

        return processedItems.joinToString(separator)
    }

    /**
     * Merge text with timestamps
     *
     * @param items List of text to merge
     * @return Merged string with timestamps
     */
    fun mergeWithTimestamps(items: List<ExtractedTextData>): String {
        return items
            .filter { it.text.trim().isNotBlank() }
            .joinToString("\n") { item ->
                "[${item.getFormattedTimestamp()}] ${item.text.trim()}"
            }
    }

    /**
     * Merge text with app information
     *
     * @param items List of text to merge
     * @return Merged string with app names
     */
    fun mergeWithAppInfo(items: List<ExtractedTextData>): String {
        return items
            .filter { it.text.trim().isNotBlank() }
            .joinToString("\n") { item ->
                "[${item.appName}] ${item.text.trim()}"
            }
    }

    /**
     * Merge text grouped by application
     *
     * @param items List of text to merge
     * @return Map of package name to merged text
     */
    fun mergeByApp(items: List<ExtractedTextData>): Map<String, String> {
        return items
            .filter { it.text.trim().isNotBlank() }
            .groupBy { it.packageName }
            .mapValues { (_, texts) ->
                texts.joinToString("\n") { it.text.trim() }
            }
    }

    /**
     * Set the current selection
     *
     * @param items Items to select
     */
    fun setSelection(items: List<ExtractedTextData>) {
        selectedItems.clear()
        selectedItems.addAll(items)
    }

    /**
     * Append an item to current selection
     *
     * @param item Item to append
     */
    fun append(item: ExtractedTextData) {
        selectedItems.add(item)
    }

    /**
     * Clear the current selection
     */
    fun clearSelection() {
        selectedItems.clear()
    }

    /**
     * Check if an item is selected
     *
     * @param item Item to check
     * @return True if selected
     */
    fun isSelected(item: ExtractedTextData): Boolean {
        return selectedItems.contains(item)
    }

    /**
     * Get currently selected items
     *
     * @return List of selected items
     */
    fun getSelectedText(): List<ExtractedTextData> {
        return selectedItems.toList()
    }

    /**
     * Get merged text from current selection
     *
     * @return Merged string of selected items
     */
    fun getMergedSelection(): String {
        return merge(selectedItems.toList())
    }

    /**
     * Toggle selection state of an item
     *
     * @param item Item to toggle
     */
    fun toggleSelection(item: ExtractedTextData) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
        } else {
            selectedItems.add(item)
        }
    }

    /**
     * Get count of selected items
     *
     * @return Number of selected items
     */
    fun getSelectionCount(): Int {
        return selectedItems.size
    }
}
