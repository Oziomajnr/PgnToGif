package com.chunkymonkey.pgntogifconverter.converter

import android.app.Application
import android.os.Environment
import com.chunkymonkey.pgntogifconverter.data.SettingsData
import com.chunkymonkey.pgntogifconverter.dependency.DependencyFactory
import com.chunkymonkey.pgntogifconverter.resource.ChessPieceResourceProvider
import com.chunkymonkey.pgntogifconverter.resource.PaintResourceProvider
import com.chunkymonkey.pgntogifconverter.util.AnimatedGifEncoder
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.game.Game
import com.github.bhlangonijr.chesslib.move.Move
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.math.roundToInt

class PgnToGifConverter(
    val context: Application,
    private val playerNameHelper: PlayerNameHelper
) {
    fun createGifFileFromChessGame(
        game: Game,
        settingsData: SettingsData,
        startFromMove: Int = 0
    ): File {
        val board = Board()

        val settingsStorage = DependencyFactory.getSettingsStorage()
        val paintResourceProvider = PaintResourceProvider(context, settingsStorage)
        val chessPieceResourceProvider = ChessPieceResourceProvider(context, settingsStorage)
        val converter = ChessBoardToBitmapConverter(
            paintResourceProvider, chessPieceResourceProvider, settingsData.boardResolution
        )

        val shouldAddName = playerNameHelper.shouldShowPlayerName(game, settingsData)

        val blackPlayerName = playerNameHelper.getBlackPlayerName(game, settingsData)
        val whitePlayerName = playerNameHelper.getWhitePlayerName(game, settingsData)

        val topName: String?
        val bottomName: String?
        if (shouldAddName) {
            if (settingsData.shouldFlipBoard) {
                topName = whitePlayerName
                bottomName = blackPlayerName
            } else {
                topName = blackPlayerName
                bottomName = whitePlayerName
            }
        } else {
            topName = null
            bottomName = null
        }

        val frameWidth = converter.frameWidth()
        val frameHeight = converter.frameHeight(shouldAddName)

        val currentFilePath = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "PgnToChessGifs${Date().time}.gif"
        )

        FileOutputStream(currentFilePath).use { fos ->
            val encoder = AnimatedGifEncoder()
            encoder.setSize(frameWidth, frameHeight)
            encoder.setDelay((settingsData.moveDelay * 1000).roundToInt())
            encoder.setRepeat(settingsData.gifLoopCount)
            encoder.setQuality(settingsData.gifQuality)
            encoder.start(fos)

            game.loadMoveText()
            val moves = game.halfMoves
            val effectiveStart = startFromMove.coerceIn(0, moves.size)

            for (i in 0 until effectiveStart) {
                board.doMove(moves[i])
            }

            val noMove = Move(Square.NONE, Square.NONE)
            encoder.addFrame(
                converter.createBitmapFromChessBoard(
                    board, noMove, settingsData.shouldFlipBoard,
                    settingsData.showBoardCoordinates, topName, bottomName
                )
            )

            val gameResult = if (settingsData.showGameResult) {
                extractGameResult(game)
            } else null

            for (i in effectiveStart until moves.size) {
                board.doMove(moves[i])
                if (i == moves.lastIndex) {
                    encoder.setDelay((settingsData.lastMoveDelay * 1000).roundToInt())
                }
                val isLastMove = i == moves.lastIndex
                encoder.addFrame(
                    converter.createBitmapFromChessBoard(
                        board, moves[i], settingsData.shouldFlipBoard,
                        settingsData.showBoardCoordinates, topName, bottomName,
                        gameResult = if (isLastMove) gameResult else null
                    )
                )
            }

            encoder.finish()
        }

        return currentFilePath
    }

    private fun extractGameResult(game: Game): String? {
        return try {
            val result = game.result?.description
            when {
                result == null -> null
                result.contains("1-0") || result.contains("White wins") -> "1-0"
                result.contains("0-1") || result.contains("Black wins") -> "0-1"
                result.contains("1/2") || result.contains("Draw") -> "½-½"
                else -> {
                    val resultProp = game.property?.get("Result")
                    when (resultProp) {
                        "1-0" -> "1-0"
                        "0-1" -> "0-1"
                        "1/2-1/2" -> "½-½"
                        else -> null
                    }
                }
            }
        } catch (e: Exception) {
            try {
                val resultProp = game.property?.get("Result")
                when (resultProp) {
                    "1-0" -> "1-0"
                    "0-1" -> "0-1"
                    "1/2-1/2" -> "½-½"
                    else -> null
                }
            } catch (e2: Exception) {
                null
            }
        }
    }
}
