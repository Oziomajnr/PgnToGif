package com.example.pgntogifconverter.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


fun String.toFile(fileName: String, applicationContext: Context): File {
    applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE).use {
        it.write(this.toByteArray())
        it.close()
    }
    return File(applicationContext.filesDir, fileName)
}

private fun saveMediaFile2(
    applicationContext: Context,
    filePath: String?,
    isVideo: Boolean,
    fileName: String,
    mimeType: String
) {
    filePath?.let {
        val context = applicationContext
        val values = ContentValues().apply {
            val folderName = if (isVideo) {
                Environment.DIRECTORY_MOVIES
            } else {
                Environment.DIRECTORY_PICTURES
            }
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(
                MediaStore.Images.Media.MIME_TYPE,
                mimeType
            )
        }

        val collection = if (isVideo) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }
        val fileUri = context.contentResolver.insert(collection, values)

        fileUri?.let {
            if (isVideo) {
                context.contentResolver.openFileDescriptor(fileUri, "w").use { descriptor ->
                    descriptor?.let {
                        FileOutputStream(descriptor.fileDescriptor).use { out ->
                            val videoFile = File(filePath)
                            FileInputStream(videoFile).use { inputStream ->
                                val buf = ByteArray(8192)
                                while (true) {
                                    val sz = inputStream.read(buf)
                                    if (sz <= 0) break
                                    out.write(buf, 0, sz)
                                }
                            }
                        }
                    }
                }
            } else {
                context.contentResolver.openOutputStream(fileUri).use { out ->
                    val bmOptions = BitmapFactory.Options()
                    val bmp = BitmapFactory.decodeFile(filePath, bmOptions)
                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    bmp.recycle()
                }
            }
            values.clear()
            values.put(
                if (isVideo) MediaStore.Video.Media.IS_PENDING else MediaStore.Images.Media.IS_PENDING,
                0
            )
            context.contentResolver.update(fileUri, values, null, null)
        }
    }
}