package com.chunkymonkey.pgntogifconverter.ui.home

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
import com.chunkymonkey.pgntogifconverter.ui.BaseActivity
import com.chunkymonkey.pgntogifconverter.ui.DefaultNavigator
import com.chunkymonkey.pgntogifconverter.ui.error.ApplicationStringProvider
import com.chunkymonkey.pgntogifconverter.ui.error.ApplicationStringProviderImpl
import com.chunkymonkey.pgntogifconverter.ui.error.ToastUiErrorHandler
import com.chunkymonkey.pgntogifconverter.ui.error.UiErrorHandler
import com.chunkymonkey.pgntogifconverter.ui.settings.SettingsActivity
import com.chunkymonkey.pgntogifconverter.util.ErrorHandler
import com.chunkymonkey.pgntogifconverter.util.extention.getStrictModeUri
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory


class HomeActivity : BaseActivity<ActivityMainBinding>(), HomeView {


    private val analyticsEventHandler: AnalyticsEventHandler =
        DependencyFactory.getAnalyticsEventHandler()

    private var currentFilePath: File? = null
    private val errorMessageHandler: UiErrorHandler by lazy {
        ToastUiErrorHandler(this)
    }
    private val settingsStorage: SettingsStorage by lazy {
        PreferenceSettingsStorage(PreferenceService(this.applicationContext))
    }

    private val applicationStringProvider: ApplicationStringProvider by lazy {
        ApplicationStringProviderImpl(this.applicationContext)
    }

    override val layout = R.layout.activity_main
    private val pgnToGifConverter: PgnToGifConverter by lazy {
        PgnToGifConverter(this.application, DependencyFactory.getPlayerNameHelper())
    }
    private val homePresenter: HomePresenter by lazy {
        HomePresenterImpl(
            analyticsEventHandler, applicationStringProvider, pgnToGifConverter, settingsStorage
        )
    }
    val reviewManager by lazy { ReviewManagerFactory.create(this.applicationContext) }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult: ActivityResult ->
            activityResult.data?.data?.let { resultUri -> handleIntent(resultUri) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsEventHandler.initialize()
        homePresenter.initializeView(this, lifecycleScope)
        binding.createGifButton.setOnClickListener {
            analyticsEventHandler.logEvent(AnalyticsEvent.CreateGifClicked(binding.pgnInput.text.toString()))
            if (binding.pgnInput.text.isNullOrBlank()) {
                errorMessageHandler.showError(getString(R.string.please_enter_pgn))
            } else {
                homePresenter.processPgnFile(
                    getCurrentPgnText().toFile(
                        "game.pgn", this.applicationContext
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
            homePresenter.shareCurrentGif()
        }
        handleFromSystemIntent()
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = reviewManager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener {

                }
            } else {
                // There was some problem, log or handle the error code.
                task.exception?.let {
                    ErrorHandler.logException(it)
                    ErrorHandler.logInfo("Review Task failed with code ${(task.exception as ReviewException).errorCode}")
                }

            }
        }
    }

    override fun shareCurrentGif(file: File) {
        DefaultNavigator.shareCurrentGif(file, this)
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
                homePresenter.processPgnFile(
                    firstItem.text.toString().toFile(
                        "game.pgn", this.applicationContext
                    )
                )
            }
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
                    "application/vnd.chess-pgn", "application/x-chess-pgn"
                )
            )
        }
        getContent.launch(filePickerIntent)
    }

    private fun handleIntent(data: Uri) {
        val selectedFile = data.uriToFile(this.applicationContext)
        if (selectedFile != null) {
            homePresenter.processPgnFile(selectedFile)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        homePresenter.onDestroy()
    }

    override fun setPgnText(text: String) {
        binding.pgnInput.setText(text)
    }

    override fun updateProgressBarVisibility(isVisible: Boolean) {
        binding.progressBar.isVisible = isVisible
    }

    override fun showErrorMessage(message: String) {
        errorMessageHandler.showError(message)
    }

    override fun getCurrentPgnText(): String {
        return binding.pgnInput.text.toString()
    }

    override fun displayGifFromFile(currentFilePath: File) {
        Glide.with(this@HomeActivity).load(currentFilePath).into(binding.image)
    }
}