package com.chunkymonkey.pgntogifconverter.ui.settings

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import com.chunkymonkey.pgntogifconverter.data.PreferenceSettingsStorage
import com.chunkymonkey.pgntogifconverter.data.SettingsData
import com.chunkymonkey.pgntogifconverter.data.SettingsStorage
import com.chunkymonkey.pgntogifconverter.preference.PreferenceService

class SettingsViewModel(applicationContext: Application) {

    //TODO: Use a DI container or at least a service locator
    private val preferenceService by lazy { PreferenceService(applicationContext.applicationContext) }
    private val preferenceSettingsStorage: SettingsStorage by lazy {
        PreferenceSettingsStorage(preferenceService)
    }

    val settingsUIState by lazy {
        mutableStateOf(preferenceSettingsStorage.getSettings().toSettingsState())
    }

    private fun onSettingsStateChanged(newSettingsUiState: SettingsUiState) {
        preferenceSettingsStorage.saveSettings(newSettingsUiState.toSettingsData())
    }

    fun onShowPlayerNameSettingsChange(shouldShowPlayerName: Boolean) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(showPlayerName = shouldShowPlayerName)
        )
        refreshUiState()
    }

    fun onShowPlayerRatingSettingsChange(shouldShowPlayerRating: Boolean) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(showPlayerRating = shouldShowPlayerRating)
        )
        refreshUiState()
    }

    fun onShowBoardCoordinateSettingsChange(shouldShowBoardCoordinate: Boolean) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings()
                .copy(showBoardCoordinates = shouldShowBoardCoordinate)
        )
        refreshUiState()
    }

    fun onMoveDelaySettingsChange(moveDelay: Float) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings()
                .copy(moveDelay = moveDelay)
        )
        refreshUiState()
    }

    private fun refreshUiState() {
        settingsUIState.value = preferenceSettingsStorage.getSettings().toSettingsState()
    }
}

fun SettingsUiState.toSettingsData(): SettingsData {
    return SettingsData(
        moveDelay = moveDelay,
        showPlayerRating = showPlayerRating,
        showBoardCoordinates = showBoardCoordinates,
        showPlayerName = showPlayerName
    )
}

fun SettingsData.toSettingsState(): SettingsUiState {
    return SettingsUiState(
        moveDelay = moveDelay,
        showPlayerRating = showPlayerRating,
        showBoardCoordinates = showBoardCoordinates,
        showPlayerName = showPlayerName
    )
}