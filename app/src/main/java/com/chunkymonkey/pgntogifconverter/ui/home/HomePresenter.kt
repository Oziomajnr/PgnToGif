package com.chunkymonkey.pgntogifconverter.ui.home

import kotlinx.coroutines.CoroutineScope
import java.io.File

interface HomePresenter {
    fun processPgnFile(pgnFile: File)
    fun initializeView(view: HomeView, coroutineScope: CoroutineScope)
    fun onDestroy()
    fun shareCurrentGif()
    fun shareMp4()
    fun navigateToMove(index: Int)
    fun generateGif()
    fun generateGifFromMove(startIndex: Int)
    fun generateMp4()
    fun generateMp4FromMove(startIndex: Int)
    fun getParsedMoves(): List<MoveData>
    fun getCurrentGame(): com.chunkymonkey.chesscore.ParsedGame?
}
