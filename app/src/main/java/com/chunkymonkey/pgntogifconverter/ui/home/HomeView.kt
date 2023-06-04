package com.chunkymonkey.pgntogifconverter.ui.home

import java.io.File

interface HomeView {
    fun setPgnText(text: String)
    fun updateProgressBarVisibility(isVisible: Boolean)
    fun showErrorMessage(message: String)
    fun getCurrentPgnText(): String
    fun displayGifFromFile(currentFilePath: File)
}