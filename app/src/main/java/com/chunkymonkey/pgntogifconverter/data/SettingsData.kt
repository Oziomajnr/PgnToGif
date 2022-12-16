package com.chunkymonkey.pgntogifconverter.data

data class SettingsData(
    val showPlayerName: Boolean,
    val showPlayerRating: Boolean,
    val showBoardCoordinates: Boolean,
    val moveDelay: Float,
    val shouldFlipBoard: Boolean,
    val lastMoveDelay: Float
)
