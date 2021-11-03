package com.example.pgntogifconverter.util.extention

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.chunkymonkey.pgntogifconverter.BuildConfig
import java.io.File


fun String.toFile(fileName: String, applicationContext: Context): File {
    applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE).use {
        it.write(this.toByteArray())
        it.close()
    }
    return File(applicationContext.filesDir, fileName)
}

fun File.getStrictModeUri(context: Context): Uri? {
    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.provider",
        this
    )
}