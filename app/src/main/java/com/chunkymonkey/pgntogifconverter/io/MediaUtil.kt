package com.chunkymonkey.pgntogifconverter.io

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileNotFoundException


class MediaStoreUtils {
    companion object {
        fun storeImage(context: Context, srcFile: File, mime: String?): Uri? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val dstFile =
                    File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), srcFile.name)
                val succeed: Boolean = FileUtils.copyFile(srcFile, dstFile)
                if (succeed) {
                    val values = ContentValues()
                    values.put(MediaStore.Images.Media.DATA, dstFile.absolutePath)
                    values.put(MediaStore.Images.Media.MIME_TYPE, mime)
                    val uri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                    val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    intent.data = uri
                    context.sendBroadcast(intent)
                    return Uri.fromFile(dstFile)
                }
            } else {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DISPLAY_NAME, getDisplayName(srcFile))
                values.put(MediaStore.Images.Media.MIME_TYPE, mime)
                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                val resolver = context.contentResolver
                val insertUri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (insertUri != null) {
                    try {
                        val outputStream = resolver.openOutputStream(insertUri)!!
                        val succeed: Boolean = FileUtils.copyFile(srcFile, outputStream)
                        if (succeed) {
                            return insertUri
                        }
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }
            return null
        }

        private fun getDisplayName(file: File): String {
            val fileName = file.name.substring(0, file.name.lastIndexOf("."))
            val fileType = file.name.substring(file.name.lastIndexOf("."))
            return fileName + "_" + System.currentTimeMillis() + fileType
        }
    }
}