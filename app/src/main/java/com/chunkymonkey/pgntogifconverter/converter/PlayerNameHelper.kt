package com.chunkymonkey.pgntogifconverter.converter

import com.chunkymonkey.pgntogifconverter.data.SettingsData
import com.github.bhlangonijr.chesslib.game.Game

interface PlayerNameHelper {
    fun shouldShowPlayerName(game: Game, settingsData: SettingsData): Boolean
}

class DefaultPlayerNameHelper: PlayerNameHelper{
    override fun shouldShowPlayerName(game: Game, settingsData: SettingsData): Boolean {
        return settingsData.showPlayerName
                && game.blackPlayer.name != null
                && game.whitePlayer.name != null
    }

}