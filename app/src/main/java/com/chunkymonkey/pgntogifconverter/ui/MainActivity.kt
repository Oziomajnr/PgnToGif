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
import com.chunkymonkey.pgntogifconverter.R
import com.chunkymonkey.pgntogifconverter.databinding.ActivityMainBinding
import com.chunkymonkey.pgntogifconverter.converter.PgnToGifConverter
import com.chunkymonkey.pgntogifconverter.data.PreferenceSettingsStorage
import com.chunkymonkey.pgntogifconverter.data.SettingsStorage
import com.chunkymonkey.pgntogifconverter.preference.PreferenceService
import com.chunkymonkey.pgntogifconverter.ui.error.ToastUiErrorHandler
import com.chunkymonkey.pgntogifconverter.ui.error.UiErrorHandler
import com.chunkymonkey.pgntogifconverter.ui.settings.SettingsActivity
import com.chunkymonkey.pgntogifconverter.util.extention.getStrictModeUri
import com.github.bhlangonijr.chesslib.pgn.PgnHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception


class MainActivity : BaseActivity<ActivityMainBinding>() {

    private var currentFilePath: File? = null
    private val errorMessageHandler: UiErrorHandler by lazy {
        ToastUiErrorHandler(this)
    }
    private val settingsStorage: SettingsStorage by lazy {
        PreferenceSettingsStorage(PreferenceService(this.applicationContext))
    }

    override val layout = R.layout.activity_main
    val pgnToGifConverter: PgnToGifConverter by lazy {
        PgnToGifConverter(this.application)
    }

    var job: Job? = null

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult: ActivityResult ->
            activityResult.data?.data?.let { resultUri -> handleIntent(resultUri) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.createGifButton.setOnClickListener {
            throw  RuntimeException("Test Crash");
            if (binding.pgnInput.text.isNullOrBlank()) {
                errorMessageHandler.showError(getString(R.string.please_enter_pgn))
            } else {
                try {
                    processPgnFile(
                        binding.pgnInput.text.toString().toFile(
                            "game.pgn",
                            this.applicationContext
                        )
                    )

                } catch (ex: Exception) {
                    errorMessageHandler.showError(getString(R.string.unable_to_generate_gif))
                }
            }
        }

        binding.loadPgn.setOnClickListener {
            selectPgnFileFromSystem()
        }

        binding.image.setOnClickListener {
            val drawable: Drawable? = (it as ImageView).drawable
            if (drawable is Animatable) {
                val animatable = (drawable as Animatable)
                if (animatable.isRunning) {
                    animatable.stop()
                } else {
                    animatable.start()
                }

            }
        }
        binding.saveGif.setOnClickListener {
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
        val pgn = PgnHolder(
            pgnFile.absolutePath
        )
        pgn.loadPgn()
        binding.pgnInput.setText(pgn.toString())
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
                    Glide.with(this@MainActivity).load(currentFilePath).into(binding.image)
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings_menu_item) {
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