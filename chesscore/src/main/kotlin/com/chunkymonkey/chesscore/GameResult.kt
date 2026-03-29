package com.chunkymonkey.chesscore

enum class GameResult(val description: String) {
    WHITE_WON("1-0"),
    BLACK_WON("0-1"),
    DRAW("1/2-1/2"),
    ONGOING("*");

    companion object {
        private val notationMap = entries.associateBy { it.description }

        fun fromNotation(notation: String): GameResult =
            notationMap[notation.trim()] ?: ONGOING
    }
}
