package com.chunkymonkey.pgntogifconverter.converter

import com.chunkymonkey.pgntogifconverter.resource.ChessPieceResourceProvider
import com.chunkymonkey.pgntogifconverter.resource.PaintResourceProvider
import com.chunkymonkey.pgntogifconverter.util.getCoordinateFromSquare

import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.Board

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.chunkymonkey.pgntogifconverter.util.getCoordinateFromSquareWithFlippedBoard

class ChessBoardToBitmapConverter(
    private val paintResourceProvider: PaintResourceProvider,
    private val chessPieceResourceProvider: ChessPieceResourceProvider,
    private val boardSize: Int = 504
) {
    val sizePerSquare = boardSize / 8

    private val pieceBitmapCache: Map<Piece, Bitmap?> by lazy {
        chessPieceResourceProvider.buildPieceBitmapCache(sizePerSquare)
    }

    companion object {
        private const val SIDE_BORDER = 3f
        private const val NAME_BORDER = 22f
    }

    fun frameWidth(): Int = boardSize + (SIDE_BORDER * 2).toInt()

    fun frameHeight(hasPlayerNames: Boolean): Int {
        val vertBorder = if (hasPlayerNames) NAME_BORDER else SIDE_BORDER
        return boardSize + (vertBorder * 2).toInt()
    }

    fun createBitmapFromChessBoard(
        chessBoard: Board,
        currentMove: Move,
        shouldFlipBoard: Boolean,
        showBoardCoordinates: Boolean = false,
        topPlayerName: String? = null,
        bottomPlayerName: String? = null,
        gameResult: String? = null
    ): Bitmap =
        getGameBoardBitmap(
            chessBoard, currentMove, shouldFlipBoard, showBoardCoordinates,
            topPlayerName, bottomPlayerName, gameResult
        )

    private fun getGameBoardBitmap(
        chessBoard: Board,
        currentMove: Move,
        shouldFlipBoard: Boolean,
        showBoardCoordinates: Boolean,
        topPlayerName: String?,
        bottomPlayerName: String?,
        gameResult: String?
    ): Bitmap {
        val boardArray = chessBoard.boardToArray()

        val hasNames = topPlayerName != null || bottomPlayerName != null
        val topBorder = if (hasNames) NAME_BORDER else SIDE_BORDER
        val bottomBorder = if (hasNames) NAME_BORDER else SIDE_BORDER
        val totalWidth = boardSize + (SIDE_BORDER * 2).toInt()
        val totalHeight = boardSize + topBorder.toInt() + bottomBorder.toInt()

        val finalBitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(finalBitmap)

        canvas.drawPaint(paintResourceProvider.boardFrameFillPaint)

        val offsetX = SIDE_BORDER
        val offsetY = topBorder

        var currentX: Float
        var currentY = (sizePerSquare * 7).toFloat()

        val hasHighlight = currentMove.from != Square.NONE && currentMove.to != Square.NONE
        val coordinateFrom = if (hasHighlight) {
            if (shouldFlipBoard) getCoordinateFromSquareWithFlippedBoard(currentMove.from)
            else getCoordinateFromSquare(currentMove.from)
        } else null
        val coordinateTo = if (hasHighlight) {
            if (shouldFlipBoard) getCoordinateFromSquareWithFlippedBoard(currentMove.to)
            else getCoordinateFromSquare(currentMove.to)
        } else null

        for (x in 0..7) {
            currentX = 0f
            for (y in 0..7) {
                val drawX = currentX + offsetX
                val drawY = currentY + offsetY

                canvas.drawRect(
                    drawX, drawY,
                    drawX + sizePerSquare, drawY + sizePerSquare,
                    getPaintFromBoardCoordinate(x, y)
                )

                val pieceIndex = if (shouldFlipBoard) {
                    getBoardIndexFromBoardCoordinateBlackPOV(x, y)
                } else {
                    getBoardIndexFromBoardCoordinate(x, y)
                }
                val currentPiece = boardArray[pieceIndex]

                if (chessBoard.isKingAttacked &&
                    ((currentPiece == Piece.BLACK_KING && chessBoard.sideToMove == Side.BLACK)
                            || (currentPiece == Piece.WHITE_KING && chessBoard.sideToMove == Side.WHITE))
                ) {
                    canvas.drawRect(
                        drawX, drawY,
                        drawX + sizePerSquare, drawY + sizePerSquare,
                        paintResourceProvider.kingAttackedPaint
                    )
                }

                if (coordinateFrom != null && coordinateFrom.first == y && coordinateFrom.second == x) {
                    canvas.drawRect(
                        drawX, drawY,
                        drawX + sizePerSquare, drawY + sizePerSquare,
                        paintResourceProvider.highlightFromPaint
                    )
                }
                if (coordinateTo != null && coordinateTo.first == y && coordinateTo.second == x) {
                    canvas.drawRect(
                        drawX, drawY,
                        drawX + sizePerSquare, drawY + sizePerSquare,
                        paintResourceProvider.highlightToPaint
                    )
                }

                pieceBitmapCache[currentPiece]?.let { pieceBitmap ->
                    canvas.drawBitmap(pieceBitmap, drawX, drawY, null)
                }

                if (showBoardCoordinates) {
                    drawCoordinates(canvas, x, y, drawX, drawY, shouldFlipBoard)
                }

                currentX += sizePerSquare
            }
            currentY -= sizePerSquare
        }

        if (topPlayerName != null) {
            val paint = paintResourceProvider.playerNameTextPaint
            val textY = (topBorder + paint.textSize) / 2f - paint.descent() / 2f
            canvas.drawText(topPlayerName, SIDE_BORDER + 6f, textY, paint)
        }

        if (bottomPlayerName != null) {
            val paint = paintResourceProvider.playerNameTextPaint
            val nameAreaTop = topBorder + boardSize
            val textY = nameAreaTop + (bottomBorder + paint.textSize) / 2f - paint.descent() / 2f
            canvas.drawText(bottomPlayerName, SIDE_BORDER + 6f, textY, paint)
        }

        if (gameResult != null) {
            drawGameResult(canvas, gameResult, offsetX, offsetY)
        }

        return finalBitmap
    }

    private fun drawGameResult(canvas: Canvas, result: String, offsetX: Float, offsetY: Float) {
        val overlayPaint = Paint().apply {
            color = android.graphics.Color.argb(180, 0, 0, 0)
            style = Paint.Style.FILL
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = boardSize / 10f
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.BOLD
            )
        }

        val centerX = offsetX + boardSize / 2f
        val centerY = offsetY + boardSize / 2f
        val textWidth = textPaint.measureText(result)
        val padding = textPaint.textSize * 0.6f
        val bgRect = RectF(
            centerX - textWidth / 2f - padding,
            centerY - textPaint.textSize - padding / 2f,
            centerX + textWidth / 2f + padding,
            centerY + padding / 2f
        )
        canvas.drawRoundRect(bgRect, 12f, 12f, overlayPaint)
        canvas.drawText(result, centerX, centerY - textPaint.descent() / 2f, textPaint)
    }

    private fun drawCoordinates(
        canvas: Canvas, x: Int, y: Int,
        drawX: Float, drawY: Float,
        shouldFlipBoard: Boolean
    ) {
        val isDarkSquare = isDarkSquare(x, y)
        val paint = if (isDarkSquare)
            paintResourceProvider.coordinateOnDarkPaint
        else
            paintResourceProvider.coordinateOnLightPaint

        val padding = 3f

        if (y == 0) {
            val rankLabel = if (shouldFlipBoard) (8 - x).toString() else (x + 1).toString()
            canvas.drawText(rankLabel, drawX + padding, drawY + paint.textSize + padding, paint)
        }

        if (x == 0) {
            val fileLabel = if (shouldFlipBoard) ('h' - y).toString() else ('a' + y).toString()
            val textWidth = paint.measureText(fileLabel)
            canvas.drawText(
                fileLabel,
                drawX + sizePerSquare - textWidth - padding,
                drawY + sizePerSquare - padding,
                paint
            )
        }
    }

    private fun isDarkSquare(x: Int, y: Int): Boolean {
        val xIsOdd = x % 2 != 0
        val yIsOdd = y % 2 != 0
        return xIsOdd == yIsOdd
    }

    private fun getBoardIndexFromBoardCoordinate(x: Int, y: Int): Int {
        return x * 8 + y
    }

    private fun getBoardIndexFromBoardCoordinateBlackPOV(x: Int, y: Int): Int {
        return 63 - y - (x * 8)
    }

    private fun getPaintFromBoardCoordinate(x: Int, y: Int): Paint {
        return if (isDarkSquare(x, y)) {
            paintResourceProvider.darkSquarePaint
        } else {
            paintResourceProvider.lightSquarePaint
        }
    }
}
