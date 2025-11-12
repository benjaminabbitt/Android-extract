package com.textextractor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * TDD Tests for ClipboardHelper - written BEFORE implementation
 * Tests clipboard copy functionality
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ClipboardHelperTest {

    private lateinit var context: Context
    private lateinit var clipboardHelper: ClipboardHelper

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        clipboardHelper = ClipboardHelper(context)
    }

    @Test
    fun `copyToClipboard should copy text to system clipboard`() {
        val testText = "Test clipboard text"

        clipboardHelper.copyToClipboard(testText, "Test Label")

        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboardManager.primaryClip

        assertThat(clip).isNotNull()
        assertThat(clip?.itemCount).isEqualTo(1)
        assertThat(clip?.getItemAt(0)?.text.toString()).isEqualTo(testText)
    }

    @Test
    fun `copyToClipboard should use default label`() {
        clipboardHelper.copyToClipboard("Test text")

        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboardManager.primaryClip

        assertThat(clip).isNotNull()
        assertThat(clip?.description?.label.toString()).isEqualTo("Extracted Text")
    }

    @Test
    fun `copyToClipboard should handle empty string`() {
        clipboardHelper.copyToClipboard("")

        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboardManager.primaryClip

        assertThat(clip).isNotNull()
        assertThat(clip?.getItemAt(0)?.text.toString()).isEmpty()
    }

    @Test
    fun `copyMergedText should copy merged text from multiple items`() {
        val items = listOf(
            ExtractedTextData(
                packageName = "com.app1",
                appName = "App 1",
                text = "First",
                className = null,
                viewIdResourceName = null,
                eventType = "Test"
            ),
            ExtractedTextData(
                packageName = "com.app1",
                appName = "App 1",
                text = "Second",
                className = null,
                viewIdResourceName = null,
                eventType = "Test"
            )
        )

        clipboardHelper.copyMergedText(items)

        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboardManager.primaryClip

        assertThat(clip).isNotNull()
        val copiedText = clip?.getItemAt(0)?.text.toString()
        assertThat(copiedText).contains("First")
        assertThat(copiedText).contains("Second")
    }

    @Test
    fun `getClipboardText should return current clipboard content`() {
        val testText = "Clipboard content"
        clipboardHelper.copyToClipboard(testText)

        val result = clipboardHelper.getClipboardText()

        assertThat(result).isEqualTo(testText)
    }

    @Test
    fun `getClipboardText should return null when clipboard is empty`() {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.clearPrimaryClip()

        val result = clipboardHelper.getClipboardText()

        assertThat(result).isNull()
    }

    @Test
    fun `hasClipboardText should return true when clipboard has text`() {
        clipboardHelper.copyToClipboard("Some text")

        val result = clipboardHelper.hasClipboardText()

        assertThat(result).isTrue()
    }

    @Test
    fun `hasClipboardText should return false when clipboard is empty`() {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.clearPrimaryClip()

        val result = clipboardHelper.hasClipboardText()

        assertThat(result).isFalse()
    }
}
