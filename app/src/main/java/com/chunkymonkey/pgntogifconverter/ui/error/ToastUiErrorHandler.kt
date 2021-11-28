package com.chunkymonkey.pgntogifconverter.ui.error

import android.content.Context
import android.widget.Toast

class ToastUiErrorHandler(val context: Context): UiErrorHandler {
    override fun showError(errorMessage: String) {
        Toast.makeText(
            context,
            errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }
}