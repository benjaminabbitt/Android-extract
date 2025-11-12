package com.textextractor

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Unit tests for TextDataRepository
 * Tests the singleton data storage and listener functionality
 */
class TextDataRepositoryTest {

    private lateinit var testData1: ExtractedTextData
    private lateinit var testData2: ExtractedTextData
    private lateinit var testData3: ExtractedTextData

    @Before
    fun setup() {
        // Clear repository before each test
        TextDataRepository.clearData()

        testData1 = ExtractedTextData(
            packageName = "com.app1",
            appName = "App 1",
            text = "Text from app 1",
            className = "TextView",
            viewIdResourceName = "id1",
            timestamp = 1000L,
            eventType = "TextChanged"
        )

        testData2 = ExtractedTextData(
            packageName = "com.app2",
            appName = "App 2",
            text = "Text from app 2",
            className = "EditText",
            viewIdResourceName = "id2",
            timestamp = 2000L,
            eventType = "ViewFocused"
        )

        testData3 = ExtractedTextData(
            packageName = "com.app1",
            appName = "App 1",
            text = "More text from app 1",
            className = "Button",
            viewIdResourceName = "id3",
            timestamp = 3000L,
            eventType = "ViewClicked"
        )
    }

    @After
    fun tearDown() {
        TextDataRepository.clearData()
    }

    @Test
    fun `addExtractedText should add data to repository`() {
        TextDataRepository.addExtractedText(testData1)

        val allData = TextDataRepository.getAllData()
        assertThat(allData).hasSize(1)
        assertThat(allData[0]).isEqualTo(testData1)
    }

    @Test
    fun `addExtractedText should add multiple items`() {
        TextDataRepository.addExtractedText(testData1)
        TextDataRepository.addExtractedText(testData2)
        TextDataRepository.addExtractedText(testData3)

        val allData = TextDataRepository.getAllData()
        assertThat(allData).hasSize(3)
        assertThat(allData).containsExactly(testData1, testData2, testData3).inOrder()
    }

    @Test
    fun `getAllData should return empty list when no data`() {
        val allData = TextDataRepository.getAllData()
        assertThat(allData).isEmpty()
    }

    @Test
    fun `clearData should remove all data`() {
        TextDataRepository.addExtractedText(testData1)
        TextDataRepository.addExtractedText(testData2)

        TextDataRepository.clearData()

        val allData = TextDataRepository.getAllData()
        assertThat(allData).isEmpty()
    }

    @Test
    fun `getDataByPackage should filter by package name`() {
        TextDataRepository.addExtractedText(testData1)
        TextDataRepository.addExtractedText(testData2)
        TextDataRepository.addExtractedText(testData3)

        val app1Data = TextDataRepository.getDataByPackage("com.app1")
        assertThat(app1Data).hasSize(2)
        assertThat(app1Data).containsExactly(testData1, testData3)
    }

    @Test
    fun `getDataByPackage should return empty list for unknown package`() {
        TextDataRepository.addExtractedText(testData1)

        val data = TextDataRepository.getDataByPackage("com.unknown")
        assertThat(data).isEmpty()
    }

    @Test
    fun `listener should be notified when data is added`() {
        val latch = CountDownLatch(1)
        var capturedData: ExtractedTextData? = null

        val listener: (ExtractedTextData) -> Unit = { data ->
            capturedData = data
            latch.countDown()
        }

        TextDataRepository.addListener(listener)
        TextDataRepository.addExtractedText(testData1)

        // Wait for listener to be called (with timeout)
        val called = latch.await(1, TimeUnit.SECONDS)

        assertThat(called).isTrue()
        assertThat(capturedData).isEqualTo(testData1)

        TextDataRepository.removeListener(listener)
    }

    @Test
    fun `multiple listeners should all be notified`() {
        val latch = CountDownLatch(2)
        var count1 = 0
        var count2 = 0

        val listener1: (ExtractedTextData) -> Unit = {
            count1++
            latch.countDown()
        }
        val listener2: (ExtractedTextData) -> Unit = {
            count2++
            latch.countDown()
        }

        TextDataRepository.addListener(listener1)
        TextDataRepository.addListener(listener2)
        TextDataRepository.addExtractedText(testData1)

        val called = latch.await(1, TimeUnit.SECONDS)

        assertThat(called).isTrue()
        assertThat(count1).isEqualTo(1)
        assertThat(count2).isEqualTo(1)

        TextDataRepository.removeListener(listener1)
        TextDataRepository.removeListener(listener2)
    }

    @Test
    fun `removeListener should stop notifications`() {
        var count = 0
        val listener: (ExtractedTextData) -> Unit = { count++ }

        TextDataRepository.addListener(listener)
        TextDataRepository.addExtractedText(testData1)

        TextDataRepository.removeListener(listener)
        TextDataRepository.addExtractedText(testData2)

        // Only first addition should trigger listener
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `repository should limit to 1000 entries`() {
        // Add 1100 items
        repeat(1100) { index ->
            val data = testData1.copy(
                text = "Text $index",
                timestamp = index.toLong()
            )
            TextDataRepository.addExtractedText(data)
        }

        val allData = TextDataRepository.getAllData()
        assertThat(allData).hasSize(1000)

        // Should keep the most recent 1000
        assertThat(allData.first().text).isEqualTo("Text 100")
        assertThat(allData.last().text).isEqualTo("Text 1099")
    }

    @Test
    fun `getAllData should return defensive copy`() {
        TextDataRepository.addExtractedText(testData1)

        val data1 = TextDataRepository.getAllData()
        val data2 = TextDataRepository.getAllData()

        // Should be different list instances
        assertThat(data1).isNotSameInstanceAs(data2)
        // But contain same data
        assertThat(data1).isEqualTo(data2)
    }

    @Test
    fun `getDataByPackage should return defensive copy`() {
        TextDataRepository.addExtractedText(testData1)

        val data1 = TextDataRepository.getDataByPackage("com.app1")
        val data2 = TextDataRepository.getDataByPackage("com.app1")

        assertThat(data1).isNotSameInstanceAs(data2)
        assertThat(data1).isEqualTo(data2)
    }
}
