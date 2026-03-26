package com.chunkymonkey.pgntogifconverter.data

sealed class BoardStyle(open val name: String) {
    object Default : BoardStyle("default")
    object Blue : BoardStyle("blue")
    object Purple : BoardStyle("purple")
    object IC : BoardStyle("ic")
    object Green : BoardStyle("green")
    object Maple : BoardStyle("maple")
    object Wood : BoardStyle("wood")
    object Canvas : BoardStyle("canvas")
    object Metal : BoardStyle("metal")

    /** Lichess-style theme id (colors from catalog / installed metadata). */
    data class Lichess(val themeId: String) : BoardStyle("lichess:$themeId")
}
