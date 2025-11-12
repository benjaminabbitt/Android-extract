package com.textextractor

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for TextLogAdapter
 */
class TextLogAdapterTest {

    private lateinit var adapter: TextLogAdapter
    private lateinit var testData: MutableList<ExtractedTextData>

    @Before
    fun setup() {
        testData = mutableListOf(
            ExtractedTextData(
                packageName = "com.app1",
                appName = "App 1",
                text = "Text 1",
                className = "TextView",
                viewIdResourceName = "id1",
                timestamp = 1000L,
                eventType = "TextChanged"
            ),
            ExtractedTextData(
                packageName = "com.app2",
                appName = "App 2",
                text = "Text 2",
                className = "EditText",
                viewIdResourceName = "id2",
                timestamp = 2000L,
                eventType = "ViewFocused"
            )
        )

        adapter = TextLogAdapter(testData)
    }

    @Test
    fun `adapter should initialize with items`() {
        assertThat(adapter.itemCount).isEqualTo(2)
    }

    @Test
    fun `addItem should increase item count`() {
        val newItem = ExtractedTextData(
            packageName = "com.app3",
            appName = "App 3",
            text = "Text 3",
            className = null,
            viewIdResourceName = null,
            eventType = "Test"
        )

        adapter.addItem(newItem)

        assertThat(adapter.itemCount).isEqualTo(3)
    }

    @Test
    fun `clearItems should remove all items`() {
        adapter.clearItems()

        assertThat(adapter.itemCount).isEqualTo(0)
    }

    @Test
    fun `setItems should replace all items`() {
        val newItems = listOf(
            ExtractedTextData(
                packageName = "com.new",
                appName = "New App",
                text = "New Text",
                className = null,
                viewIdResourceName = null,
                eventType = "Test"
            )
        )

        adapter.setItems(newItems)

        assertThat(adapter.itemCount).isEqualTo(1)
    }

    @Test
    fun `empty adapter should have zero items`() {
        val emptyAdapter = TextLogAdapter(mutableListOf())

        assertThat(emptyAdapter.itemCount).isEqualTo(0)
    }
}
