package com.dmv.texas.analytics

/**
 * Abstraction for analytics event logging. Call sites use this interface
 * so the underlying implementation (Logcat, Firebase, etc.) can be swapped
 * without touching any screen or ViewModel code.
 */
interface AnalyticsLogger {
    fun logEvent(name: String, properties: Map<String, Any> = emptyMap())
}
