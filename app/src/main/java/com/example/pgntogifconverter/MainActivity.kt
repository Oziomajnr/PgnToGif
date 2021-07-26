package com.example.pgntogifconverter

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Animatable
import android.os.Bundle
import com.example.pgntogifconverter.databinding.ActivityMainBinding
import com.github.bhlangonijr.chesslib.Board
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.example.pgntogifconverter.util.AnimatedGifEncoder
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.pgn.PgnHolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import com.example.pgntogifconverter.util.extention.uriToFile
import com.example.pgntogifconverter.util.getCoordinateFromSquare
import com.example.pgntogifconverter.util.toFile
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView


class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val blackSquarePaint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.dark_square_color)
            colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.DST_OVER)
        }
    }
    private val whiteSquarePaint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.light_square_color)
            colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.DST_OVER)
        }
    }

    private val kingAttackedPaint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.kind_attached_colour)
            colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.DST_OVER)
        }
    }

    private val clearPaint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.kind_attached_colour)
            colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.CLEAR)
        }
    }

    private val highlightedSquarePaint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.highlighted_square_paint)
            colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.LIGHTEN)
        }
    }

    private val piecePaint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.light_square_color)
        }
    }

    override val layout = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.createGifButton.setOnClickListener {
            if (binding.pgnInput.text.isNullOrBlank()) {
                Toast.makeText(this, "Please enter Pgn", Toast.LENGTH_LONG).show()
            } else {
                createGifFromPgn(
                    binding.pgnInput.text.toString().toFile(
                        "game.pgn",
                        this.applicationContext
                    )
                )
            }
        }

        binding.loadPgn.setOnClickListener {
            selectPgnFileFromSystem()
        }

        binding.image.setOnClickListener {
            val drawable: Drawable = (it as ImageView).drawable
            if (drawable is Animatable) {
                val animatedble = (drawable as Animatable)
                if (animatedble.isRunning) {
                    animatedble.stop()
                } else {
                    animatedble.start()
                }

            }
        }

        handleFromSystemIntent()

    }

    private fun handleFromSystemIntent() {
        val intentData = intent.data
        val clipData = intent.clipData
        if (intentData != null) {
            handleIntent(intentData)
        } else if (clipData != null) {
            val firstItem = if (clipData.itemCount > 0) {
                clipData.getItemAt(0)
            } else {
                null
            }
            if (firstItem != null) {
                createGifFromPgn(
                    firstItem.text.toString().toFile(
                        "game.pgn",
                        this.applicationContext
                    )
                )
            }
        }
    }

    private fun createGifFromPgn(pgnFile: File) {
        val board = Board()
        val pgn = PgnHolder(
            pgnFile.absolutePath
        )
        val bos = ByteArrayOutputStream()

        val encoder = AnimatedGifEncoder()
        encoder.setSize(500, 500)
        encoder.setDelay(500)
        encoder.setRepeat(1)
        encoder.start(bos)

        pgn.loadPgn()
        binding.pgnInput.setText(pgn.toString())
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
        val filePath =
            File(application.filesDir, Date().time.toString() + ".gif")
        val outStream =
            FileOutputStream(filePath)
        outStream.write(bos.toByteArray())
        outStream.close()
        Glide.with(this).load(filePath).into(binding.image)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

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
                if (chessBoard.isKingAttacked && ((currentPiece == Piece.BLACK_KING && chessBoard.sideToMove == Side.BLACK)
                            || (currentPiece == Piece.WHITE_KING && chessBoard.sideToMove == Side.WHITE))
                ) {
                    canvas.drawRect(
                        currentx,
                        currenty,
                        currentx + sizePerSquare,
                        currenty + sizePerSquare, kingAttackedPaint
                    )
                }

                val coordinateFrom = getCoordinateFromSquare(currentMove.from)
                val coordinateTo = getCoordinateFromSquare(currentMove.to)
                if (coordinateFrom.first == y && coordinateFrom.second == x) {
                    canvas.drawRect(
                        currentx,
                        currenty,
                        currentx + sizePerSquare,
                        currenty + sizePerSquare, highlightedSquarePaint
                    )
                }
                if (coordinateTo.first == y && coordinateTo.second == x) {
                    canvas.drawRect(
                        currentx,
                        currenty,
                        currentx + sizePerSquare,
                        currenty + sizePerSquare, highlightedSquarePaint
                    )
                }
                val pieceDrawableId = pieceToDrawableMap[currentPiece]

                if (pieceDrawableId != null) {
                    val pieceDrawable = ContextCompat.getDrawable(this, pieceDrawableId)
                    val bitmap =
                        pieceDrawable?.toBitmap(
                            sizePerSquare,
                            sizePerSquare
                        )
                    bitmap?.let {
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

    private fun selectPgnFileFromSystem() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "application/vnd.chess-pgn",
                    "application/x-chess-pgn"
                )
            )
        }

        startActivityForResult(intent, PICK_PDF_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_FILE) {
            if (data != null && data.data != null) {
                handleIntent(data.data!!)
            }
        }
    }

    private fun handleIntent(data: Uri) {
        val selectedFile = data.uriToFile(this.applicationContext)
        if (selectedFile != null) {
            createGifFromPgn(selectedFile)
        }
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


        // Request code for selecting a PDF document.
        const val PICK_PDF_FILE = 123
    }
}