package com.chunkymonkey.pgntogifconverter.resource

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.chunkymonkey.pgntogifconverter.data.PieceSet
import com.chunkymonkey.pgntogifconverter.data.PreferenceSettingsStorage
import com.chunkymonkey.pgntogifconverter.data.SettingsStorage
import com.github.bhlangonijr.chesslib.Piece

class ChessPieceResourceProvider(
    val context: Context, private val settingsStorage: SettingsStorage
) {
    fun getDrawableFromChessPiece(chessPiece: Piece): Drawable? {

        return pieceToDrawableMap(
            context, settingsStorage.getSettings().pieceSet
        )[chessPiece]?.let {
            ContextCompat.getDrawable(
                context, it
            )
        }
    }

    companion object {
        fun pieceToDrawableMap(context: Context, pieceSet: PieceSet) = when (pieceSet) {
            PieceSet.Default -> mapOf(
                Pair(
                    Piece.BLACK_BISHOP, getDrawableResourceId(context, "ic_bb")
                ),
                Pair(Piece.BLACK_KING, getDrawableResourceId(context, "ic_bk")),
                Pair(Piece.BLACK_KNIGHT, getDrawableResourceId(context, "ic_bn")),
                Pair(Piece.BLACK_PAWN, getDrawableResourceId(context, "ic_bp")),
                Pair(Piece.BLACK_QUEEN, getDrawableResourceId(context, "ic_bq")),
                Pair(Piece.BLACK_ROOK, getDrawableResourceId(context, "ic_br")),

                Pair(Piece.WHITE_BISHOP, getDrawableResourceId(context, "ic_wb")),
                Pair(Piece.WHITE_KING, getDrawableResourceId(context, "ic_wk")),
                Pair(Piece.WHITE_KNIGHT, getDrawableResourceId(context, "ic_wn")),
                Pair(Piece.WHITE_PAWN, getDrawableResourceId(context, "ic_wp")),
                Pair(Piece.WHITE_QUEEN, getDrawableResourceId(context, "ic_wq")),
                Pair(Piece.WHITE_ROOK, getDrawableResourceId(context, "ic_wr")),
                Pair(Piece.NONE, null)
            )

            else -> mapOf(
                Pair(
                    Piece.BLACK_BISHOP, getDrawableResourceId(context, "ic_${pieceSet.name}_bb")
                ), Pair(
                    Piece.BLACK_KING, getDrawableResourceId(context, "ic_${pieceSet.name}_bk")
                ), Pair(
                    Piece.BLACK_KNIGHT, getDrawableResourceId(context, "ic_${pieceSet.name}_bn")
                ), Pair(
                    Piece.BLACK_PAWN, getDrawableResourceId(context, "ic_${pieceSet.name}_bp")
                ), Pair(
                    Piece.BLACK_QUEEN, getDrawableResourceId(context, "ic_${pieceSet.name}_bq")
                ), Pair(
                    Piece.BLACK_ROOK, getDrawableResourceId(context, "ic_${pieceSet.name}_br")
                ),

                Pair(
                    Piece.WHITE_BISHOP, getDrawableResourceId(context, "ic_${pieceSet.name}_wb")
                ), Pair(
                    Piece.WHITE_KING, getDrawableResourceId(context, "ic_${pieceSet.name}_wk")
                ), Pair(
                    Piece.WHITE_KNIGHT, getDrawableResourceId(context, "ic_${pieceSet.name}_wn")
                ), Pair(
                    Piece.WHITE_PAWN, getDrawableResourceId(context, "ic_${pieceSet.name}_wp")
                ), Pair(
                    Piece.WHITE_QUEEN, getDrawableResourceId(context, "ic_${pieceSet.name}_wq")
                ), Pair(
                    Piece.WHITE_ROOK, getDrawableResourceId(context, "ic_${pieceSet.name}_wr")
                ), Pair(Piece.NONE, null)
            )

        }


        @SuppressLint("DiscouragedApi")
        private fun getDrawableResourceId(context: Context, resourceName: String): Int {
            return context.resources.getIdentifier(
                resourceName, "drawable", context.packageName
            )
        }

    }
}

