package com.textextractor

/**
 * AppFilter - Utility for filtering extracted text by application
 *
 * Provides functionality to:
 * - Filter data by package name
 * - Get unique packages from data
 * - Map packages to app names
 *
 * Implementation follows TDD - tests written first in AppFilterTest
 */
object AppFilter {

    /**
     * Filter extracted text data by package name
     *
     * @param data List of extracted text data
     * @param packageName Package name to filter by, or null for all data
     * @return Filtered list
     */
    fun filterByApp(data: List<ExtractedTextData>, packageName: String?): List<ExtractedTextData> {
        return if (packageName == null) {
            data
        } else {
            data.filter { it.packageName == packageName }
        }
    }

    /**
     * Get unique package names from extracted data
     *
     * @param data List of extracted text data
     * @return Set of unique package names
     */
    fun getUniquePackages(data: List<ExtractedTextData>): Set<String> {
        return data.map { it.packageName }.toSet()
    }

    /**
     * Create map of package names to app names
     *
     * @param data List of extracted text data
     * @return Map of packageName to appName
     */
    fun getAppNamesMap(data: List<ExtractedTextData>): Map<String, String> {
        return data
            .distinctBy { it.packageName }
            .associate { it.packageName to it.appName }
    }
}
