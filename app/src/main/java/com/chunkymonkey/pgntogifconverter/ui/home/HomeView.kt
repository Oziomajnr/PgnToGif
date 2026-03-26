package com.chunkymonkey.pgntogifconverter.ui.home

import android.graphics.Bitmap
import java.io.File

interface HomeView {
    fun setPgnText(text: String)
    fun updateProgressBarVisibility(isVisible: Boolean)
    fun updateLoadingStatus(status: String?)
    fun showErrorMessage(message: String)
    fun getCurrentPgnText(): String
    fun displayGifFromFile(currentFilePath: File)
    fun shareCurrentGif(file: File)
    fun shareMp4(file: File)
    fun displayMoveList(moves: List<MoveData>)
    fun displayBoardAtPosition(bitmap: Bitmap)
    fun displayMp4File(file: File)
}
