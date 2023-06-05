package com.chunkymonkey.pgntogifconverter.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class PieceSet(open val name: String) : Parcelable {
    object Default : PieceSet("default")
    object Pirouetti : PieceSet("pirouetti")
    object California : PieceSet("california")
    object Spatial : PieceSet("spatial")
    object Letter : PieceSet("letter")
}

