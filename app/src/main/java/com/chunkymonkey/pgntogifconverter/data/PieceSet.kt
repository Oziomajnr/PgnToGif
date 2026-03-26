package com.chunkymonkey.pgntogifconverter.data

sealed class PieceSet(open val name: String) {
    object Default : PieceSet("default")
    object Pirouetti : PieceSet("pirouetti")
    object California : PieceSet("california")
    object Spatial : PieceSet("spatial")
    object Letter : PieceSet("letter")

    /** Lichess CDN piece family (SVGs under filesDir/lichess/pieces/{familyId}/). */
    data class Lichess(val familyId: String) : PieceSet("lichess:$familyId")
}



