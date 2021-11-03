package com.example.pgntogifconverter.resource

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.pgntogifconverter.R
import com.github.bhlangonijr.chesslib.Piece

class ChessPieceResource(val context: Context) {
    fun getDrawableFromChessPiece(chessPiece: Piece): Drawable? {
        return pieceToDrawableMap[chessPiece]?.let { ContextCompat.getDrawable(context, it) }
    }

    companion object {
        val pieceToDrawableMap = mapOf(
            Pair(Piece.BLACK_BISHOP, R.drawable.ic_bb),
            Pair(Piece.BLACK_KING, R.drawable.ic_bk),
            Pair(Piece.BLACK_KNIGHT, R.drawable.ic_bn),
            Pair(Piece.BLACK_PAWN, R.drawable.ic_bp),
            Pair(Piece.BLACK_QUEEN, R.drawable.ic_bq),
            Pair(Piece.BLACK_ROOK, R.drawable.ic_br),

            Pair(Piece.WHITE_BISHOP, R.drawable.ic_wb),
            Pair(Piece.WHITE_KING, R.drawable.ic_wk),
            Pair(Piece.WHITE_KNIGHT, R.drawable.ic_wn),
            Pair(Piece.WHITE_PAWN, R.drawable.ic_wp),
            Pair(Piece.WHITE_QUEEN, R.drawable.ic_wq),
            Pair(Piece.WHITE_ROOK, R.drawable.ic_wr),
            Pair(Piece.NONE, null)
        )
    }
}