package com.chunkymonkey.pgntogifconverter.converter

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Environment
import androidx.core.graphics.drawable.toBitmap
import com.chunkymonkey.pgntogifconverter.util.AnimatedGifEncoder
import com.example.pgntogifconverter.resource.ChessPieceResource
import com.example.pgntogifconverter.resource.PaintResource
import com.example.pgntogifconverter.util.getCoordinateFromSquare
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.pgn.PgnHolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

class PgnToGifConverter(private val context: Application) {
    private val paintResource = PaintResource(context)
    private val chessPieceResource = ChessPieceResource(context)
    private fun createBitmapFromChessBoard(chessBoard: Board, currentMove: Move): Bitmap {
        val boardArray = chessBoard.boardToArray()

        val boardSize = 505

        var currentx = 0f
        var currenty = 0f

        val sizePerSquare = boardSize / 8

        val conf = Bitmap.Config.ARGB_8888 // see other conf types

        val finalBitmap =
            Bitmap.createBitmap(boardSize, boardSize, conf) // this creates a MUTABLE bitmap

        val canvas = Canvas(finalBitmap)

        var boardArrayIndex = 0
        for (x in 0 until 8) {
            for (y in 0 until 8) {
                val xIsOddNumber = x % 2 != 0
                val yIsOddNumber = y % 2 != 0
                val paint =
                    if (xIsOddNumber && yIsOddNumber || !xIsOddNumber && !yIsOddNumber) {
                        paintResource.blackSquarePaint
                    } else {
                        paintResource.whiteSquarePaint
                    }
                canvas.drawRect(
                    currentx,
                    currenty,
                    currentx + sizePerSquare,
                    currenty + sizePerSquare, paint
                )


                val currentPiece = boardArray[boardArrayIndex]
                if (chessBoard.isKingAttacked && ((currentPiece == Piece.BLACK_KING && chessBoard.sideToMove == Side.BLACK)
                            || (currentPiece == Piece.WHITE_KING && chessBoard.sideToMove == Side.WHITE))
                ) {
                    canvas.drawRect(
                        currentx,
                        currenty,
                        currentx + sizePerSquare,
                        currenty + sizePerSquare, paintResource.kingAttackedPaint
                    )
                }

                val coordinateFrom = getCoordinateFromSquare(currentMove.from)
                val coordinateTo = getCoordinateFromSquare(currentMove.to)
                if (coordinateFrom.first == y && coordinateFrom.second == x) {
                    canvas.drawRect(
                        currentx,
                        currenty,
                        currentx + sizePerSquare,
                        currenty + sizePerSquare, paintResource.highlightedSquarePaint
                    )
                }
                if (coordinateTo.first == y && coordinateTo.second == x) {
                    canvas.drawRect(
                        currentx,
                        currenty,
                        currentx + sizePerSquare,
                        currenty + sizePerSquare, paintResource.highlightedSquarePaint
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
                            it, currentx,
                            currenty,
                            null
                        )
                    }
                }
                currentx += sizePerSquare

                boardArrayIndex++
            }
            currentx = 0f
            currenty += sizePerSquare
        }
        return finalBitmap
    }

    fun createGifFileFromPgn(pgn: PgnHolder): File {
        val board = Board()
        val bos = ByteArrayOutputStream()

        val encoder = AnimatedGifEncoder()
        encoder.setSize(500, 500)
        encoder.setDelay(500)
        encoder.setRepeat(1)
        encoder.start(bos)


        for (game in pgn.games) {
            game.loadMoveText()
            val moves = game.halfMoves
            for (move in moves) {
                board.doMove(move)
                val bitmap = createBitmapFromChessBoard(board, move)
                encoder.addFrame(bitmap)
            }
        }
        encoder.finish()
        val currentFilePath =
            File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "PgnToChessGifs" + Date().time.toString() + ".gif"
            )
        val outStream =
            FileOutputStream(currentFilePath)
        outStream.write(bos.toByteArray())
        outStream.close()
        return currentFilePath
    }
}