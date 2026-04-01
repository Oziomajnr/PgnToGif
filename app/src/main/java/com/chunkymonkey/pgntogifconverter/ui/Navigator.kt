package com.chunkymonkey.pgntogifconverter.ui

import android.content.Context
import android.content.Intent
import com.chunkymonkey.pgntogifconverter.R
import com.chunkymonkey.pgntogifconverter.util.extention.getStrictModeUri
import java.io.File


object DefaultNavigator {
    /** @return true if the share sheet was started */
    fun shareCurrentGif(file: File, context: Context): Boolean {
        val uri = file.getStrictModeUri(context) ?: return false
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/gif"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(
                shareIntent, context.resources.getText(R.string.share)
            )
        )
        return true
    }

    /** @return true if the share sheet was started */
    fun shareMp4(file: File, context: Context): Boolean {
        val uri = file.getStrictModeUri(context) ?: return false
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(
                shareIntent, context.resources.getText(R.string.share)
            )
        )
        return true
    }
}