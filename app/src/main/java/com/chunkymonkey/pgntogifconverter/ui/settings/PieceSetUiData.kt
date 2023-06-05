package com.chunkymonkey.pgntogifconverter.ui.settings

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.chunkymonkey.pgntogifconverter.data.BoardStyle
import com.chunkymonkey.pgntogifconverter.data.PieceSet

data class PieceSetUiData(
    val pieceSet: PieceSet,
    @DrawableRes val resourceId: Int,
    val title: String
)

data class BoardStyleUiData(
    val boardStyle: BoardStyle,
    val title: String,
    @ColorRes val lightSquareColorResourceId: Int,
    @ColorRes val darkSquareColorResourceId: Int,
    @DrawableRes val drawable: Int
)
