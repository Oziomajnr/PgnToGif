package com.chunkymonkey.pgntogifconverter.dependency

import android.content.Context
import com.chunkymonkey.pgntogifconverter.PgnToGifApplication
import com.chunkymonkey.pgntogifconverter.analytics.AnalyticsEventHandler
import com.chunkymonkey.pgntogifconverter.analytics.FirebaseAnalyticsEventHandler
import com.chunkymonkey.pgntogifconverter.data.PreferenceSettingsStorage
import com.chunkymonkey.pgntogifconverter.data.SettingsStorage
import com.chunkymonkey.pgntogifconverter.preference.PreferenceService

object DependencyFactory {
    fun getAnalyticsEventHandler(): AnalyticsEventHandler =
        FirebaseAnalyticsEventHandler(getSettingsStorage())

    fun getSettingsStorage(): SettingsStorage = PreferenceSettingsStorage(getPreferenceService())
    fun getPreferenceService(): PreferenceService = PreferenceService(getApplicationContext())
    fun getApplicationContext(): Context = PgnToGifApplication.application
}