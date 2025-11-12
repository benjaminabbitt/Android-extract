package com.textextractor

import android.accessibilityservice.AccessibilityService
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import timber.log.Timber

/**
 * Accessibility Service that extracts text from other applications
 *
 * This service monitors accessibility events and extracts text from:
 * - Text views and labels
 * - Edit text fields
 * - Buttons with text
 * - Content descriptions
 */
class TextExtractionAccessibilityService : AccessibilityService() {

    private val packageManager: PackageManager by lazy { applicationContext.packageManager }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Timber.d("Text Extraction Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        try {
            val packageName = event.packageName?.toString() ?: return

            // Skip our own app to avoid recursion
            if (packageName == this.packageName) {
                return
            }

            // Get app name
            val appName = try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                packageName
            }

            val eventType = getEventTypeName(event.eventType)

            // Extract text from the event itself
            event.text?.forEach { text ->
                if (text.isNotBlank()) {
                    extractAndStore(
                        packageName = packageName,
                        appName = appName,
                        text = text.toString(),
                        className = event.className?.toString(),
                        viewIdResourceName = null,
                        eventType = eventType
                    )
                }
            }

            // Extract content description
            event.contentDescription?.let { desc ->
                if (desc.isNotBlank()) {
                    extractAndStore(
                        packageName = packageName,
                        appName = appName,
                        text = desc.toString(),
                        className = event.className?.toString(),
                        viewIdResourceName = null,
                        eventType = "$eventType (ContentDesc)"
                    )
                }
            }

            // Extract text from node hierarchy
            event.source?.let { rootNode ->
                extractTextFromNode(rootNode, packageName, appName, eventType)
                rootNode.recycle()
            }

        } catch (e: Exception) {
            Timber.e(e, "Error processing accessibility event")
        }
    }

    /**
     * Recursively extract text from accessibility node tree
     */
    private fun extractTextFromNode(
        node: AccessibilityNodeInfo,
        packageName: String,
        appName: String,
        eventType: String
    ) {
        try {
            // Extract text from current node
            node.text?.let { text ->
                if (text.isNotBlank()) {
                    extractAndStore(
                        packageName = packageName,
                        appName = appName,
                        text = text.toString(),
                        className = node.className?.toString(),
                        viewIdResourceName = node.viewIdResourceName,
                        eventType = eventType
                    )
                }
            }

            // Extract content description
            node.contentDescription?.let { desc ->
                if (desc.isNotBlank()) {
                    extractAndStore(
                        packageName = packageName,
                        appName = appName,
                        text = desc.toString(),
                        className = node.className?.toString(),
                        viewIdResourceName = node.viewIdResourceName,
                        eventType = "$eventType (NodeDesc)"
                    )
                }
            }

            // Extract hint text (for EditText fields)
            if (node.className?.contains("EditText") == true) {
                node.hintText?.let { hint ->
                    if (hint.isNotBlank()) {
                        extractAndStore(
                            packageName = packageName,
                            appName = appName,
                            text = "HINT: $hint",
                            className = node.className?.toString(),
                            viewIdResourceName = node.viewIdResourceName,
                            eventType = "$eventType (Hint)"
                        )
                    }
                }
            }

            // Recursively process child nodes
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { childNode ->
                    extractTextFromNode(childNode, packageName, appName, eventType)
                    childNode.recycle()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error extracting text from node")
        }
    }

    private fun extractAndStore(
        packageName: String,
        appName: String,
        text: String,
        className: String?,
        viewIdResourceName: String?,
        eventType: String
    ) {
        val data = ExtractedTextData(
            packageName = packageName,
            appName = appName,
            text = text,
            className = className,
            viewIdResourceName = viewIdResourceName,
            eventType = eventType
        )

        TextDataRepository.addExtractedText(data)
        Timber.v("Extracted: ${data.toLogString()}")
    }

    private fun getEventTypeName(eventType: Int): String {
        return when (eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> "ViewClicked"
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> "ViewFocused"
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> "TextChanged"
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> "WindowStateChanged"
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> "ContentChanged"
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> "ViewScrolled"
            AccessibilityEvent.TYPE_VIEW_SELECTED -> "ViewSelected"
            else -> "Event($eventType)"
        }
    }

    override fun onInterrupt() {
        Timber.d("Text Extraction Accessibility Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Text Extraction Accessibility Service destroyed")
    }
}
