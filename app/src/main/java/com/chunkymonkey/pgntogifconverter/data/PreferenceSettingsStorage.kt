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
    }

    override fun getSettings(): SettingsData {
        val shouldShowPlayerName = preferenceService.getBoolean(showPlayerNameKey, true)
        val shouldShowPlayerRating = preferenceService.getBoolean(showPlayerRatingKey, true)
        val shouldShowBoardCoordinates =
            preferenceService.getBoolean(showBoardCoordinatesKey, false)
        val moveDelay = preferenceService.getFloat(moveDelayKey, 0.5F)
        val lastMoveDelay = preferenceService.getFloat(lastMoveDelay, 1F)
        val flipBoard = preferenceService.getBoolean(flipBoardKey, false)
        val pieceSet = preferenceService.getString(pieceSetKey, "")
        return SettingsData(
            showPlayerName = shouldShowPlayerName,
            showBoardCoordinates = shouldShowBoardCoordinates,
            showPlayerRating = shouldShowPlayerRating,
            moveDelay = moveDelay,
            shouldFlipBoard = flipBoard,
            lastMoveDelay = lastMoveDelay,
            pieceSet = convertPrefToPieceSet(pieceSet)
        )
    }

    private fun convertPrefToPieceSet(pieceSetPreference: String): PieceSet {
        return when (pieceSetPreference) {
            PieceSet.Pirouetti.name -> PieceSet.Pirouetti
            PieceSet.California.name -> PieceSet.California
            PieceSet.Spatial.name -> PieceSet.Spatial
            PieceSet.Letter.name -> PieceSet.Letter
            else -> PieceSet.Default
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
    }
}