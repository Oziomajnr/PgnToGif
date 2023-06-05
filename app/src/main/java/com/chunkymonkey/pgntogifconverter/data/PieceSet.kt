package com.chunkymonkey.pgntogifconverter.data

sealed class PieceSet(open val name: String) {
    object Default : PieceSet("default")
    object Pirouetti : PieceSet("pirouetti")
    object California : PieceSet("california")
    object Spatial : PieceSet("spatial")
    object Letter : PieceSet("letter")
}



