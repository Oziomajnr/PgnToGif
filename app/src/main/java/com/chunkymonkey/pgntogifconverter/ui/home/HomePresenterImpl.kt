package com.chunkymonkey.pgntogifconverter.ui.home

import com.chunkymonkey.pgntogifconverter.analytics.AnalyticsEvent
import com.chunkymonkey.pgntogifconverter.analytics.AnalyticsEventHandler
import com.chunkymonkey.pgntogifconverter.converter.ChessBoardToBitmapConverter
import com.chunkymonkey.pgntogifconverter.converter.PgnToGifConverter
import com.chunkymonkey.pgntogifconverter.converter.PgnToMp4Converter
import com.chunkymonkey.pgntogifconverter.data.SettingsStorage
import com.chunkymonkey.pgntogifconverter.dependency.DependencyFactory
import com.chunkymonkey.pgntogifconverter.resource.ChessPieceResourceProvider
import com.chunkymonkey.pgntogifconverter.resource.PaintResourceProvider
import com.chunkymonkey.pgntogifconverter.ui.ApplicationText
import com.chunkymonkey.pgntogifconverter.ui.error.ApplicationStringProvider
import com.chunkymonkey.pgntogifconverter.util.ErrorHandler
import com.chunkymonkey.chesscore.Board
import com.chunkymonkey.chesscore.Move
import com.chunkymonkey.chesscore.ParsedGame
import com.chunkymonkey.chesscore.PgnParseException
import com.chunkymonkey.chesscore.PgnParser
import com.chunkymonkey.chesscore.Square
import kotlinx.coroutines.*
import java.io.File

class HomePresenterImpl(
    private val analyticsEventHandler: AnalyticsEventHandler,
    private val applicationStringProvider: ApplicationStringProvider,
    private val pgnToGifConverter: PgnToGifConverter,
    private val settingsStorage: SettingsStorage
) : HomePresenter {

    private var currentFilePath: File? = null
    var view: HomeView? = null
    private var job: Job? = null
    private var coroutineScope: CoroutineScope? = null

    private var currentGame: ParsedGame? = null
    private var parsedMoves: List<MoveData> = emptyList()
    private var currentMoveIndex: Int = -1
    private var lastMp4File: File? = null

    private val pgnToMp4Converter: PgnToMp4Converter by lazy {
        PgnToMp4Converter(pgnToGifConverter.context, DependencyFactory.getPlayerNameHelper())
    }

    private fun showLoading(status: String) {
        view?.updateProgressBarVisibility(true)
        view?.updateLoadingStatus(status)
    }

    private fun hideLoading() {
        view?.updateProgressBarVisibility(false)
        view?.updateLoadingStatus(null)
    }

    override fun processPgnFile(pgnFile: File) {
        analyticsEventHandler.logEvent(AnalyticsEvent.ProcessingPgnFile)
        job?.cancel()
        job = coroutineScope?.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    showLoading("Loading PGN…")
                }

                val pgnText = pgnFile.readText()
                val game = try {
                    PgnParser.parse(pgnText)
                } catch (_: PgnParseException) {
                    null
                }

                withContext(Dispatchers.Main) {
                    if (game != null) view?.setPgnText(pgnText)
                }

                if (game == null) {
                    withContext(Dispatchers.Main) {
                        view?.showErrorMessage(
                            applicationStringProvider.getErrorMessage(ApplicationText.CURRENT_PGN_DOES_NOT_CONTAIN_ANY_GAME)
                        )
                        hideLoading()
                    }
                } else {
                    lastMp4File = null
                    currentGame = game
                    val halfMoves = game.moves
                    val moveDataList = halfMoves.mapIndexed { index, move ->
                        MoveData(
                            index = index,
                            san = move.san ?: move.toString(),
                            move = move,
                            isWhite = index % 2 == 0
                        )
                    }
                    parsedMoves = moveDataList
                    currentMoveIndex = -1

                    withContext(Dispatchers.Main) {
                        view?.displayMoveList(moveDataList)
                        showLoading("Generating GIF…")
                    }

                    renderBoardAtCurrentPosition()

                    currentFilePath = pgnToGifConverter.createGifFileFromChessGame(
                        game, settingsStorage.getSettings()
                    )

                    withContext(Dispatchers.Main) {
                        hideLoading()
                        currentFilePath?.let {
                            analyticsEventHandler.logEvent(AnalyticsEvent.LoadingPgnFileToView)
                            view?.displayGifFromFile(it)
                        }
                    }
                }
            } catch (ex: Exception) {
                ErrorHandler.logException(ex)
                ErrorHandler.logInfo("Failed to parse pgn with value ${view?.getCurrentPgnText()}")
                withContext(Dispatchers.Main) {
                    hideLoading()
                    view?.showErrorMessage(
                        applicationStringProvider.getErrorMessage(ApplicationText.UNABLE_TO_GENERATE_GIF)
                    )
                }
            }
        }
    }

    override fun navigateToMove(index: Int) {
        if (index < -1 || index >= parsedMoves.size) return
        currentMoveIndex = index
        coroutineScope?.launch(Dispatchers.IO) {
            renderBoardAtCurrentPosition()
        }
    }

    override fun generateGif() {
        val game = currentGame ?: return
        job?.cancel()
        job = coroutineScope?.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    showLoading("Generating GIF…")
                }

                currentFilePath = pgnToGifConverter.createGifFileFromChessGame(
                    game, settingsStorage.getSettings()
                )

                withContext(Dispatchers.Main) {
                    hideLoading()
                    currentFilePath?.let {
                        analyticsEventHandler.logEvent(AnalyticsEvent.LoadingPgnFileToView)
                        view?.displayGifFromFile(it)
                    }
                }
            } catch (ex: Exception) {
                ErrorHandler.logException(ex)
                withContext(Dispatchers.Main) {
                    hideLoading()
                    view?.showErrorMessage(
                        applicationStringProvider.getErrorMessage(ApplicationText.UNABLE_TO_GENERATE_GIF)
                    )
                }
            }
        }
    }

    override fun generateGifFromMove(startIndex: Int) {
        val game = currentGame ?: return
        job?.cancel()
        job = coroutineScope?.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    showLoading("Generating GIF…")
                }

                currentFilePath = pgnToGifConverter.createGifFileFromChessGame(
                    game, settingsStorage.getSettings(), startFromMove = startIndex
                )

                withContext(Dispatchers.Main) {
                    hideLoading()
                    currentFilePath?.let {
                        view?.displayGifFromFile(it)
                    }
                }
            } catch (ex: Exception) {
                ErrorHandler.logException(ex)
                withContext(Dispatchers.Main) {
                    hideLoading()
                    view?.showErrorMessage(
                        applicationStringProvider.getErrorMessage(ApplicationText.UNABLE_TO_GENERATE_GIF)
                    )
                }
            }
        }
    }

    override fun generateMp4() {
        generateMp4FromMove(0)
    }

    override fun generateMp4FromMove(startIndex: Int) {
        val game = currentGame ?: return
        job?.cancel()
        job = coroutineScope?.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    showLoading("Generating MP4…")
                }

                val mp4File = pgnToMp4Converter.createMp4FileFromChessGame(
                    game, settingsStorage.getSettings(), startFromMove = startIndex
                )

                withContext(Dispatchers.Main) {
                    hideLoading()
                    lastMp4File = mp4File
                    view?.displayMp4File(mp4File)
                }
            } catch (ex: Exception) {
                ErrorHandler.logException(ex)
                withContext(Dispatchers.Main) {
                    hideLoading()
                    view?.showErrorMessage("Failed to generate MP4")
                }
            }
        }
    }

    override fun shareMp4() {
        val game = currentGame ?: return
        if (lastMp4File?.exists() == true) {
            view?.shareMp4(lastMp4File!!)
            return
        }
        job?.cancel()
        job = coroutineScope?.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    showLoading("Generating MP4…")
                }
                val mp4File = pgnToMp4Converter.createMp4FileFromChessGame(
                    game, settingsStorage.getSettings(), startFromMove = 0
                )
                withContext(Dispatchers.Main) {
                    hideLoading()
                    lastMp4File = mp4File
                    view?.shareMp4(mp4File)
                }
            } catch (ex: Exception) {
                ErrorHandler.logException(ex)
                withContext(Dispatchers.Main) {
                    hideLoading()
                    view?.showErrorMessage("Failed to generate MP4")
                }
            }
        }
    }

    override fun getParsedMoves(): List<MoveData> = parsedMoves

    override fun getCurrentGame(): ParsedGame? = currentGame

    private suspend fun renderBoardAtCurrentPosition() {
        val board = Board()
        val moves = parsedMoves
        val targetIndex = currentMoveIndex
        val settings = settingsStorage.getSettings()

        val settingsStorageRef = DependencyFactory.getSettingsStorage()
        val context = pgnToGifConverter.context
        val paintProvider = PaintResourceProvider(context, settingsStorageRef)
        val pieceProvider = ChessPieceResourceProvider(context, settingsStorageRef)
        val converter = ChessBoardToBitmapConverter(paintProvider, pieceProvider, settings.boardResolution)

        var lastMove = Move(Square.NONE, Square.NONE)
        for (i in 0..targetIndex) {
            if (i < moves.size) {
                board.doMove(moves[i].move)
                lastMove = moves[i].move
            }
        }

        val bitmap = converter.createBitmapFromChessBoard(
            board, lastMove, settings.shouldFlipBoard,
            settings.showBoardCoordinates
        )

        withContext(Dispatchers.Main) {
            view?.displayBoardAtPosition(bitmap)
        }
    }

    override fun initializeView(view: HomeView, coroutineScope: CoroutineScope) {
        this.view = view
        this.coroutineScope = coroutineScope
    }

    override fun onDestroy() {
        view = null
        job?.cancel()
    }

    override fun shareCurrentGif() {
        currentFilePath?.let {
            view?.shareCurrentGif(it)
        } ?: run {
            view?.showErrorMessage(applicationStringProvider.getErrorMessage(ApplicationText.PLEASE_LOAD_IN_GIF))
        }
    }
}
