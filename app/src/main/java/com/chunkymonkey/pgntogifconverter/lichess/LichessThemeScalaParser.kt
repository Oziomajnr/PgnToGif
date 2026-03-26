package com.chunkymonkey.pgntogifconverter.lichess

/**
 * Parses 2D board theme ids from lila [Theme.scala] (the [Theme] object, before [Theme3d]).
 */
object LichessThemeScalaParser {

    private val themeRegex = Regex("""Theme\("([^"]+)"""")

    fun parseTwoDimensionalThemeIds(themeScalaSource: String): List<String> {
        val slice = themeScalaSource.substringBefore("object Theme3d")
        return themeRegex.findAll(slice).map { it.groupValues[1] }.distinct().toList()
    }
}
