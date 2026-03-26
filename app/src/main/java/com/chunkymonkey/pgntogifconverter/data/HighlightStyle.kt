package com.chunkymonkey.pgntogifconverter.data

import android.graphics.Color

sealed class HighlightStyle(open val name: String) {
    abstract val baseColor: Int

    object Green : HighlightStyle("green") {
        override val baseColor: Int = Color.rgb(0x9B, 0xC7, 0x00)
    }

    object Yellow : HighlightStyle("yellow") {
        override val baseColor: Int = Color.rgb(0xFF, 0xEB, 0x3B)
    }

    object Blue : HighlightStyle("blue") {
        override val baseColor: Int = Color.rgb(0x42, 0xA5, 0xF5)
    }

    object Red : HighlightStyle("red") {
        override val baseColor: Int = Color.rgb(0xEF, 0x53, 0x50)
    }

    object Orange : HighlightStyle("orange") {
        override val baseColor: Int = Color.rgb(0xFF, 0x98, 0x00)
    }

    data class Custom(val argb: Int) : HighlightStyle("custom") {
        override val baseColor: Int get() = argb
    }

    fun fromColorAlpha(alpha: Int): Int {
        return Color.argb(
            alpha,
            Color.red(baseColor),
            Color.green(baseColor),
            Color.blue(baseColor)
        )
    }
}
