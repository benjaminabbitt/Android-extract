package com.textextractor

import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing extracted text from an application
 */
data class ExtractedTextData(
    val packageName: String,
    val appName: String,
    val text: String,
    val className: String?,
    val viewIdResourceName: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String
) {
    fun getFormattedTimestamp(): String {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun toLogString(): String {
        return buildString {
            append("[${getFormattedTimestamp()}] ")
            append("$appName ($packageName)\n")
            append("Event: $eventType\n")
            className?.let { append("Class: $it\n") }
            viewIdResourceName?.let { append("ViewID: $it\n") }
            append("Text: $text\n")
            append("---")
        }
    }
}

/**
 * Singleton to store extracted text data
 * Implements ITextDataRepository for IoC/testing
 */
object TextDataRepository : ITextDataRepository {
    private val extractedData = mutableListOf<ExtractedTextData>()
    private val listeners = mutableListOf<(ExtractedTextData) -> Unit>()

    override fun addExtractedText(data: ExtractedTextData) {
        synchronized(extractedData) {
            extractedData.add(data)
            // Keep only last 1000 entries to prevent memory issues
            if (extractedData.size > 1000) {
                extractedData.removeAt(0)
            }
        }

        // Notify listeners
        listeners.forEach { it(data) }
    }

    override fun getAllData(): List<ExtractedTextData> {
        synchronized(extractedData) {
            return extractedData.toList()
        }
    }

    override fun clearData() {
        synchronized(extractedData) {
            extractedData.clear()
        }
    }

    override fun addListener(listener: (ExtractedTextData) -> Unit) {
        listeners.add(listener)
    }

    override fun removeListener(listener: (ExtractedTextData) -> Unit) {
        listeners.remove(listener)
    }

    override fun getDataByPackage(packageName: String): List<ExtractedTextData> {
        synchronized(extractedData) {
            return extractedData.filter { it.packageName == packageName }
        }
    }
}
