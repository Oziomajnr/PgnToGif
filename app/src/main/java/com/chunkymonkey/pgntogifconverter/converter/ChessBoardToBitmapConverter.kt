package com.chunkymonkey.pgntogifconverter.converter


import com.chunkymonkey.pgntogifconverter.resource.ChessPieceResourceProvider
import com.chunkymonkey.pgntogifconverter.resource.PaintResourceProvider
import com.chunkymonkey.pgntogifconverter.util.getCoordinateFromSquare

import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.Board

import androidx.core.graphics.drawable.toBitmap

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.chunkymonkey.pgntogifconverter.util.ErrorHandler
import com.chunkymonkey.pgntogifconverter.util.getCoordinateFromSquareWithFlippedBoard
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class ChessBoardToBitmapConverter(
    private val paintResourceProvider: PaintResourceProvider,
    private val chessPieceResourceProvider: ChessPieceResourceProvider
) {
    private val boardSize = 505
    private val sizePerSquare = boardSize / 8
    private val conf = Bitmap.Config.ARGB_8888 // Bitmap configuration
    private var finalBitmap = Bitmap.createBitmap(boardSize, boardSize, conf) // Mutable bitmap
    private var canvas = Canvas(finalBitmap) // Canvas for drawing on the bitmap

    @OptIn(ExperimentalTime::class)
    fun createBitmapFromChessBoard(
        initialBoard: Board,
        finalBoard: Board,
        currentMove: Move,
        shouldFlipBoard: Boolean,
        isFirstBoard: Boolean,
        prevMove: Move?
    ): Bitmap {
        measureTimedValue {
            val movesFromPrev = (prevMove?.run {
                val (x1, y1) = getCoordinateFromSquare(this.from)
                val (x2, y2) = getCoordinateFromSquare(this.to)
                setOf(
                    getBoardIndexFromBoardCoordinate(y1, x1),
                    getBoardIndexFromBoardCoordinate(y2, x2)
                )
            } ?: emptySet())
            val squaresToRedraw =
                if (isFirstBoard) initialBoard.boardToArray().indices.toSet()
                    .toSet() else getSquaresToRedraw(initialBoard, finalBoard)
            getGameBoardBitmap(
                finalBoard,
                currentMove,
                shouldFlipBoard,
                squaresToRedraw + movesFromPrev
            )
        }.also {
            ErrorHandler.logInfo("createBitmapFromChessBoard took ${it.duration}")
            return it.value
        }
    }


    private fun getGameBoardBitmap(
        chessBoard: Board, currentMove: Move, shouldFlipBoard: Boolean, squaresToRedraw: Set<Int>
    ): Bitmap {
        val boardArray = chessBoard.boardToArray()

        var currentX = 0f
        var currentY = (sizePerSquare * 7).toFloat()



        for (x in 0..7) {
            for (y in 0..7) {
                val pieceIndex = if (shouldFlipBoard) {
                    getBoardIndexFromBoardCoordinateBlackPOV(x, y)
                } else {
                    getBoardIndexFromBoardCoordinate(x, y)
                }
                if (squaresToRedraw.contains(pieceIndex)) {
                    val squarePaint = getPaintFromBoardCoordinate(x, y)
                    canvas.drawRect(
                        currentX,
                        currentY,
                        currentX + sizePerSquare,
                        currentY + sizePerSquare,
                        squarePaint
                    )


                    val currentPiece = boardArray[pieceIndex]
                    if (chessBoard.isKingAttacked && ((currentPiece == Piece.BLACK_KING && chessBoard.sideToMove == Side.BLACK)
                                || (currentPiece == Piece.WHITE_KING && chessBoard.sideToMove == Side.WHITE))
                    ) {
                        canvas.drawRect(
                            currentX,
                            currentY,
                            currentX + sizePerSquare,
                            currentY + sizePerSquare, paintResourceProvider.kingAttackedPaint
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
                            currentY + sizePerSquare, paintResourceProvider.highlightedSquarePaint
                        )
                    }
                    if (coordinateTo.first == y && coordinateTo.second == x) {
                        canvas.drawRect(
                            currentX,
                            currentY,
                            currentX + sizePerSquare,
                            currentY + sizePerSquare, paintResourceProvider.highlightedSquarePaint
                        )
                    }
                    val pieceDrawable =
                        chessPieceResourceProvider.getDrawableFromChessPiece(currentPiece)

                    if (pieceDrawable != null) {
                        val bitmap = pieceDrawable.toBitmap(
                            sizePerSquare, sizePerSquare
                        )
                        bitmap.let {
                            canvas.drawBitmap(
                                it, currentX, currentY, null
                            )
                        }
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

    private fun getSquaresToRedraw(initialBoard: Board, finalBoard: Board): Set<Int> {
        val initialBoardArray = initialBoard.boardToArray()
        val finalBoardArray = finalBoard.boardToArray()
        return initialBoardArray.mapIndexedNotNull { index, piece ->
            if (finalBoardArray[index] != piece) index else null
        }.toSet()
    }

    private fun getPaintFromBoardCoordinate(x: Int, y: Int): Paint {
        val xIsOddNumber = x % 2 != 0
        val yIsOddNumber = y % 2 != 0
        return if (xIsOddNumber && yIsOddNumber || !xIsOddNumber && !yIsOddNumber) {
            paintResourceProvider.getBlackSquarePaint()
        } else {
            paintResourceProvider.getWhiteSquarePaint()
        }
    }
}