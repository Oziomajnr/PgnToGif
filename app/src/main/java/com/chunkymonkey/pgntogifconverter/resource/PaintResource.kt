package com.example.pgntogifconverter.resource

import android.content.Context
import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.chunkymonkey.pgntogifconverter.R

class PaintResource(context: Context) {
    val blackSquarePaint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(context, R.color.dark_square_color)
        }
    }
    val whiteSquarePaint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(context, R.color.light_square_color)
        }
    }

    val kingAttackedPaint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(context, R.color.kind_attached_colour)
        }
    }

    val highlightedSquarePaint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(context, R.color.highlighted_square_paint)
        }
    }
}