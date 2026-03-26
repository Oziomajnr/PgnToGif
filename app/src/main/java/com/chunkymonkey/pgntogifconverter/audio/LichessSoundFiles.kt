package com.chunkymonkey.pgntogifconverter.audio

import android.content.Context
import android.content.res.AssetManager

/**
 * Bundled Lichess (lila) sound assets — see [ASSET_PREFIX]/README.txt for license/source.
 */
object LichessSoundFiles {

    const val ASSET_PREFIX = "lichess_sounds"

    fun assetPathFor(type: MoveSoundType): String = "$ASSET_PREFIX/${fileName(type)}"

    private fun fileName(type: MoveSoundType): String = when (type) {
        MoveSoundType.Move -> "Move.mp3"
        MoveSoundType.Capture -> "Capture.mp3"
        MoveSoundType.Check -> "Check.mp3"
        MoveSoundType.Castle -> "Castles.mp3"
    }

    const val VICTORY = "$ASSET_PREFIX/Victory.mp3"

    fun hasBundledSet(assets: AssetManager): Boolean {
        val names = assets.list(ASSET_PREFIX)?.toSet() ?: return false
        return listOf("Move.mp3", "Capture.mp3", "Check.mp3", "Castles.mp3").all { it in names }
    }

    fun hasBundledSet(context: Context): Boolean = hasBundledSet(context.assets)
}
