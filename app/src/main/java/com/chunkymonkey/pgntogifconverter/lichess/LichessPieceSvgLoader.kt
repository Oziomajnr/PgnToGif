package com.chunkymonkey.pgntogifconverter.lichess

import android.graphics.Bitmap
import android.graphics.Canvas
import com.caverock.androidsvg.SVG
import java.io.File
import java.io.FileInputStream

object LichessPieceSvgLoader {

    fun svgFileToBitmap(svgFile: File, sizePx: Int): Bitmap? {
        if (!svgFile.isFile || sizePx <= 0) return null
        return runCatching {
            FileInputStream(svgFile).use { stream ->
                val svg = SVG.getFromInputStream(stream)
                svg.setDocumentWidth(sizePx.toFloat())
                svg.setDocumentHeight(sizePx.toFloat())
                val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                svg.renderToCanvas(canvas)
                bitmap
            }
        }.getOrNull()
    }
}
