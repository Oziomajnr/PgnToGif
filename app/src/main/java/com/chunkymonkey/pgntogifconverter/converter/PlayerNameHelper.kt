package com.chunkymonkey.pgntogifconverter.converter

import com.chunkymonkey.pgntogifconverter.data.SettingsData
import com.github.bhlangonijr.chesslib.game.Game

interface PlayerNameHelper {
    fun shouldShowPlayerName(game: Game, settingsData: SettingsData): Boolean
    fun getBlackPlayerName(game: Game, settingsData: SettingsData): String
    fun getWhitePlayerName(game: Game, settingsData: SettingsData): String
}

class DefaultPlayerNameHelper : PlayerNameHelper {
    override fun shouldShowPlayerName(game: Game, settingsData: SettingsData): Boolean {
        return settingsData.showPlayerName
                && game.blackPlayer.name != null
                && game.whitePlayer.name != null
    }

    override fun getBlackPlayerName(game: Game, settingsData: SettingsData): String {
        return if (settingsData.showPlayerRating && game.blackPlayer.elo != 0) {
            "${game.blackPlayer.name} (${game.blackPlayer.elo})"
        } else {
            game.blackPlayer.name
        }
    }

    override fun getWhitePlayerName(game: Game, settingsData: SettingsData): String {
        return if (settingsData.showPlayerRating && game.whitePlayer.elo != 0) {
            "${game.whitePlayer.name} (${game.whitePlayer.elo})"
        } else {
            game.whitePlayer.name
        }
    }

}