package com.chunkymonkey.pgntogifconverter.ui

import android.content.Context
import android.content.Intent
import com.chunkymonkey.pgntogifconverter.R
import com.chunkymonkey.pgntogifconverter.util.extention.getStrictModeUri
import java.io.File


object DefaultNavigator {
     fun shareCurrentGif(file: File, context: Context) {
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_STREAM, file.getStrictModeUri(context)
            )
            type = "image/gif"
        }
        context.startActivity(
            Intent.createChooser(
                shareIntent, context.resources.getText(R.string.share)
            )
        )
    }

}