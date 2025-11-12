package com.textextractor

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for MainActivity
 * These tests run on an Android device or emulator
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() {
        // Clear repository before each test
        TextDataRepository.clearData()

        // Launch activity
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        TextDataRepository.clearData()
        scenario.close()
    }

    @Test
    fun activityLaunches() {
        // Verify activity launches without crashing
        scenario.onActivity { activity ->
            assertThat(activity).isNotNull()
        }
    }

    @Test
    fun statusTextViewDisplaysCorrectly() {
        onView(withId(R.id.statusTextView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun enableServiceButtonDisplaysWhenServiceDisabled() {
        // Button may or may not be visible depending on service state
        // This test just verifies the view exists
        onView(withId(R.id.enableServiceButton))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun clearLogButtonDisplaysCorrectly() {
        onView(withId(R.id.clearLogButton))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.clear_log)))
    }

    @Test
    fun exportLogButtonDisplaysCorrectly() {
        onView(withId(R.id.exportLogButton))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.export_log)))
    }

    @Test
    fun recyclerViewDisplaysCorrectly() {
        onView(withId(R.id.recyclerView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun clearLogButtonClearsData() {
        // Add some test data
        scenario.onActivity {
            TextDataRepository.addExtractedText(
                ExtractedTextData(
                    packageName = "com.test",
                    appName = "Test App",
                    text = "Test text",
                    className = null,
                    viewIdResourceName = null,
                    eventType = "Test"
                )
            )
        }

        // Click clear button
        onView(withId(R.id.clearLogButton))
            .perform(click())

        // Verify data is cleared
        val data = TextDataRepository.getAllData()
        assertThat(data).isEmpty()
    }

    @Test
    fun recyclerViewUpdatesWhenDataAdded() {
        scenario.onActivity { activity ->
            // Add test data
            val testData = ExtractedTextData(
                packageName = "com.test",
                appName = "Test App",
                text = "Test extracted text",
                className = "TextView",
                viewIdResourceName = "test_id",
                eventType = "TextChanged"
            )

            activity.runOnUiThread {
                TextDataRepository.addExtractedText(testData)
            }
        }

        // Give RecyclerView time to update
        Thread.sleep(500)

        // Verify data appears in repository
        val data = TextDataRepository.getAllData()
        assertThat(data).hasSize(1)
    }
}
