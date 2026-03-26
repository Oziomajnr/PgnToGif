package com.example.pgntogifconverter

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.chunkymonkey.pgntogifconverter.data.HighlightStyle
import com.chunkymonkey.pgntogifconverter.data.PreferenceSettingsStorage
import com.chunkymonkey.pgntogifconverter.preference.PreferenceService
import com.chunkymonkey.pgntogifconverter.resource.PaintResourceProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HighlightStyleInstrumentedTest {

    private lateinit var preferenceService: PreferenceService
    private lateinit var settingsStorage: PreferenceSettingsStorage

    private val appContext get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        preferenceService = PreferenceService(appContext)
        settingsStorage = PreferenceSettingsStorage(preferenceService)
        settingsStorage.saveSettings(
            settingsStorage.getSettings().copy(highlightStyle = HighlightStyle.Green)
        )
    }

    @Test
    fun highlightStyle_afterReset_isGreen() {
        val settings = settingsStorage.getSettings()
        assertEquals(HighlightStyle.Green, settings.highlightStyle)
    }

    @Test
    fun highlightStyle_saveAndLoadPreset_roundTrips() {
        settingsStorage.saveSettings(
            settingsStorage.getSettings().copy(highlightStyle = HighlightStyle.Blue)
        )
        val loaded = settingsStorage.getSettings()
        assertEquals(HighlightStyle.Blue, loaded.highlightStyle)
    }

    @Test
    fun highlightStyle_saveAndLoadCustom_roundTrips() {
        val customColor = 0xFF123456.toInt()
        settingsStorage.saveSettings(
            settingsStorage.getSettings().copy(highlightStyle = HighlightStyle.Custom(customColor))
        )
        val loaded = settingsStorage.getSettings()
        val loadedStyle = loaded.highlightStyle
        assertEquals(true, loadedStyle is HighlightStyle.Custom)
        assertEquals(customColor, (loadedStyle as HighlightStyle.Custom).argb)
    }

    @Test
    fun highlightStyle_changingFromGreenToRed_updatesHighlightPaintColor() {
        settingsStorage.saveSettings(
            settingsStorage.getSettings().copy(highlightStyle = HighlightStyle.Green)
        )
        val greenFromColor = PaintResourceProvider(appContext, settingsStorage).highlightFromPaint.color
        val greenToColor = PaintResourceProvider(appContext, settingsStorage).highlightToPaint.color

        settingsStorage.saveSettings(
            settingsStorage.getSettings().copy(highlightStyle = HighlightStyle.Red)
        )
        val redFromColor = PaintResourceProvider(appContext, settingsStorage).highlightFromPaint.color
        val redToColor = PaintResourceProvider(appContext, settingsStorage).highlightToPaint.color

        assertNotEquals("From paint should differ between Green and Red", greenFromColor, redFromColor)
        assertNotEquals("To paint should differ between Green and Red", greenToColor, redToColor)
    }

    @Test
    fun highlightStyle_changingFromGreenToBlue_updatesHighlightPaintColor() {
        settingsStorage.saveSettings(
            settingsStorage.getSettings().copy(highlightStyle = HighlightStyle.Green)
        )
        val greenColor = PaintResourceProvider(appContext, settingsStorage).highlightFromPaint.color

        settingsStorage.saveSettings(
            settingsStorage.getSettings().copy(highlightStyle = HighlightStyle.Blue)
        )
        val blueColor = PaintResourceProvider(appContext, settingsStorage).highlightFromPaint.color

        assertNotEquals("Highlight from paint should differ between Green and Blue", greenColor, blueColor)
    }

    @Test
    fun highlightStyle_customColor_reflectedInPaint() {
        val customArgb = 0xFFFF0000.toInt()
        settingsStorage.saveSettings(
            settingsStorage.getSettings().copy(highlightStyle = HighlightStyle.Custom(customArgb))
        )
        val paint = PaintResourceProvider(appContext, settingsStorage)
        val fromColor = paint.highlightFromPaint.color
        val toColor = paint.highlightToPaint.color

        assertEquals(android.graphics.Color.red(customArgb), android.graphics.Color.red(fromColor))
        assertEquals(android.graphics.Color.green(customArgb), android.graphics.Color.green(fromColor))
        assertEquals(android.graphics.Color.blue(customArgb), android.graphics.Color.blue(fromColor))

        assertEquals(android.graphics.Color.red(customArgb), android.graphics.Color.red(toColor))
    }
}
