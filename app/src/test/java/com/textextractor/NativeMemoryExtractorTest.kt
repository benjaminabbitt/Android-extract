package com.textextractor

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for NativeMemoryExtractor
 * Tests the JNI wrapper functionality
 */
class NativeMemoryExtractorTest {

    @Before
    fun setup() {
        // Note: Native library won't be loaded in unit tests
        // These tests verify the Kotlin wrapper logic
    }

    @Test
    fun `isNativeLibraryAvailable should return false in unit tests`() {
        // Native library won't load in unit test environment
        assertThat(NativeMemoryExtractor.isNativeLibraryAvailable()).isFalse()
    }

    @Test
    fun `checkRootAccess should return error message when library not loaded`() {
        val result = NativeMemoryExtractor.checkRootAccess()

        assertThat(result).contains("Native library not loaded")
    }

    @Test
    fun `readProcessMemory should return error message when library not loaded`() {
        val result = NativeMemoryExtractor.readProcessMemory(1234)

        assertThat(result).contains("Native library not loaded")
        assertThat(result).contains("Accessibility Service")
    }

    @Test
    fun `extractStrings should return error message when library not loaded`() {
        val result = NativeMemoryExtractor.extractStrings(1234, 4)

        assertThat(result).contains("Native library not loaded")
        assertThat(result).contains("Accessibility Service")
    }

    @Test
    fun `extractStrings should use default minLength`() {
        // Verify method signature accepts default parameter
        val result = NativeMemoryExtractor.extractStrings(1234)

        assertThat(result).isNotNull()
    }

    @Test
    fun `getProcessIdByPackage should handle valid package`() {
        // This test verifies the method doesn't crash
        // Actual PID lookup will vary by environment
        val result = NativeMemoryExtractor.getProcessIdByPackage("com.android.systemui")

        // Result can be null or a PID, both are valid
        if (result != null) {
            assertThat(result).isGreaterThan(0)
        }
    }

    @Test
    fun `getProcessIdByPackage should return null for invalid package`() {
        val result = NativeMemoryExtractor.getProcessIdByPackage("com.nonexistent.package.name.that.does.not.exist")

        assertThat(result).isNull()
    }

    @Test
    fun `getProcessIdByPackage should handle empty string`() {
        val result = NativeMemoryExtractor.getProcessIdByPackage("")

        assertThat(result).isNull()
    }
}
