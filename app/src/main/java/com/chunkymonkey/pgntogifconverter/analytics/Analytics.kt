package com.chunkymonkey.pgntogifconverter.analytics

interface Analytics {
    fun initialize()
    fun logEvent(event: Event)
}