package com.chunkymonkey.pgntogifconverter.ui.settings

import androidx.compose.runtime.mutableStateOf
import com.chunkymonkey.pgntogifconverter.analytics.AnalyticsEvent
import com.chunkymonkey.pgntogifconverter.analytics.AnalyticsEventHandler
import com.chunkymonkey.pgntogifconverter.data.BoardStyle
import com.chunkymonkey.pgntogifconverter.data.HighlightStyle
import com.chunkymonkey.pgntogifconverter.data.PieceSet
import com.chunkymonkey.pgntogifconverter.data.SettingsData
import com.chunkymonkey.pgntogifconverter.data.SettingsStorage
import com.chunkymonkey.pgntogifconverter.dependency.DependencyFactory
import com.chunkymonkey.pgntogifconverter.lichess.LichessBoardThemeInstaller
import com.chunkymonkey.pgntogifconverter.lichess.LichessPieceDownloader

class SettingsViewModel {
    private val analyticsEventHandler: AnalyticsEventHandler =
        DependencyFactory.getAnalyticsEventHandler()
    private val preferenceSettingsStorage: SettingsStorage = DependencyFactory.getSettingsStorage()
    private val lichessPieceDownloader = LichessPieceDownloader(DependencyFactory.getApplicationContext())
    private val lichessBoardInstaller = LichessBoardThemeInstaller(DependencyFactory.getApplicationContext())

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
        refreshUiState()
    }

    fun onNewBoardStyleSelected(boardStyle: BoardStyle) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(boardStyle = boardStyle)
        )
        analyticsEventHandler.logEvent(AnalyticsEvent.OnNewBoardStyleSelected(boardStyle.name))
        refreshUiState()
    }

    fun onHighlightStyleSelected(highlightStyle: HighlightStyle) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(highlightStyle = highlightStyle)
        )
        refreshUiState()
    }

    fun onGifQualityChanged(quality: Int) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(gifQuality = quality)
        )
        refreshUiState()
    }

    fun onGifLoopCountChanged(loopCount: Int) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(gifLoopCount = loopCount)
        )
        refreshUiState()
    }

    fun onBoardResolutionChanged(resolution: Int) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(boardResolution = resolution)
        )
        refreshUiState()
    }

    fun onShowGameResultChanged(show: Boolean) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(showGameResult = show)
        )
        refreshUiState()
    }

    fun onMp4AudioEnabledChanged(enabled: Boolean) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(mp4AudioEnabled = enabled)
        )
        refreshUiState()
    }

    fun onMp4SoundMoveChanged(v: Boolean) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(mp4SoundMove = v)
        )
        refreshUiState()
    }

    fun onMp4SoundCaptureChanged(v: Boolean) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(mp4SoundCapture = v)
        )
        refreshUiState()
    }

    fun onMp4SoundCheckChanged(v: Boolean) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(mp4SoundCheck = v)
        )
        refreshUiState()
    }

    fun onMp4SoundCastleChanged(v: Boolean) {
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(mp4SoundCastle = v)
        )
        refreshUiState()
    }


    suspend fun downloadLichessPieceFamily(familyId: String): Result<Unit> =
        lichessPieceDownloader.downloadPieceFamily(familyId)

    suspend fun installLichessBoardTheme(themeId: String): Result<Unit> =
        lichessBoardInstaller.installBoardTheme(themeId)

    fun selectLichessPieceFamily(familyId: String) {
        val pieceSet = PieceSet.Lichess(familyId)
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(pieceSet = pieceSet)
        )
        analyticsEventHandler.logEvent(AnalyticsEvent.OnNewPieceSetSelected(pieceSet.name))
        refreshUiState()
    }

    fun selectLichessBoardTheme(themeId: String) {
        val boardStyle = BoardStyle.Lichess(themeId)
        preferenceSettingsStorage.saveSettings(
            preferenceSettingsStorage.getSettings().copy(boardStyle = boardStyle)
        )
        analyticsEventHandler.logEvent(AnalyticsEvent.OnNewBoardStyleSelected(boardStyle.name))
        refreshUiState()
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
        boardStyle = boardStyle,
        highlightStyle = highlightStyle,
        gifQuality = gifQuality,
        gifLoopCount = gifLoopCount,
        boardResolution = boardResolution,
        showGameResult = showGameResult,
        mp4AudioEnabled = mp4AudioEnabled,
        mp4SoundMove = mp4SoundMove,
        mp4SoundCapture = mp4SoundCapture,
        mp4SoundCheck = mp4SoundCheck,
        mp4SoundCastle = mp4SoundCastle,
    )
}
