package com.chunkymonkey.pgntogifconverter.converter

import android.app.Application
import android.graphics.*
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.chunkymonkey.pgntogifconverter.R
import com.chunkymonkey.pgntogifconverter.data.SettingsData
import com.chunkymonkey.pgntogifconverter.resource.ChessPieceResource
import com.chunkymonkey.pgntogifconverter.resource.PaintResource
import com.chunkymonkey.pgntogifconverter.util.AnimatedGifEncoder
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.game.Game
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.math.roundToInt


class PgnToGifConverter(
    private val context: Application,
    private val playerNameHelper: PlayerNameHelper
) {
    private val paintResource = PaintResource(context)
    private val chessPieceResource = ChessPieceResource(context)
    private val chessBoardToBitmapConverter =
        ChessBoardToBitmapConverter(paintResource, chessPieceResource)

    fun createGifFileFromChessGame(game: Game, settingsData: SettingsData): File {
        val board = Board()
        val bos = ByteArrayOutputStream()

        val shouldAddName = playerNameHelper.shouldShowPlayerName(game, settingsData)

        val bitmapWidth = 500
        val bitmapHeight = if (shouldAddName) {
            560
        } else {
            500
        }
        val blackPlayerName = playerNameHelper.getBlackPlayerName(game, settingsData)

        val whitePlayerName = playerNameHelper.getWhitePlayerName(game, settingsData)

        val encoder = AnimatedGifEncoder()
        encoder.setSize(bitmapWidth, bitmapHeight)
        encoder.setDelay((settingsData.moveDelay * 1000).roundToInt())
        encoder.setRepeat(1)
        encoder.setQuality(7)
        encoder.start(bos)

        game.loadMoveText()
        val moves = game.halfMoves
        moves.forEachIndexed { index, move ->
            board.doMove(move)
            val bitmap = chessBoardToBitmapConverter.createBitmapFromChessBoard(
                board,
                move,
                settingsData.shouldFlipBoard
            )
            if (index == moves.lastIndex) {
                encoder.setDelay((settingsData.lastMoveDelay * 1000).roundToInt())
            }
            encoder.addFrame(
                if (shouldAddName) {
                    if (settingsData.shouldFlipBoard) {
                        mergeBoardAndText(
                            bitmap,
                            getTextBitmap(whitePlayerName),
                            getTextBitmap(blackPlayerName)

                        )
                    } else {
                        mergeBoardAndText(
                            bitmap,
                            getTextBitmap(blackPlayerName),
                            getTextBitmap(whitePlayerName)
                        )
                    }

                } else {
                    bitmap
                }
            )
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

    private fun getTextBitmap(playerName: String): Bitmap {
        val finalBitmap =
            Bitmap.createBitmap(500, 30, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(finalBitmap)
        val paint = Paint()
        val textColor = ContextCompat.getColor(context, R.color.player_name_text_color)
        val textBackgroundColor =
            ContextCompat.getColor(context, R.color.player_name_background_color)
        paint.color = textBackgroundColor
        paint.style = Paint.Style.FILL
        canvas.drawPaint(paint)
        paint.typeface = ResourcesCompat.getFont(context, R.font.roboto_bolditalic)
        paint.color = textColor
        paint.textSize = 25F
        canvas.drawText(playerName, 10F, 25F, paint)
        canvas.drawBitmap(finalBitmap, 0F, 0F, null)
        return finalBitmap
    }

    private fun mergeBoardAndText(bmp1: Bitmap, bmp2: Bitmap, bmp3: Bitmap): Bitmap {
        val bmOverlay = Bitmap.createBitmap(bmp1.width, bmp1.height + 60, bmp1.config)
        val canvas = Canvas(bmOverlay)
        canvas.drawBitmap(bmp1, 0f, 25f, null)
        canvas.drawBitmap(bmp2, 0f, 0f, null)
        canvas.drawBitmap(bmp3, 0f, 530f, null)
        bmp1.recycle()
        bmp2.recycle()
        return bmOverlay
    }

}