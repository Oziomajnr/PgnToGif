package com.example.pgntogifconverter

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chunkymonkey.pgntogifconverter.data.HighlightStyle
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HighlightStyleUnitTest {

    @Test
    fun green_baseColor_matchesExpectedRGB() {
        assertEquals(Color.rgb(0x9B, 0xC7, 0x00), HighlightStyle.Green.baseColor)
    }

    @Test
    fun yellow_baseColor_matchesExpectedRGB() {
        assertEquals(Color.rgb(0xFF, 0xEB, 0x3B), HighlightStyle.Yellow.baseColor)
    }

    @Test
    fun blue_baseColor_matchesExpectedRGB() {
        assertEquals(Color.rgb(0x42, 0xA5, 0xF5), HighlightStyle.Blue.baseColor)
    }

    @Test
    fun red_baseColor_matchesExpectedRGB() {
        assertEquals(Color.rgb(0xEF, 0x53, 0x50), HighlightStyle.Red.baseColor)
    }

    @Test
    fun orange_baseColor_matchesExpectedRGB() {
        assertEquals(Color.rgb(0xFF, 0x98, 0x00), HighlightStyle.Orange.baseColor)
    }

    @Test
    fun custom_baseColor_returnsProvidedArgb() {
        val argb = Color.rgb(0x12, 0x34, 0x56)
        val style = HighlightStyle.Custom(argb)
        assertEquals(argb, style.baseColor)
        assertEquals("custom", style.name)
    }

    @Test
    fun fromColorAlpha_appliesAlphaCorrectly() {
        val style = HighlightStyle.Green
        val withAlpha = style.fromColorAlpha(102)
        assertEquals(102, Color.alpha(withAlpha))
        assertEquals(Color.red(style.baseColor), Color.red(withAlpha))
        assertEquals(Color.green(style.baseColor), Color.green(withAlpha))
        assertEquals(Color.blue(style.baseColor), Color.blue(withAlpha))
    }

    @Test
    fun presetStyles_haveDifferentBaseColors() {
        val colors = listOf(
            HighlightStyle.Green.baseColor,
            HighlightStyle.Yellow.baseColor,
            HighlightStyle.Blue.baseColor,
            HighlightStyle.Red.baseColor,
            HighlightStyle.Orange.baseColor,
        )
        assertEquals("All preset colors should be unique", colors.size, colors.distinct().size)
    }
}
