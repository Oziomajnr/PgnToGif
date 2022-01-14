package com.chunkymonkey.pgntogifconverter

import android.app.Application

class PgnToGifApplication : Application() {

    override fun onCreate() {
        application = this
        super.onCreate()
    }

    companion object {
        lateinit var application: Application
            private set
    }
}