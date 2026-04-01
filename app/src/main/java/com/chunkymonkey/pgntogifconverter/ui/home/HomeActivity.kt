package com.chunkymonkey.pgntogifconverter.ui.home

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chunkymonkey.pgntogifconverter.R
import com.chunkymonkey.pgntogifconverter.analytics.AnalyticsEvent
import com.chunkymonkey.pgntogifconverter.analytics.AnalyticsEventHandler
import com.chunkymonkey.pgntogifconverter.audio.ChessSoundPlayer
import com.chunkymonkey.pgntogifconverter.converter.PgnToGifConverter
import com.chunkymonkey.pgntogifconverter.data.PreferenceSettingsStorage
import com.chunkymonkey.pgntogifconverter.data.RecentGame
import com.chunkymonkey.pgntogifconverter.data.RecentGamesStorage
import com.chunkymonkey.pgntogifconverter.data.SettingsStorage
import com.chunkymonkey.pgntogifconverter.dependency.DependencyFactory
import com.chunkymonkey.pgntogifconverter.preference.PreferenceService
import com.chunkymonkey.pgntogifconverter.review.InAppReviewPromptController
import com.chunkymonkey.pgntogifconverter.ui.DefaultNavigator
import com.chunkymonkey.pgntogifconverter.ui.error.ApplicationStringProvider
import com.chunkymonkey.pgntogifconverter.ui.error.ApplicationStringProviderImpl
import com.chunkymonkey.pgntogifconverter.ui.error.ToastUiErrorHandler
import com.chunkymonkey.pgntogifconverter.ui.error.UiErrorHandler
import com.chunkymonkey.pgntogifconverter.ui.settings.SettingsScreen
import com.chunkymonkey.pgntogifconverter.ui.ui.theme.ImageToGifConverterTheme
import com.chunkymonkey.pgntogifconverter.util.ErrorHandler
import com.chunkymonkey.pgntogifconverter.util.TestProcess
import com.chunkymonkey.pgntogifconverter.util.extention.toFile
import com.chunkymonkey.pgntogifconverter.util.extention.uriToFile
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import java.io.File
import kotlin.math.roundToInt

class HomeActivity : AppCompatActivity(), HomeView {

    private val analyticsEventHandler: AnalyticsEventHandler =
        DependencyFactory.getAnalyticsEventHandler()

    private val errorMessageHandler: UiErrorHandler by lazy {
        ToastUiErrorHandler(this)
    }
    private val settingsStorage: SettingsStorage by lazy {
        PreferenceSettingsStorage(PreferenceService(this.applicationContext))
    }
    private val applicationStringProvider: ApplicationStringProvider by lazy {
        ApplicationStringProviderImpl(this.applicationContext)
    }
    private val pgnToGifConverter: PgnToGifConverter by lazy {
        PgnToGifConverter(this.application, DependencyFactory.getPlayerNameHelper())
    }
    private val homePresenter: HomePresenter by lazy {
        HomePresenterImpl(
            analyticsEventHandler, applicationStringProvider, pgnToGifConverter, settingsStorage
        )
    }
    private val reviewManager by lazy { ReviewManagerFactory.create(this.applicationContext) }
    private val inAppReviewPromptController by lazy {
        InAppReviewPromptController(PreferenceService(applicationContext))
    }
    private val recentGamesStorage: RecentGamesStorage by lazy {
        RecentGamesStorage(PreferenceService(this.applicationContext))
    }
    private val chessSoundPlayer: ChessSoundPlayer by lazy { ChessSoundPlayer(this@HomeActivity) }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult: ActivityResult ->
            activityResult.data?.data?.let { resultUri -> handleIntent(resultUri) }
        }

    // --- Compose UI state ---
    var pgnInputText by mutableStateOf("")
    var isProgressVisible by mutableStateOf(false)
        private set
    var loadingStatusText by mutableStateOf<String?>(null)
        private set
    var gifFile by mutableStateOf<File?>(null)
        private set
    private var gifLoadKey by mutableIntStateOf(0)
    var boardBitmap by mutableStateOf<Bitmap?>(null)
        private set
    val moveList = mutableStateListOf<MoveData>()
    var currentMoveIndex by mutableIntStateOf(-1)
        private set
    var showGifMode by mutableStateOf(false)
        private set
    var isAutoPlaying by mutableStateOf(false)
        private set
    var clipboardPgn by mutableStateOf<String?>(null)
        private set
    val recentGames = mutableStateListOf<RecentGame>()
    var soundEnabled by mutableStateOf(false)
        private set
    var mp4File by mutableStateOf<File?>(null)
        private set

    var gifImageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        analyticsEventHandler.initialize()
        homePresenter.initializeView(this, lifecycleScope)

        refreshRecentGames()

        setContent {
            ImageToGifConverterTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        val autoPlayDelayMs = (settingsStorage.getSettings().moveDelay * 1000).roundToInt().toLong()

                        HomeScreen(
                            pgnText = pgnInputText,
                            onPgnTextChange = { pgnInputText = it },
                            isLoading = isProgressVisible,
                            loadingStatus = loadingStatusText,
                            gifFile = if (showGifMode) gifFile else null,
                            gifLoadKey = gifLoadKey,
                            boardBitmap = if (!showGifMode) boardBitmap else null,
                            moveList = moveList,
                            currentMoveIndex = currentMoveIndex,
                            isAutoPlaying = isAutoPlaying,
                            autoPlayDelayMs = autoPlayDelayMs,
                            clipboardPgn = clipboardPgn,
                            recentGames = recentGames,
                            onMoveClick = { index ->
                                showGifMode = false
                                currentMoveIndex = index
                                homePresenter.navigateToMove(index)
                            },
                            onStepForward = {
                                val nextIndex = (currentMoveIndex + 1).coerceAtMost(moveList.size - 1)
                                showGifMode = false
                                currentMoveIndex = nextIndex
                                homePresenter.navigateToMove(nextIndex)
                                if (soundEnabled && nextIndex >= 0 && nextIndex < moveList.size) {
                                    playMoveSound(moveList[nextIndex])
                                }
                            },
                            onStepBackward = {
                                val prevIndex = (currentMoveIndex - 1).coerceAtLeast(-1)
                                showGifMode = false
                                currentMoveIndex = prevIndex
                                homePresenter.navigateToMove(prevIndex)
                            },
                            onToggleAutoPlay = {
                                isAutoPlaying = !isAutoPlaying
                                if (isAutoPlaying) showGifMode = false
                            },
                            onLoadPgn = ::onLoadPgnClicked,
                            onGenerateGifClick = {
                                homePresenter.generateGif()
                            },
                            onImportPgnClick = ::onImportPgnClicked,
                            onExportClick = { homePresenter.shareCurrentGif() },
                            onSaveClick = ::onSaveToDeviceClicked,
                            onExportMp4Click = {
                                homePresenter.generateMp4()
                            },
                            onShareMp4Click = {
                                homePresenter.shareMp4()
                            },
                            onSettingsClick = {
                                analyticsEventHandler.logEvent(AnalyticsEvent.SettingsClicked)
                                navController.navigate("settings")
                            },
                            onGifImageViewCreated = { gifImageView = it },
                            onClipboardLoad = {
                                clipboardPgn?.let { pgn ->
                                    pgnInputText = pgn
                                    clipboardPgn = null
                                    onLoadPgnClicked()
                                }
                            },
                            onClipboardDismiss = { clipboardPgn = null },
                            onRecentGameClick = { game ->
                                pgnInputText = game.pgn
                                onLoadPgnClicked()
                            },
                            onClearRecentGames = {
                                recentGamesStorage.clearRecentGames()
                                refreshRecentGames()
                            },
                            onStartFromMove = { moveIndex ->
                                homePresenter.generateGifFromMove(moveIndex)
                            },
                            soundEnabled = soundEnabled,
                        )
                    }
                    composable("settings") {
                        SettingsScreen(onBackPressed = { navController.popBackStack() })
                    }
                }
            }
        }

        handleFromSystemIntent()
        checkClipboardForPgn()
    }

    private fun playMoveSound(moveData: MoveData) {
        val san = moveData.san
        when {
            san.contains("+") || san.contains("#") -> chessSoundPlayer.playCheckSound()
            san.contains("x") -> chessSoundPlayer.playCaptureSound()
            san.startsWith("O-O") -> chessSoundPlayer.playCastleSound()
            else -> chessSoundPlayer.playMoveSound()
        }
    }

    private fun checkClipboardForPgn() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString() ?: return
                if (looksLikePgn(text)) {
                    clipboardPgn = text
                }
            }
        } catch (_: Exception) {}
    }

    private fun looksLikePgn(text: String): Boolean {
        if (text.length < 10) return false
        val hasHeader = text.contains("[Event ") || text.contains("[White ") || text.contains("[Black ")
        val hasMoves = Regex("1\\.\\s*[A-Za-z]").containsMatchIn(text)
        return hasHeader || hasMoves
    }

    private fun refreshRecentGames() {
        recentGames.clear()
        recentGames.addAll(recentGamesStorage.getRecentGames())
    }

    // --- Button handlers ---

    private fun onLoadPgnClicked() {
        analyticsEventHandler.logEvent(AnalyticsEvent.CreateGifClicked(pgnInputText))
        if (pgnInputText.isBlank()) {
            errorMessageHandler.showError(getString(R.string.please_enter_pgn))
        } else {
            saveToRecent(pgnInputText)
            homePresenter.processPgnFile(
                pgnInputText.toFile("game.pgn", this.applicationContext)
            )
        }
    }

    private fun saveToRecent(pgn: String) {
        val title = extractGameTitle(pgn)
        recentGamesStorage.saveGame(pgn, title)
        refreshRecentGames()
    }

    private fun extractGameTitle(pgn: String): String {
        val whiteMatch = Regex("\\[White \"(.*?)\"\\]").find(pgn)
        val blackMatch = Regex("\\[Black \"(.*?)\"\\]").find(pgn)
        val white = whiteMatch?.groupValues?.getOrNull(1) ?: "?"
        val black = blackMatch?.groupValues?.getOrNull(1) ?: "?"
        return "$white vs $black"
    }

    internal fun onCreateGifClicked() {
        onLoadPgnClicked()
    }

    private fun onImportPgnClicked() {
        analyticsEventHandler.logEvent(AnalyticsEvent.ImportPgnClicked)
        selectPgnFileFromSystem()
    }

    private fun onSaveToDeviceClicked() {
        val file = gifFile
        if (file == null) {
            errorMessageHandler.showError(getString(R.string.no_gif_to_save))
            return
        }
        saveDocumentLauncher.launch(file.name)
    }

    // --- SAF export ---

    private val saveDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("image/gif")) { uri ->
            if (uri != null) {
                saveGifToUri(uri)
            }
        }

    private val saveMp4Launcher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("video/mp4")) { uri ->
            if (uri != null) {
                saveMp4ToUri(uri)
            }
        }

    private fun saveGifToUri(uri: Uri) {
        try {
            val source = gifFile ?: return
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                source.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            errorMessageHandler.showError(getString(R.string.gif_saved))
            onValuableActionCompleted()
        } catch (e: Exception) {
            ErrorHandler.logException(e)
            errorMessageHandler.showError(getString(R.string.gif_save_failed))
        }
    }

    private fun saveMp4ToUri(uri: Uri) {
        try {
            val source = mp4File ?: return
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                source.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            errorMessageHandler.showError(getString(R.string.mp4_saved))
            onValuableActionCompleted()
        } catch (e: Exception) {
            ErrorHandler.logException(e)
            errorMessageHandler.showError(getString(R.string.mp4_save_failed))
        }
    }

    // --- HomeView implementation ---

    override fun shareCurrentGif(file: File) {
        if (DefaultNavigator.shareCurrentGif(file, this)) {
            onValuableActionCompleted()
        }
    }

    override fun shareMp4(file: File) {
        mp4File = file
        if (DefaultNavigator.shareMp4(file, this)) {
            onValuableActionCompleted()
        }
    }

    override fun setPgnText(text: String) {
        pgnInputText = text
    }

    override fun updateProgressBarVisibility(isVisible: Boolean) {
        isProgressVisible = isVisible
        if (!isVisible) loadingStatusText = null
    }

    override fun updateLoadingStatus(status: String?) {
        loadingStatusText = status
    }

    override fun showErrorMessage(message: String) {
        errorMessageHandler.showError(message)
    }

    override fun getCurrentPgnText(): String = pgnInputText

    override fun displayGifFromFile(currentFilePath: File) {
        gifFile = currentFilePath
        gifLoadKey++
        showGifMode = true
    }

    override fun displayMoveList(moves: List<MoveData>) {
        moveList.clear()
        moveList.addAll(moves)
        currentMoveIndex = -1
        showGifMode = false
    }

    override fun displayBoardAtPosition(bitmap: Bitmap) {
        boardBitmap = bitmap
    }

    override fun displayMp4File(file: File) {
        mp4File = file
        saveMp4Launcher.launch(file.name)
    }

    // --- Intent handling ---

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleFromSystemIntent(intent)
    }

    private fun handleFromSystemIntent(newIntent: Intent? = null) {
        val intentData = intent.data ?: newIntent?.data
        val clipData = intent.clipData ?: newIntent?.clipData
        if (intentData != null) {
            analyticsEventHandler.logEvent(AnalyticsEvent.HandlingSystemIntent)
            handleIntent(intentData)
        } else if (clipData != null) {
            analyticsEventHandler.logEvent(AnalyticsEvent.HandlingSystemClipData)
            val firstItem = if (clipData.itemCount > 0) clipData.getItemAt(0) else null
            if (firstItem != null) {
                homePresenter.processPgnFile(
                    firstItem.text.toString().toFile("game.pgn", this.applicationContext)
                )
            }
        }
    }

    private fun handleIntent(data: Uri) {
        val selectedFile = data.uriToFile(this.applicationContext)
        if (selectedFile != null) {
            homePresenter.processPgnFile(selectedFile)
        }
    }

    private fun selectPgnFileFromSystem() {
        val filePickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "application/vnd.chess-pgn", "application/x-chess-pgn"
                )
            )
        }
        getContent.launch(filePickerIntent)
    }

    // --- In-app review (Play In-App Review API; throttled after saves/shares) ---

    private fun onValuableActionCompleted() {
        if (TestProcess.isInstrumentedTest()) return
        inAppReviewPromptController.recordValuableAction()
        maybeRequestInAppReview()
    }

    private fun maybeRequestInAppReview() {
        if (TestProcess.isInstrumentedTest()) return
        if (!inAppReviewPromptController.shouldOfferReview(System.currentTimeMillis())) return
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                inAppReviewPromptController.markReviewFlowLaunched(System.currentTimeMillis())
                reviewManager.launchReviewFlow(this, task.result)
            } else {
                task.exception?.let { ex ->
                    ErrorHandler.logException(ex)
                    if (ex is ReviewException) {
                        ErrorHandler.logInfo("Review Task failed with code ${ex.errorCode}")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        homePresenter.onDestroy()
        chessSoundPlayer.release()
    }
}
