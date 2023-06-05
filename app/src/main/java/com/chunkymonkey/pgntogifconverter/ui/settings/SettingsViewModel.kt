package com.chunkymonkey.pgntogifconverter.ui.settings

import androidx.compose.runtime.mutableStateOf
import com.chunkymonkey.pgntogifconverter.analytics.AnalyticsEvent
import com.chunkymonkey.pgntogifconverter.analytics.AnalyticsEventHandler
import com.chunkymonkey.pgntogifconverter.data.BoardStyle
import com.chunkymonkey.pgntogifconverter.data.PieceSet
import com.chunkymonkey.pgntogifconverter.data.SettingsData
import com.chunkymonkey.pgntogifconverter.data.SettingsStorage
import com.chunkymonkey.pgntogifconverter.dependency.DependencyFactory

class SettingsViewModel {
    private val analyticsEventHandler: AnalyticsEventHandler =
        DependencyFactory.getAnalyticsEventHandler()
    private val preferenceSettingsStorage: SettingsStorage = DependencyFactory.getSettingsStorage()

    val settingsUIState by lazy {
        mutableStateOf(preferenceSettingsStorage.getSettings().toSettingsState())
    }

    fun onShowPlayerNameSettingsChange(shouldShowPlayerName: Boolean) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(showPlayerName = shouldShowPlayerName)
        )
        analyticsEventHandler.logEvent(AnalyticsEvent.SettingsShowPlayerNameClicked)
        refreshUiState()
    }

    fun onShowPlayerRatingSettingsChange(shouldShowPlayerRating: Boolean) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(showPlayerRating = shouldShowPlayerRating)
        )
        analyticsEventHandler.logEvent(AnalyticsEvent.SettingsShowPlayerRatingClicked)
        refreshUiState()
    }

    fun onFlipBoardSettingsChange(shouldFlipBoard: Boolean) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(shouldFlipBoard = shouldFlipBoard)
        )
        analyticsEventHandler.logEvent(AnalyticsEvent.SettingsFlipBoardClicked)
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
            preferenceSettingsStorage.getSettings().copy(moveDelay = moveDelay)
        )
        analyticsEventHandler.logEvent(AnalyticsEvent.MoveDelaySliderClicked)
        refreshUiState()
    }

    fun onLastMoveDelaySettingsChanged(lastMoveDelay: Float) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(lastMoveDelay = lastMoveDelay)
        )
        analyticsEventHandler.logEvent(AnalyticsEvent.LastMoveDelaySliderClicked)
        refreshUiState()
    }

    fun settingsPieceSetClicked() {
        analyticsEventHandler.logEvent(AnalyticsEvent.SettingsPieceSetClicked)
    }

    fun settingsBoardStyleClicked() {
        analyticsEventHandler.logEvent(AnalyticsEvent.SettingsBoardStyleClicked)
    }

    fun onNewPieceSetSelected(selectedPieceSet: PieceSet) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(pieceSet = selectedPieceSet)
        )
        analyticsEventHandler.logEvent(AnalyticsEvent.OnNewPieceSetSelected(selectedPieceSet.name))
    }

    fun onNewBoardStyleSelected(boardStyle: BoardStyle) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(boardStyle = boardStyle)
        )
        analyticsEventHandler.logEvent(AnalyticsEvent.OnNewBoardStyleSelected(boardStyle.name))
    }

    private fun refreshUiState() {
        settingsUIState.value = preferenceSettingsStorage.getSettings().toSettingsState()
    }
}

fun SettingsData.toSettingsState(): SettingsUiState {
    return SettingsUiState(
        moveDelay = moveDelay,
        showPlayerRating = showPlayerRating,
        showBoardCoordinates = showBoardCoordinates,
        showPlayerName = showPlayerName,
        flipBoard = shouldFlipBoard,
        lastMoveDelay = lastMoveDelay,
        pieceSet = pieceSet,
        boardStyle = boardStyle
    )
}