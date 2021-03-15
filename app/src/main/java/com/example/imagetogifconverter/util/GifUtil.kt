package com.example.imagetogifconverter.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import androidx.core.content.ContextCompat
import com.example.imagetogifconverter.R
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*


class GifUtil {
    companion object {
        private fun generateGIF(context: Context, bitmaps: List<Bitmap>): ByteArray {

            val bos = ByteArrayOutputStream()
            val encoder = AnimatedGifEncoder()
            encoder.setSize(980, 980)
            encoder.setDelay(1000)
            encoder.start(bos)
            for (bitmap in bitmaps) {
                encoder.addFrame(bitmap)
            }
            encoder.finish()
            return bos.toByteArray()
        }

        fun saveGif(context: Context, bitmaps: List<Bitmap>): String {
            val filePath =
                Environment.getExternalStorageDirectory().absolutePath + "/generated_gif" + Date().time + ".gif"
            val outStream =
                FileOutputStream(filePath)
            outStream.write(generateGIF(context, bitmaps))
            outStream.close()
            return filePath
        }
    }
}