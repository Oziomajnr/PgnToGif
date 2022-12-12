package com.chunkymonkey.pgntogifconverter.ui.settings

data class SettingsUiState(
    val showPlayerName: Boolean,
    val showPlayerRating: Boolean,
    val showBoardCoordinates: Boolean,
    val moveDelay: Float,
    val flipBoard: Boolean,
    val lastMoveDelay: Float
)
