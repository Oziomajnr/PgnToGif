package com.example.pgntogifconverter.lichess

import com.chunkymonkey.pgntogifconverter.lichess.LichessThemeCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LichessThemeCatalogTest {

    @Test
    fun getBoardTheme_blueMarble_returnsLightAndDark() {
        val t = LichessThemeCatalog.getBoardTheme("blue-marble")
        assertNotNull(t)
        assertEquals("blue-marble", t!!.id)
        assertTrue(t.lightArgb != 0)
        assertTrue(t.darkArgb != 0)
    }

    @Test
    fun pieceFamilies_containsCburnett() {
        val ids = LichessThemeCatalog.pieceFamilies().map { it.id }
        assertTrue(ids.contains("cburnett"))
    }

    @Test
    fun boardThemes_containsBrown_embeddedFallback() {
        val ids = LichessThemeCatalog.boardThemes().map { it.id }
        assertTrue(ids.contains("brown"))
    }
}
