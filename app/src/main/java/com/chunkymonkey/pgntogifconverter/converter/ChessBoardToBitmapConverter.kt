package com.chunkymonkey.pgntogifconverter.converter


import com.chunkymonkey.pgntogifconverter.resource.ChessPieceResource
import com.chunkymonkey.pgntogifconverter.resource.PaintResource
import com.chunkymonkey.pgntogifconverter.util.getCoordinateFromSquare

import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.Board

import androidx.core.graphics.drawable.toBitmap

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.chunkymonkey.pgntogifconverter.util.getCoordinateFromSquareWithFlippedBoard

class ChessBoardToBitmapConverter(
    private val paintResource: PaintResource,
    private val chessPieceResource: ChessPieceResource
) {
    private val boardSize = 505
    private val sizePerSquare = boardSize / 8

    fun createBitmapFromChessBoard(
        chessBoard: Board,
        currentMove: Move,
        shouldFlipBoard: Boolean
    ): Bitmap =
        getGameBoardBitmap(chessBoard, currentMove, shouldFlipBoard)


    private fun getGameBoardBitmap(
        chessBoard: Board,
        currentMove: Move,
        shouldFlipBoard: Boolean
    ): Bitmap {
        val boardArray = chessBoard.boardToArray()

        var currentX = 0f
        var currentY = (sizePerSquare * 7).toFloat()

        val conf = Bitmap.Config.ARGB_8888 // see other conf types

        val finalBitmap =
            Bitmap.createBitmap(boardSize, boardSize, conf) // this creates a MUTABLE bitmap

        val canvas = Canvas(finalBitmap)


        for (x in 0..7) {
            for (y in 0..7) {
                val squarePaint = getPaintFromBoardCoordinate(x, y)
                canvas.drawRect(
                    currentX,
                    currentY,
                    currentX + sizePerSquare,
                    currentY + sizePerSquare, squarePaint
                )

                val pieceIndex = if (shouldFlipBoard) {
                    getBoardIndexFromBoardCoordinateBlackPOV(x, y)
                } else {
                    getBoardIndexFromBoardCoordinate(x, y)
                }
                val currentPiece = boardArray[pieceIndex]
                if (chessBoard.isKingAttacked && ((currentPiece == Piece.BLACK_KING && chessBoard.sideToMove == Side.BLACK)
                            || (currentPiece == Piece.WHITE_KING && chessBoard.sideToMove == Side.WHITE))
                ) {
                    canvas.drawRect(
                        currentX,
                        currentY,
                        currentX + sizePerSquare,
                        currentY + sizePerSquare, paintResource.kingAttackedPaint
                    )
                }
                val coordinateFrom = if (shouldFlipBoard) {
                    getCoordinateFromSquareWithFlippedBoard(currentMove.from)
                } else {
                    getCoordinateFromSquare(currentMove.from)
                }
                val coordinateTo = if (shouldFlipBoard) {
                    getCoordinateFromSquareWithFlippedBoard(currentMove.to)
                } else {
                    getCoordinateFromSquare(currentMove.to)
                }
                if (coordinateFrom.first == y && coordinateFrom.second == x) {
                    canvas.drawRect(
                        currentX,
                        currentY,
                        currentX + sizePerSquare,
                        currentY + sizePerSquare, paintResource.highlightedSquarePaint
                    )
                }
                if (coordinateTo.first == y && coordinateTo.second == x) {
                    canvas.drawRect(
                        currentX,
                        currentY,
                        currentX + sizePerSquare,
                        currentY + sizePerSquare, paintResource.highlightedSquarePaint
                    )
                }
                val pieceDrawable = chessPieceResource.getDrawableFromChessPiece(currentPiece)

                if (pieceDrawable != null) {
                    val bitmap =
                        pieceDrawable.toBitmap(
                            sizePerSquare,
                            sizePerSquare
                        )
                    bitmap.let {
                        canvas.drawBitmap(
                            it, currentX,
                            currentY,
                            null
                        )
                    }
                }
                currentX += sizePerSquare
            }
            currentX = 0f
            currentY -= sizePerSquare
        }
        return finalBitmap
    }

    private fun getBoardIndexFromBoardCoordinate(x: Int, y: Int): Int {
        return x * 8 + y
    }

    private fun getBoardIndexFromBoardCoordinateBlackPOV(x: Int, y: Int): Int {
        return 63 - y - (x * 8)
    }

    private fun getPaintFromBoardCoordinate(x: Int, y: Int): Paint {
        val xIsOddNumber = x % 2 != 0
        val yIsOddNumber = y % 2 != 0
        return if (xIsOddNumber && yIsOddNumber || !xIsOddNumber && !yIsOddNumber) {
            paintResource.blackSquarePaint
        } else {
            paintResource.whiteSquarePaint
        }
    }
}