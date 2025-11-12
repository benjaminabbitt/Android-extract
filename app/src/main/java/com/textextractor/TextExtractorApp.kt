package com.textextractor

import android.app.Application
import timber.log.Timber

class TextExtractorApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("TextExtractorApp initialized")
    }
}
