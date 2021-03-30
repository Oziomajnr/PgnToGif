package com.example.imagetogifconverter.util.extention

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.net.toFile
import java.io.*

fun Uri.uriToFile(context: Context): File? {
    try {
        if (this.scheme == "file") {
            return this.toFile()
        } else {
            val inputStream =
                context.contentResolver.openInputStream(this) ?: return null
            return copyInputStreamToFile(
                inputStream,
                context,
                this.resolveFileName(context.contentResolver) ?: ""
            )
        }
    } catch (ex: Exception) {
        Log.e(this::class.java.name, "Unable to load file with url $this ", ex)
    }
    return null
}

private fun copyInputStreamToFile(
    inputStream: InputStream,
    context: Context,
    fileName: String
): File {
    val file = File(context.cacheDir, fileName)
    var outputStream: OutputStream? = null
    try {
        outputStream = FileOutputStream(file)
        val buf = ByteArray(10024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) {
            outputStream.write(buf, 0, len)
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    } finally {
        // Ensure that the InputStreams are closed even if there's an exception.
        try {
            outputStream?.close()

            // If you want to close the "in" InputStream yourself then remove this
            // from here but ensure that you close it yourself eventually.
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return file
}

fun Uri.resolveFileName(resolver: ContentResolver): String? {
    var returnCursor: Cursor? = null
    try {
        returnCursor = resolver.query(
            this,
            null, null, null, null
        ) ?: return null
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        return returnCursor.getString(nameIndex)
    } catch (ex: java.lang.Exception) {

    } finally {
        returnCursor?.close()
    }
    return null
}