package com.example.pgntogifconverter.util.extention

import android.content.Context
import java.io.File


fun String.toFile(fileName: String, applicationContext: Context): File {
    applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE).use {
        it.write(this.toByteArray())
        it.close()
    }
    return File(applicationContext.filesDir, fileName)
}