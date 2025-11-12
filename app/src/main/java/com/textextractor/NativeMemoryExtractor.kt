package com.textextractor

import timber.log.Timber

/**
 * JNI wrapper for native memory extraction functionality
 *
 * This class provides access to Rust-based native code for advanced
 * memory extraction on rooted devices. Falls back gracefully if native
 * library is not available or device is not rooted.
 */
object NativeMemoryExtractor {

    private var nativeLibraryLoaded = false

    init {
        try {
            System.loadLibrary("native_extractor")
            nativeLibraryLoaded = true
            Timber.d("Native library loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            Timber.w("Native library not available: ${e.message}")
            Timber.w("Advanced memory extraction features will be disabled")
        } catch (e: Exception) {
            Timber.e(e, "Error loading native library")
        }
    }

    /**
     * Check if native library is available
     */
    fun isNativeLibraryAvailable(): Boolean = nativeLibraryLoaded

    /**
     * Check if device has root access
     */
    fun checkRootAccess(): String {
        return if (nativeLibraryLoaded) {
            try {
                nativeCheckRootAccess()
            } catch (e: Exception) {
                Timber.e(e, "Error checking root access")
                "Error checking root access: ${e.message}"
            }
        } else {
            "Native library not loaded"
        }
    }

    /**
     * Read process memory (requires root)
     *
     * @param pid Process ID to read memory from
     * @return String containing memory information
     */
    fun readProcessMemory(pid: Int): String {
        return if (nativeLibraryLoaded) {
            try {
                nativeReadProcessMemory(pid)
            } catch (e: Exception) {
                Timber.e(e, "Error reading process memory")
                "Error: ${e.message}"
            }
        } else {
            "Native library not loaded - use Accessibility Service instead"
        }
    }

    /**
     * Extract printable strings from process memory (requires root)
     *
     * @param pid Process ID to extract strings from
     * @param minLength Minimum string length to extract
     * @return String containing extracted strings
     */
    fun extractStrings(pid: Int, minLength: Int = 4): String {
        return if (nativeLibraryLoaded) {
            try {
                nativeExtractStrings(pid, minLength)
            } catch (e: Exception) {
                Timber.e(e, "Error extracting strings")
                "Error: ${e.message}"
            }
        } else {
            "Native library not loaded - use Accessibility Service instead"
        }
    }

    /**
     * Get process ID by package name
     * Note: This is a helper function that uses standard Android APIs
     */
    fun getProcessIdByPackage(packageName: String): Int? {
        return try {
            val process = Runtime.getRuntime().exec("pidof $packageName")
            val reader = process.inputStream.bufferedReader()
            val pid = reader.readText().trim().toIntOrNull()
            process.waitFor()
            pid
        } catch (e: Exception) {
            Timber.e(e, "Error getting process ID for $packageName")
            null
        }
    }

    // Native methods (implemented in Rust)
    @JvmStatic
    private external fun nativeCheckRootAccess(): String

    @JvmStatic
    private external fun nativeReadProcessMemory(pid: Int): String

    @JvmStatic
    private external fun nativeExtractStrings(pid: Int, minLength: Int): String
}
