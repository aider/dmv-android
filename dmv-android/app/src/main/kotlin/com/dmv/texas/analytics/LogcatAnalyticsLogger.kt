package com.dmv.texas.analytics

import android.util.Log

/**
 * Development implementation that writes analytics events to Logcat.
 * Replace with a real SDK (Firebase Analytics, Amplitude, etc.) when ready,
 * keeping call sites unchanged.
 */
class LogcatAnalyticsLogger : AnalyticsLogger {

    override fun logEvent(name: String, properties: Map<String, Any>) {
        val propsString = if (properties.isEmpty()) "" else " | $properties"
        Log.d(TAG, "[$name]$propsString")
    }

    companion object {
        private const val TAG = "Analytics"
    }
}
