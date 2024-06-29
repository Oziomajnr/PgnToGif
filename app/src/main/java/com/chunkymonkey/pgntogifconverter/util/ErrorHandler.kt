package com.chunkymonkey.pgntogifconverter.util

import android.util.Log
import com.chunkymonkey.pgntogifconverter.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics

class ErrorHandler {
    companion object {
        fun logException(exception: Throwable) {
            if (BuildConfig.DEBUG) {
                exception.printStackTrace()
            } else {
                FirebaseCrashlytics.getInstance().recordException(exception)
            }
        }

        fun logInfo(message: String) {
            if (BuildConfig.DEBUG) {
                Log.d("Log Info", message)
            }
            FirebaseCrashlytics.getInstance().log(message)
        }
    }
}