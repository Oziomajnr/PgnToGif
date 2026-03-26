package com.chunkymonkey.pgntogifconverter.resource

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.chunkymonkey.pgntogifconverter.data.PieceSet
import com.chunkymonkey.pgntogifconverter.data.SettingsStorage
import com.chunkymonkey.pgntogifconverter.lichess.LichessPieceDownloader
import com.chunkymonkey.pgntogifconverter.lichess.LichessPieceSvgLoader
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side
import java.io.File

class ChessPieceResourceProvider(
    val context: Context, private val settingsStorage: SettingsStorage
) {
    /**
     * Returns a map of Piece → Bitmap pre-rasterized at [sizePerSquare].
     * Built once per generation call; NONE maps to null.
     */
    fun buildPieceBitmapCache(sizePerSquare: Int): Map<Piece, Bitmap?> {
        val pieceSet = settingsStorage.getSettings().pieceSet
        return when (pieceSet) {
            is PieceSet.Lichess -> buildLichessPieceBitmapCache(pieceSet.familyId, sizePerSquare)
            else -> pieceToDrawableMap(context, pieceSet).mapValues { (_, resId) ->
                resId?.let {
                    ContextCompat.getDrawable(context, it)?.toBitmap(sizePerSquare, sizePerSquare)
                }
            }
        }
    }

    private fun buildLichessPieceBitmapCache(familyId: String, sizePerSquare: Int): Map<Piece, Bitmap?> {
        val downloader = LichessPieceDownloader(context)
        if (!downloader.isPieceFamilyInstalled(familyId)) {
            return pieceToDrawableMap(context, PieceSet.Default).mapValues { (_, resId) ->
                resId?.let {
                    ContextCompat.getDrawable(context, it)?.toBitmap(sizePerSquare, sizePerSquare)
                }
            }
        }
        val dir = downloader.pieceFamilyDir(familyId)
        val result = mutableMapOf<Piece, Bitmap?>()
        for (p in Piece.values()) {
            if (p == Piece.NONE) {
                result[p] = null
                continue
            }
            val base = pieceToSvgBaseName(p)
            val file = File(dir, "$base.svg")
            result[p] = LichessPieceSvgLoader.svgFileToBitmap(file, sizePerSquare)
        }
        return result
    }

    private fun pieceToSvgBaseName(piece: Piece): String {
        val prefix = if (piece.pieceSide == Side.WHITE) "w" else "b"
        val letter = when (piece.pieceType) {
            PieceType.PAWN -> "P"
            PieceType.KNIGHT -> "N"
            PieceType.BISHOP -> "B"
            PieceType.ROOK -> "R"
            PieceType.QUEEN -> "Q"
            PieceType.KING -> "K"
            else -> "P"
        }
        return "$prefix$letter"
    }

    fun getDrawableFromChessPiece(chessPiece: Piece): Drawable? {
        val pieceSet = settingsStorage.getSettings().pieceSet
        if (pieceSet is PieceSet.Lichess) return null
        return pieceToDrawableMap(
            context, pieceSet
        )[chessPiece]?.let {
            ContextCompat.getDrawable(context, it)
        }
    }

    companion object {
        fun pieceToDrawableMap(context: Context, pieceSet: PieceSet) = when (pieceSet) {
            is PieceSet.Lichess -> emptyMap()
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
