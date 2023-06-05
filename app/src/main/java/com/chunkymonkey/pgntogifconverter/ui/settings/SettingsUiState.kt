package com.chunkymonkey.pgntogifconverter.ui.settings

import com.chunkymonkey.pgntogifconverter.data.BoardStyle
import com.chunkymonkey.pgntogifconverter.data.PieceSet

data class SettingsUiState(
    val showPlayerName: Boolean,
    val showPlayerRating: Boolean,
    val showBoardCoordinates: Boolean,
    val moveDelay: Float,
    val flipBoard: Boolean,
    val lastMoveDelay: Float,
    val pieceSet: PieceSet,
    val boardStyle: BoardStyle
)
