package com.chunkymonkey.pgntogifconverter.ui.error

import com.chunkymonkey.pgntogifconverter.ui.ApplicationText


interface ErrorMessageProvider {
    fun getErrorMessage(applicationText: ApplicationText): String
}