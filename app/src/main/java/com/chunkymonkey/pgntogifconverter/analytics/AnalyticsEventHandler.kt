package com.chunkymonkey.pgntogifconverter.analytics

interface AnalyticsEventHandler {
    fun initialize()
    fun logEvent(analyticsEvent: AnalyticsEvent)
}