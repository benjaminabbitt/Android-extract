# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Timber logging in debug builds
-assumenosideeffects class timber.log.Timber* {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}

# Keep accessibility service
-keep class com.textextractor.TextExtractionAccessibilityService { *; }

# Keep data classes
-keep class com.textextractor.ExtractedTextData { *; }
