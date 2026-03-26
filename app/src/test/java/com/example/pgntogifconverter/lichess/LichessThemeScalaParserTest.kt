package com.example.pgntogifconverter.lichess

import com.chunkymonkey.pgntogifconverter.lichess.LichessThemeScalaParser
import org.junit.Assert.assertTrue
import org.junit.Test

class LichessThemeScalaParserTest {

    @Test
    fun parseTwoDimensionalThemeIds_extractsIdsBeforeTheme3d() {
        val src = """
            object Theme extends ThemeObject:
             val all = List(
             Theme("brown", "brown.png", Featured.Yes),
             Theme("blue-marble", "blue-marble.jpg"),
             )
            object Theme3d extends ThemeObject:
             val all = List(
             Theme("Woodi", "Woodi.png"),
             )
        """.trimIndent()
        val ids = LichessThemeScalaParser.parseTwoDimensionalThemeIds(src)
        assertTrue(ids.contains("brown"))
        assertTrue(ids.contains("blue-marble"))
        assertTrue(ids.none { it == "Woodi" })
    }
}
