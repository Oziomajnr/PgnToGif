package com.chunkymonkey.chesslibrarywrapper.board

import com.chunkymonkey.chesslibrarywrapper.board.side.Side
import com.github.bhlangonijr.chesslib.Piece

interface ChessBoard {
    fun boardToArray(): Array<Piece>
    fun isKingAttacked(): Boolean
    fun sideToMove(): Side
}