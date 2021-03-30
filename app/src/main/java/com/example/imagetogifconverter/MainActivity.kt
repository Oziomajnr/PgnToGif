package com.example.imagetogifconverter

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.imagetogifconverter.databinding.ActivityMainBinding
import com.github.bhlangonijr.chesslib.Board
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.example.imagetogifconverter.util.AnimatedGifEncoder
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.pgn.PgnHolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import com.example.imagetogifconverter.util.toFile
import com.example.imagetogifconverter.util.extention.uriToFile


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

    override val layout = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.createGifButton.setOnClickListener {
            if (binding.pgnInput.text.isBlank()) {
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
        handleFromSystemIntent()

    }

    fun handleFromSystemIntent() {
        val intentData = intent.data
        if (intentData != null) {
            handleIntent(intent)
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
        for (game in pgn.games) {
            game.loadMoveText()
            val moves = game.halfMoves
            for (move in moves) {
                board.doMove(move)
                val bitmap = createBitmapFromChessBoard(board)
                encoder.addFrame(bitmap)
            }
        }
        encoder.finish()
        val filePath =
            File(application.cacheDir, Date().time.toString() + ".gif")
        val outStream =
            FileOutputStream(filePath)
        outStream.write(bos.toByteArray())
        outStream.close()
        Glide.with(this).load(filePath).into(binding.image)
    }

    private fun createBitmapFromChessBoard(chessBoard: Board): Bitmap {
        val boardArray = chessBoard.boardToArray()

        val boardSize = 500

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
                            sizePerSquare - 5,
                            sizePerSquare - 5
                        )
                    bitmap?.let {
                        canvas.drawBitmap(
                            it, currentx + 5,
                            currenty + 5,
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

    private fun generateGIF(context: Context, bitmaps: List<Bitmap>): ByteArray {

        val bos = ByteArrayOutputStream()
        val encoder = AnimatedGifEncoder()
        encoder.setSize(980, 980)
        encoder.setDelay(50)
        encoder.start(bos)
        for (bitmap in bitmaps) {
            encoder.addFrame(bitmap)
        }
        encoder.finish()
        return bos.toByteArray()
    }

    fun saveGif(file: File): Uri? {
        // Add a specific media item.
        val resolver = applicationContext.contentResolver

// Find all audio files on the primary external storage device.
        val imageCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

// Publish a new song.
        val newSongDetails = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "My Song.mp3")
        }

// Keeps a handle to the new song's URI in case we need to modify it
// later.
        val myFavoriteSongUri = resolver
            .insert(imageCollection, newSongDetails)
        return myFavoriteSongUri
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
            if (data != null) {
                handleIntent(data)
            }
        }
    }

    fun handleIntent(intent: Intent) {
        val selectedFile = intent.data?.uriToFile(this.applicationContext)
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