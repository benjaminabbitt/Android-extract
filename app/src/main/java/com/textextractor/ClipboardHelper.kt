package com.textextractor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import timber.log.Timber

/**
 * ClipboardHelper - Manages clipboard operations for extracted text
 *
 * Provides functionality to:
 * - Copy text to system clipboard
 * - Merge and copy multiple text extractions
 * - Read clipboard content
 * - Check clipboard state
 *
 * Implementation follows TDD - tests written first in ClipboardHelperTest
 */
class ClipboardHelper(private val context: Context) {

    private val clipboardManager: ClipboardManager by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private val textMerger = TextMerger()

    /**
     * Copy text to clipboard
     *
     * @param text Text to copy
     * @param label Label for the clipboard entry (default: "Extracted Text")
     */
    fun copyToClipboard(text: String, label: String = "Extracted Text") {
        try {
            val clip = ClipData.newPlainText(label, text)
            clipboardManager.setPrimaryClip(clip)
            Timber.d("Copied to clipboard: ${text.take(50)}...")
        } catch (e: Exception) {
            Timber.e(e, "Error copying to clipboard")
        }
    }

    /**
     * Copy merged text from multiple extracted items
     *
     * @param items List of items to merge and copy
     * @param label Clipboard label
     */
    fun copyMergedText(items: List<ExtractedTextData>, label: String = "Merged Extracted Text") {
        val mergedText = textMerger.merge(items)
        copyToClipboard(mergedText, label)
    }

    /**
     * Get current clipboard text
     *
     * @return Clipboard text or null if no text available
     */
    fun getClipboardText(): String? {
        return try {
            val clip = clipboardManager.primaryClip
            if (clip != null && clip.itemCount > 0) {
                clip.getItemAt(0)?.text?.toString()
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error reading clipboard")
            null
        }
    }

    /**
     * Check if clipboard has text content
     *
     * @return True if clipboard contains text
     */
    fun hasClipboardText(): Boolean {
        return try {
            val clip = clipboardManager.primaryClip
            clip != null && clip.itemCount > 0
        } catch (e: Exception) {
            Timber.e(e, "Error checking clipboard")
            false
        }
    }
}
