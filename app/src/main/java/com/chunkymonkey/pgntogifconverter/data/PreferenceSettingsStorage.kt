package com.chunkymonkey.pgntogifconverter.data

import com.chunkymonkey.pgntogifconverter.preference.PreferenceService

class PreferenceSettingsStorage(private val preferenceService: PreferenceService) :
    SettingsStorage {

    override fun saveSettings(settingsData: SettingsData) {
        preferenceService.saveData(showPlayerNameKey, settingsData.showPlayerName)
        preferenceService.saveData(showPlayerRatingKey, settingsData.showPlayerRating)
        preferenceService.saveData(showBoardCoordinatesKey, settingsData.showBoardCoordinates)
        preferenceService.saveData(moveDelayKey, settingsData.moveDelay)
    }

    override fun getSettings(): SettingsData {
        val shouldShowPlayerName = preferenceService.getBoolean(showPlayerNameKey, true)
        val shouldShowPlayerRating = preferenceService.getBoolean(showPlayerRatingKey, true)
        val shouldShowBoardCoordinates =
            preferenceService.getBoolean(showBoardCoordinatesKey, false)
        val moveDelay = preferenceService.getFloat(moveDelayKey, 0.5F)
        return SettingsData(
            showPlayerName = shouldShowPlayerName,
            showBoardCoordinates = shouldShowBoardCoordinates,
            showPlayerRating = shouldShowPlayerRating,
            moveDelay = moveDelay
        )
    }

    companion object {
        private const val showPlayerNameKey = "SHOW_PLAYER_NAME_KEY"
        private const val showPlayerRatingKey = "SHOW_PLAYER_RATING_KEY"
        private const val showBoardCoordinatesKey = "SHOW_BOARD_COORDINATES_KEY"
        private const val moveDelayKey = "MOVE_DELAY_KEY"
    }
}