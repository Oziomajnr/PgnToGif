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
        private fun generateGIF(context: Context): ByteArray {
            val drawableResources = listOf(
                R.drawable.ic_search_black,
                R.drawable.ic_send,
                R.drawable.ic_settings,
                R.drawable.ic_share,
                R.drawable.ic_skip_next,
                R.drawable.ic_sleep_timer
            )

            val bitmaps = mutableListOf<Bitmap>()
            drawableResources.forEach {
                val bitmapDrawable = ContextCompat.getDrawable(context, it)
                bitmapDrawable?.toBitmap()?.let { it1 -> bitmaps.add(it1) }
            }

            val bos = ByteArrayOutputStream()
            val encoder = AnimatedGifEncoder()
            encoder.setDelay(2000)
            encoder.start(bos)
            for (bitmap in bitmaps) {
                encoder.addFrame(bitmap)
            }
            encoder.finish()
            return bos.toByteArray()
        }

        fun saveGif(context: Context) {
            context.contentResolver.insert()

            val contentValue = ContentValues()
            val outStream =
                FileOutputStream(Environment.getExternalStorageDirectory().absolutePath + "/generated_gif" + Date().time + ".gif")
            outStream.write(generateGIF(context))
            outStream.close()

        }
    }
}