package com.example.pgntogifconverter

import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import com.example.pgntogifconverter.databinding.ActivityMainBinding
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import java.io.File
import com.example.pgntogifconverter.util.extention.uriToFile
import com.example.pgntogifconverter.util.extention.toFile
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.pgntogifconverter.converter.PgnToGifConverter
import com.example.pgntogifconverter.util.MediaStoreUtils
import com.github.bhlangonijr.chesslib.pgn.PgnHolder
import java.lang.Exception


class MainActivity : BaseActivity<ActivityMainBinding>() {

    private var currentFilePath: File? = null

    override val layout = R.layout.activity_main
    val pgnToGifConverter: PgnToGifConverter by lazy {
        PgnToGifConverter(this.applicationContext)
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult: ActivityResult ->
            activityResult.data?.data?.let { resultUri -> handleIntent(resultUri) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.createGifButton.setOnClickListener {
            if (binding.pgnInput.text.isNullOrBlank()) {
                Toast.makeText(this, getString(R.string.please_enter_pgn), Toast.LENGTH_LONG).show()
            } else {
                try {
                    processPgnFile(
                        binding.pgnInput.text.toString().toFile(
                            "game.pgn",
                            this.applicationContext
                        )
                    )
                    if (currentFilePath != null) {
                        Glide.with(this).load(currentFilePath).into(binding.image)
                    }
                } catch (ex: Exception) {
                    Toast.makeText(this, getString(R.string.unable_to_generate_gif), Toast.LENGTH_LONG).show()
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
                MediaStoreUtils.storeImage(this.applicationContext, it, "image/gif")
            }
        }
        handleFromSystemIntent()
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
        currentFilePath = pgnToGifConverter.createGifFileFromPgn(
            pgn
        )
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
}