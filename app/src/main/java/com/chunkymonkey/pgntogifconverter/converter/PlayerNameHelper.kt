package com.chunkymonkey.pgntogifconverter.converter

import com.chunkymonkey.pgntogifconverter.data.SettingsData
import com.chunkymonkey.chesscore.ParsedGame

interface PlayerNameHelper {
    fun shouldShowPlayerName(game: ParsedGame, settingsData: SettingsData): Boolean
    fun getBlackPlayerName(game: ParsedGame, settingsData: SettingsData): String
    fun getWhitePlayerName(game: ParsedGame, settingsData: SettingsData): String
}

class DefaultPlayerNameHelper : PlayerNameHelper {
    override fun shouldShowPlayerName(game: ParsedGame, settingsData: SettingsData): Boolean {
        return settingsData.showPlayerName
                && game.blackPlayer != "?"
                && game.whitePlayer != "?"
    }

    override fun getBlackPlayerName(game: ParsedGame, settingsData: SettingsData): String {
        return if (settingsData.showPlayerRating && game.blackElo != 0) {
            "${game.blackPlayer} (${game.blackElo})"
        } else {
            game.blackPlayer
        }
    }

    override fun getWhitePlayerName(game: ParsedGame, settingsData: SettingsData): String {
        return if (settingsData.showPlayerRating && game.whiteElo != 0) {
            "${game.whitePlayer} (${game.whiteElo})"
        } else {
            game.whitePlayer
        }
    }

}