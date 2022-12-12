package com.chunkymonkey.pgntogifconverter.analytics

import com.chunkymonkey.pgntogifconverter.data.SettingsData
import com.chunkymonkey.pgntogifconverter.data.SettingsStorage
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.ParametersBuilder
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

class FirebaseAnalyticsEventHandler(
    private val settingsStorage: SettingsStorage
) : AnalyticsEventHandler {
    lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun initialize() {
        firebaseAnalytics = Firebase.analytics
    }

    override fun logEvent(analyticsEvent: AnalyticsEvent) {
        try {
            if (this::firebaseAnalytics.isInitialized) {
                when (analyticsEvent) {
                    is AnalyticsEvent.CreateGifClicked -> {
                        firebaseAnalytics.logEvent(analyticsEvent.title) {
                            param(currentPgnTextParam, analyticsEvent.currentPgnText.take(40))
                            addParamFromSettings(this, settingsStorage.getSettings())
                        }
                    }
                    is AnalyticsEvent.ExportPgnClicked -> {
                        firebaseAnalytics.logEvent(analyticsEvent.title) {
                            param(currentPgnTextParam, analyticsEvent.currentPgnText.take(40))
                            addParamFromSettings(this, settingsStorage.getSettings())
                        }
                    }
                    else -> {
                        firebaseAnalytics.logEvent(analyticsEvent.title) {
                            addParamFromSettings(this, settingsStorage.getSettings())
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    private fun addParamFromSettings(paramBuilder: ParametersBuilder, settingsData: SettingsData) {
        paramBuilder.param(showPlayerNameParam, settingsData.showPlayerName.toString())
        paramBuilder.param(showPlayerRatingParam, settingsData.showPlayerRating.toString())
        paramBuilder.param(showBoardCoordinatesParam, settingsData.showBoardCoordinates.toString())
        paramBuilder.param(moveDelayParam, settingsData.moveDelay.toString())
        paramBuilder.param(lastMoveDelay, settingsData.lastMoveDelay.toString())
    }
}