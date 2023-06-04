package com.chunkymonkey.pgntogifconverter.ui.error

import com.chunkymonkey.pgntogifconverter.ui.ApplicationText


interface ApplicationStringProvider {
    fun getErrorMessage(applicationText: ApplicationText): String
}