package com.chunkymonkey.pgntogifconverter.resource

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.chunkymonkey.pgntogifconverter.R
import com.chunkymonkey.pgntogifconverter.data.SettingsStorage

class PaintResourceProvider(
    private val context: Context, private val settingsStorage: SettingsStorage
) {
    fun getBlackSquarePaint(): Paint {
        return Paint().apply {
            color = ContextCompat.getColor(
                context,
                getDrawableResourceId("dark_square_color_${settingsStorage.getSettings().boardStyle.name}")
            )
        }
    }

    fun getWhiteSquarePaint(): Paint {
        return Paint().apply {
            color = ContextCompat.getColor(
                context,
                getDrawableResourceId("light_square_color_${settingsStorage.getSettings().boardStyle.name}")
            )
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

    @SuppressLint("DiscouragedApi")
    private fun getDrawableResourceId(resourceName: String): Int {
        return context.resources.getIdentifier(
            resourceName, "color", context.packageName
        )
    }
}