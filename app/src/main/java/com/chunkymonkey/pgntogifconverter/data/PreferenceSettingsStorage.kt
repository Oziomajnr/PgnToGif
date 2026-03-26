package com.chunkymonkey.pgntogifconverter.data

import com.chunkymonkey.pgntogifconverter.preference.PreferenceService

class PreferenceSettingsStorage(private val preferenceService: PreferenceService) :
    SettingsStorage {

    override fun saveSettings(settingsData: SettingsData) {
        preferenceService.saveData(showPlayerNameKey, settingsData.showPlayerName)
        preferenceService.saveData(showPlayerRatingKey, settingsData.showPlayerRating)
        preferenceService.saveData(showBoardCoordinatesKey, settingsData.showBoardCoordinates)
        preferenceService.saveData(moveDelayKey, settingsData.moveDelay)
        preferenceService.saveData(flipBoardKey, settingsData.shouldFlipBoard)
        preferenceService.saveData(lastMoveDelay, settingsData.lastMoveDelay)
        preferenceService.saveData(pieceSetKey, settingsData.pieceSet.name)
        preferenceService.saveData(boardStyleKey, settingsData.boardStyle.name)
        val highlightValue = when (val hs = settingsData.highlightStyle) {
            is HighlightStyle.Custom -> "custom:${hs.argb}"
            else -> hs.name
        }
        preferenceService.saveData(highlightStyleKey, highlightValue)
        preferenceService.saveData(gifQualityKey, settingsData.gifQuality)
        preferenceService.saveData(gifLoopCountKey, settingsData.gifLoopCount)
        preferenceService.saveData(boardResolutionKey, settingsData.boardResolution)
        preferenceService.saveData(showGameResultKey, settingsData.showGameResult)
        preferenceService.saveData(startFromMoveKey, settingsData.startFromMoveIndex)
        preferenceService.saveData(mp4AudioEnabledKey, settingsData.mp4AudioEnabled)
        preferenceService.saveData(mp4SoundMoveKey, settingsData.mp4SoundMove)
        preferenceService.saveData(mp4SoundCaptureKey, settingsData.mp4SoundCapture)
        preferenceService.saveData(mp4SoundCheckKey, settingsData.mp4SoundCheck)
        preferenceService.saveData(mp4SoundCastleKey, settingsData.mp4SoundCastle)
    }

    override fun getSettings(): SettingsData {
        val shouldShowPlayerName = preferenceService.getBoolean(showPlayerNameKey, true)
        val shouldShowPlayerRating = preferenceService.getBoolean(showPlayerRatingKey, true)
        val shouldShowBoardCoordinates =
            preferenceService.getBoolean(showBoardCoordinatesKey, true)
        val moveDelay = preferenceService.getFloat(moveDelayKey, 0.5F)
        val lastMoveDelay = preferenceService.getFloat(lastMoveDelay, 1F)
        val flipBoard = preferenceService.getBoolean(flipBoardKey, false)
        val pieceSet = preferenceService.getString(pieceSetKey, "")
        val boardStyle = preferenceService.getString(boardStyleKey, "")
        val highlightStyle = preferenceService.getString(highlightStyleKey, "")
        val gifQuality = preferenceService.getInt(gifQualityKey, 10)
        val gifLoopCount = preferenceService.getInt(gifLoopCountKey, 0)
        val boardResolution = preferenceService.getInt(boardResolutionKey, 504)
        val showGameResult = preferenceService.getBoolean(showGameResultKey, true)
        val startFromMove = preferenceService.getInt(startFromMoveKey, 0)
        val mp4AudioEnabled = preferenceService.getBoolean(mp4AudioEnabledKey, true)
        val mp4SoundMove = preferenceService.getBoolean(mp4SoundMoveKey, true)
        val mp4SoundCapture = preferenceService.getBoolean(mp4SoundCaptureKey, true)
        val mp4SoundCheck = preferenceService.getBoolean(mp4SoundCheckKey, true)
        val mp4SoundCastle = preferenceService.getBoolean(mp4SoundCastleKey, true)
        return SettingsData(
            showPlayerName = shouldShowPlayerName,
            showBoardCoordinates = shouldShowBoardCoordinates,
            showPlayerRating = shouldShowPlayerRating,
            moveDelay = moveDelay,
            shouldFlipBoard = flipBoard,
            lastMoveDelay = lastMoveDelay,
            pieceSet = convertPrefToPieceSet(pieceSet),
            boardStyle = convertPrefToBoardStyle(boardStyle),
            highlightStyle = convertPrefToHighlightStyle(highlightStyle),
            gifQuality = gifQuality,
            gifLoopCount = gifLoopCount,
            boardResolution = boardResolution,
            showGameResult = showGameResult,
            startFromMoveIndex = startFromMove,
            mp4AudioEnabled = mp4AudioEnabled,
            mp4SoundMove = mp4SoundMove,
            mp4SoundCapture = mp4SoundCapture,
            mp4SoundCheck = mp4SoundCheck,
            mp4SoundCastle = mp4SoundCastle,
        )
    }

    private fun convertPrefToPieceSet(pieceSetPreference: String): PieceSet {
        if (pieceSetPreference.startsWith(LICHESS_PREFIX)) {
            val id = pieceSetPreference.removePrefix(LICHESS_PREFIX)
            if (id.isNotEmpty()) return PieceSet.Lichess(id)
        }
        return when (pieceSetPreference) {
            PieceSet.Pirouetti.name -> PieceSet.Pirouetti
            PieceSet.California.name -> PieceSet.California
            PieceSet.Spatial.name -> PieceSet.Spatial
            PieceSet.Letter.name -> PieceSet.Letter
            else -> PieceSet.Default
        }
    }

    private fun convertPrefToBoardStyle(pieceSetPreference: String): BoardStyle {
        if (pieceSetPreference.startsWith(LICHESS_PREFIX)) {
            val id = pieceSetPreference.removePrefix(LICHESS_PREFIX)
            if (id.isNotEmpty()) return BoardStyle.Lichess(id)
        }
        return when (pieceSetPreference) {
            BoardStyle.Default.name -> BoardStyle.Default
            BoardStyle.Blue.name -> BoardStyle.Blue
            BoardStyle.IC.name -> BoardStyle.IC
            BoardStyle.Purple.name -> BoardStyle.Purple
            BoardStyle.Green.name -> BoardStyle.Green
            BoardStyle.Maple.name -> BoardStyle.Maple
            BoardStyle.Wood.name -> BoardStyle.Wood
            BoardStyle.Canvas.name -> BoardStyle.Canvas
            BoardStyle.Metal.name -> BoardStyle.Metal
            else -> BoardStyle.Default
        }
    }

    private fun convertPrefToHighlightStyle(pref: String): HighlightStyle {
        if (pref.startsWith("custom:")) {
            val argb = pref.removePrefix("custom:").toIntOrNull()
            if (argb != null) return HighlightStyle.Custom(argb)
        }
        return when (pref) {
            HighlightStyle.Green.name -> HighlightStyle.Green
            HighlightStyle.Yellow.name -> HighlightStyle.Yellow
            HighlightStyle.Blue.name -> HighlightStyle.Blue
            HighlightStyle.Red.name -> HighlightStyle.Red
            HighlightStyle.Orange.name -> HighlightStyle.Orange
            else -> HighlightStyle.Green
        }
    }

    companion object {
        private const val showPlayerNameKey = "SHOW_PLAYER_NAME_KEY"
        private const val showPlayerRatingKey = "SHOW_PLAYER_RATING_KEY"
        private const val showBoardCoordinatesKey = "SHOW_BOARD_COORDINATES_KEY"
        private const val moveDelayKey = "MOVE_DELAY_KEY"
        private const val flipBoardKey = "FLIP_BOARD_KEY"
        private const val lastMoveDelay = "LAST_MOVE_DELAY"
        private const val pieceSetKey = "PIECE_SET"
        private const val boardStyleKey = "BOARD_STYLE"
        private const val highlightStyleKey = "HIGHLIGHT_STYLE"
        private const val gifQualityKey = "GIF_QUALITY"
        private const val gifLoopCountKey = "GIF_LOOP_COUNT"
        private const val boardResolutionKey = "BOARD_RESOLUTION"
        private const val showGameResultKey = "SHOW_GAME_RESULT"
        private const val startFromMoveKey = "START_FROM_MOVE"
        private const val mp4AudioEnabledKey = "MP4_AUDIO_ENABLED"
        private const val mp4SoundMoveKey = "MP4_SOUND_MOVE"
        private const val mp4SoundCaptureKey = "MP4_SOUND_CAPTURE"
        private const val mp4SoundCheckKey = "MP4_SOUND_CHECK"
        private const val mp4SoundCastleKey = "MP4_SOUND_CASTLE"
        private const val LICHESS_PREFIX = "lichess:"
    }
}
