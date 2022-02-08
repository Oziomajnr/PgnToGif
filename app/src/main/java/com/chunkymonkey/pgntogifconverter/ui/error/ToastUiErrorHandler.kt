package com.chunkymonkey.pgntogifconverter.ui.error

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.chunkymonkey.pgntogifconverter.util.ErrorHandler
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport

class ToastUiErrorHandler(val context: Activity) : UiErrorHandler {

    override fun showError(errorMessage: String) {
        try {
            context.runOnUiThread {
                Toast.makeText(
                    context,
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (ex: Exception) {
            ErrorHandler.logException(ex)
        }
    }
}