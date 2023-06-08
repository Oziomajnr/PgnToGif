package com.chunkymonkey.pgntogifconverter.ui.home

import com.chunkymonkey.pgntogifconverter.R
import com.chunkymonkey.pgntogifconverter.analytics.AnalyticsEvent
import com.chunkymonkey.pgntogifconverter.analytics.AnalyticsEventHandler
import com.chunkymonkey.pgntogifconverter.converter.PgnToGifConverter
import com.chunkymonkey.pgntogifconverter.data.SettingsStorage
import com.chunkymonkey.pgntogifconverter.ui.ApplicationText
import com.chunkymonkey.pgntogifconverter.ui.error.ApplicationStringProvider
import com.chunkymonkey.pgntogifconverter.util.ErrorHandler
import com.github.bhlangonijr.chesslib.pgn.PgnHolder
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

    override fun processPgnFile(pgnFile: File) {
        try {
            analyticsEventHandler.logEvent(AnalyticsEvent.ProcessingPgnFile)
            val pgn = PgnHolder(
                pgnFile.absolutePath
            )
            pgn.loadPgn()
            if (pgn.games.firstOrNull() != null) {
                view?.setPgnText(pgn.toString())
            }
            job?.cancel()
            job = coroutineScope?.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    view?.updateProgressBarVisibility(true)
                }
                val game = pgn.games.firstOrNull()
                if (game == null) {
                    view?.showErrorMessage(applicationStringProvider.getErrorMessage(ApplicationText.CURRENT_PGN_DOES_NOT_CONTAIN_ANY_GAME))
                } else {
                    currentFilePath = pgnToGifConverter.createGifFileFromChessGame(
                        game, settingsStorage.getSettings()
                    )
                }

                withContext(Dispatchers.Main) {
                    view?.updateProgressBarVisibility(false)
                    currentFilePath?.let {
                        analyticsEventHandler.logEvent(AnalyticsEvent.LoadingPgnFileToView)
                        view?.displayGifFromFile(it)
                    }
                }
            }
        } catch (ex: Exception) {
            ErrorHandler.logException(ex)
            ErrorHandler.logInfo("Failed to parse pgn with value ${view?.getCurrentPgnText()}")
            view?.showErrorMessage(applicationStringProvider.getErrorMessage(ApplicationText.UNABLE_TO_GENERATE_GIF))
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