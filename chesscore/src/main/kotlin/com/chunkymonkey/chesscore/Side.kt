package com.chunkymonkey.chesscore

enum class Side {
    WHITE,
    BLACK;

    fun flip(): Side = if (this == WHITE) BLACK else WHITE

    companion object {
        val allSides: Array<Side> = values()
    }
}
