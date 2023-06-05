package com.chunkymonkey.pgntogifconverter.data

sealed class BoardStyle(open val name: String) {
    object Default : BoardStyle("default")
    object Blue : BoardStyle("blue")
    object Purple : BoardStyle("purple")
    object IC : BoardStyle("ic")
    object Green : BoardStyle("green")
}