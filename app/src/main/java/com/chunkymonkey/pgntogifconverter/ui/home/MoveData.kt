package com.chunkymonkey.pgntogifconverter.ui.home

import com.github.bhlangonijr.chesslib.move.Move

data class MoveData(
    val index: Int,
    val san: String,
    val move: Move,
    val isWhite: Boolean
) {
    val moveNumber: Int get() = (index / 2) + 1
}
