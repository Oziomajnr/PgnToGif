package com.chunkymonkey.pgntogifconverter.ui.home


import kotlinx.coroutines.*
import java.io.File

interface HomePresenter {
    fun processPgnFile(pgnFile: File)
    fun initializeView(view: HomeView, coroutineScope: CoroutineScope)
    fun onDestroy()
}





