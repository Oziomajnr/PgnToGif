package com.chunkymonkey.pgntogifconverter.converter

import android.app.Application
import android.os.Environment
import com.chunkymonkey.pgntogifconverter.resource.ChessPieceResource
import com.chunkymonkey.pgntogifconverter.resource.PaintResource
import com.chunkymonkey.pgntogifconverter.util.AnimatedGifEncoder
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.game.Game
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

class PgnToGifConverter(private val context: Application) {
    private val paintResource = PaintResource(context)
    private val chessPieceResource = ChessPieceResource(context)
    private val chessBoardToBitmapConverter =
        ChessBoardToBitmapConverter(paintResource, chessPieceResource)

    fun createGifFileFromChessGame(game: Game): File {
        val board = Board()
        val bos = ByteArrayOutputStream()

        val encoder = AnimatedGifEncoder()
        encoder.setSize(500, 500)
        encoder.setDelay(500)
        encoder.setRepeat(1)
        encoder.start(bos)


        game.loadMoveText()
        val moves = game.halfMoves
        for (move in moves) {
            board.doMove(move)
            val bitmap = chessBoardToBitmapConverter.createBitmapFromChessBoard(
                board,
                move
            )
            encoder.addFrame(bitmap)
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