package com.example.pgntogifconverter

import com.chunkymonkey.pgntogifconverter.ui.ApplicationText
import com.chunkymonkey.pgntogifconverter.ui.error.ApplicationStringProvider

class FakeApplicationStringProvider : ApplicationStringProvider {
    override fun getErrorMessage(applicationText: ApplicationText): String {
        return when (applicationText) {
            ApplicationText.CURRENT_PGN_DOES_NOT_CONTAIN_ANY_GAME -> "CURRENT_PGN_DOES_NOT_CONTAIN_ANY_GAME"
            ApplicationText.PLEASE_LOAD_IN_PGN -> "PLEASE_LOAD_IN_PGN"
            ApplicationText.UNABLE_TO_GENERATE_GIF -> "UNABLE_TO_GENERATE_GIF"
        }
    }

}