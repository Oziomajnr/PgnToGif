package com.chunkymonkey.pgntogifconverter.ui

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import java.io.File
import com.chunkymonkey.pgntogifconverter.util.extention.uriToFile
import com.chunkymonkey.pgntogifconverter.util.extention.toFile
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.chunkymonkey.pgntogifconverter.dependency.DependencyFactory
import com.chunkymonkey.pgntogifconverter.R
import com.chunkymonkey.pgntogifconverter.analytics.AnalyticsEvent
import com.chunkymonkey.pgntogifconverter.analytics.AnalyticsEventHandler
import com.chunkymonkey.pgntogifconverter.databinding.ActivityMainBinding
import com.chunkymonkey.pgntogifconverter.converter.PgnToGifConverter
import com.chunkymonkey.pgntogifconverter.data.PreferenceSettingsStorage
import com.chunkymonkey.pgntogifconverter.data.SettingsStorage
import com.chunkymonkey.pgntogifconverter.preference.PreferenceService
import com.chunkymonkey.pgntogifconverter.ui.error.ToastUiErrorHandler
import com.chunkymonkey.pgntogifconverter.ui.error.UiErrorHandler
import com.chunkymonkey.pgntogifconverter.ui.settings.SettingsActivity
import com.chunkymonkey.pgntogifconverter.util.ErrorHandler
import com.chunkymonkey.pgntogifconverter.util.extention.getStrictModeUri
import com.github.bhlangonijr.chesslib.pgn.PgnHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception


class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val analyticsEventHandler: AnalyticsEventHandler =
        DependencyFactory.getAnalyticsEventHandler()

    private var currentFilePath: File? = null
    private val errorMessageHandler: UiErrorHandler by lazy {
        ToastUiErrorHandler(this)
    }
    private val settingsStorage: SettingsStorage by lazy {
        PreferenceSettingsStorage(PreferenceService(this.applicationContext))
    }

    override val layout = R.layout.activity_main
    val pgnToGifConverter: PgnToGifConverter by lazy {
        PgnToGifConverter(this.application, DependencyFactory.getPlayerNameHelper())
    }

    private var job: Job? = null

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult: ActivityResult ->
            activityResult.data?.data?.let { resultUri -> handleIntent(resultUri) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsEventHandler.initialize()
        binding.createGifButton.setOnClickListener {
            analyticsEventHandler.logEvent(AnalyticsEvent.CreateGifClicked(binding.pgnInput.text.toString()))
            if (binding.pgnInput.text.isNullOrBlank()) {
                errorMessageHandler.showError(getString(R.string.please_enter_pgn))
            } else {
                processPgnFile(
                    binding.pgnInput.text.toString().toFile(
                        "game.pgn",
                        this.applicationContext
                    )
                )
            }
        }

        binding.loadPgn.setOnClickListener {
            analyticsEventHandler.logEvent(AnalyticsEvent.ImportPgnClicked)
            selectPgnFileFromSystem()
        }

        binding.image.setOnClickListener {
            analyticsEventHandler.logEvent(AnalyticsEvent.GifImageClicked)

            val drawable: Drawable? = (it as ImageView).drawable
            if (drawable is Animatable) {
                val animatable = (drawable as Animatable)
                if (animatable.isRunning) {
                    analyticsEventHandler.logEvent(AnalyticsEvent.GifImageAnimationPaused)
                    animatable.stop()
                } else {
                    analyticsEventHandler.logEvent(AnalyticsEvent.GifImageAnimationStarted)
                    animatable.start()
                }
            }
        }
        binding.saveGif.setOnClickListener {
            analyticsEventHandler.logEvent(AnalyticsEvent.ExportPgnClicked(binding.pgnInput.text.toString()))
            currentFilePath?.let {
                shareCurrentGif()
            } ?: run {
                errorMessageHandler.showError(getString(R.string.load_in_a_gif))
            }
        }
        handleFromSystemIntent()
    }

    private fun shareCurrentGif() {
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_STREAM,
                currentFilePath?.getStrictModeUri(this@MainActivity)
            )
            type = "image/gif"
        }
        startActivity(
            Intent.createChooser(
                shareIntent,
                resources.getText(R.string.share)
            )
        )
    }

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
            val firstItem = if (clipData.itemCount > 0) {
                clipData.getItemAt(0)
            } else {
                null
            }
            if (firstItem != null) {
                processPgnFile(
                    firstItem.text.toString().toFile(
                        "game.pgn",
                        this.applicationContext
                    )
                )
            }
        }
    }

    private fun processPgnFile(pgnFile: File) {
        try {
            analyticsEventHandler.logEvent(AnalyticsEvent.ProcessingPgnFile)
            val pgn = PgnHolder(
                pgnFile.absolutePath
            )
            pgn.loadPgn()
            if (pgn.games.firstOrNull() != null) {
                binding.pgnInput.setText(pgn.toString())
            }
            job?.cancel()
            job = lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.isVisible = true
                }
                val game = pgn.games.firstOrNull()
                if (game == null) {
                    errorMessageHandler.showError(getString(R.string.current_pgn_does_not_contain_any_game))
                } else {
                    currentFilePath = pgnToGifConverter.createGifFileFromChessGame(
                        game,
                        settingsStorage.getSettings()
                    )
                }

                withContext(Dispatchers.Main) {
                    binding.progressBar.isVisible = false
                    if (currentFilePath != null) {
                        analyticsEventHandler.logEvent(AnalyticsEvent.LoadingPgnFileToView)
                        Glide.with(this@MainActivity).load(currentFilePath).into(binding.image)
                    }
                }
            }
        } catch (ex: Exception) {
            ErrorHandler.logException(ex)
            ErrorHandler.logInfo("Failed to parse pgn with value ${binding.pgnInput.text.toString()}")
            errorMessageHandler.showError(getString(R.string.unable_to_generate_gif))
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings_menu_item) {
            analyticsEventHandler.logEvent(AnalyticsEvent.SettingsClicked)
            startActivity(
                Intent(this, SettingsActivity::class.java),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            )
        }
        return super.onOptionsItemSelected(item)
    }


    private fun selectPgnFileFromSystem() {
        val filePickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "application/vnd.chess-pgn",
                    "application/x-chess-pgn"
                )
            )
        }
        getContent.launch(filePickerIntent)
    }

    private fun handleIntent(data: Uri) {
        val selectedFile = data.uriToFile(this.applicationContext)
        if (selectedFile != null) {
            processPgnFile(selectedFile)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}