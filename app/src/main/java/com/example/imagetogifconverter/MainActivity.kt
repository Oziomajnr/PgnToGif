package com.example.imagetogifconverter

import android.os.Bundle
import com.example.imagetogifconverter.databinding.ActivityMainBinding
import com.example.imagetogifconverter.util.GifUtil
import com.example.imagetogifconverter.util.Permission
import com.github.bhlangonijr.chesslib.Board
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.pgn.PgnHolder
import java.io.File
import java.util.*


class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val blackSquarePaint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.dark_square_color)
        }
    }
    private val whiteSquarePaint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.light_square_color)
        }
    }

    private val piecePaint by lazy {
        Paint().apply {
            color = Color.RED
        }
    }

    private val borderPaint = Paint().apply {
        color = Color.BLACK
    }

    private val bitmaps: MutableList<Bitmap> = mutableListOf()

    override val layout = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Permission.verifyStoragePermissions(this)
        val board = Board()
        val pgn = PgnHolder(
            Environment.getExternalStorageDirectory().absolutePath + "/sample.pgn"
        )
        pgn.loadPgn()
        for (game in pgn.games) {
            game.loadMoveText()
            val moves = game.halfMoves
            //Replay all the moves from the game and print the final position in FEN format
            for (move in moves) {
                board.doMove(move)
                bitmaps.add(createBitmapFromChessBoard(board))
            }
            println("FEN: " + board.fen)
        }
        val filePath = GifUtil.saveGif(this, bitmaps)

        Glide.with(this).load(File(filePath)).into(binding.image)
    }

    private fun createBitmapFromChessBoard(chessBoard: Board): Bitmap {
        val boardArray = chessBoard.boardToArray()

        borderPaint.color = Color.BLACK
        borderPaint.strokeWidth = 20f

        val boardSize = 1000

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
                        blackSquarePaint
                    } else {
                        whiteSquarePaint
                    }
                canvas.drawRect(
                    currentx,
                    currenty,
                    currentx + sizePerSquare,
                    currenty + sizePerSquare, paint
                )
                val currentPiece = boardArray[boardArrayIndex]
                val pieceDrawableId = pieceToDrawableMap[currentPiece]

                if (pieceDrawableId != null) {
                    val pieceDrawable = ContextCompat.getDrawable(this, pieceDrawableId)
                    val bitmap =
                        pieceDrawable?.toBitmap(
                            sizePerSquare - 20,
                            sizePerSquare - 20
                        )
                    bitmap?.let {
                        canvas.drawBitmap(
                            it, currentx + 10,
                            currenty + 10,
                            piecePaint
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