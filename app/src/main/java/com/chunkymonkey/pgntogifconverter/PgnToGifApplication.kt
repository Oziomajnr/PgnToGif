package com.chunkymonkey.pgntogifconverter

import android.app.Application
import com.chunkymonkey.pgntogifconverter.lichess.LichessThemeCatalog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PgnToGifApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        application = this
        super.onCreate()
        LichessThemeCatalog.init(this)
        applicationScope.launch(Dispatchers.IO) {
            LichessThemeCatalog.syncIfStale()
        }
        // Debug builds use placeholder google-services.json; keep Crashlytics off to avoid noisy logcat.
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        }
    }

    companion object {
        lateinit var application: Application
            private set
    }
}