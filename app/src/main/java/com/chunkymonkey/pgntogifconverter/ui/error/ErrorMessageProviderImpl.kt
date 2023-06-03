package com.chunkymonkey.pgntogifconverter.ui.error

import android.content.Context
import com.chunkymonkey.pgntogifconverter.R
import com.chunkymonkey.pgntogifconverter.ui.ApplicationText

class ErrorMessageProviderImpl(private val applicationContext: Context) : ErrorMessageProvider {
    override fun getErrorMessage(applicationText: ApplicationText): String {
        return when (applicationText) {
            ApplicationText.PLEASE_LOAD_IN_PGN -> applicationContext.getString(R.string.please_enter_pgn)
            ApplicationText.UNABLE_TO_GENERATE_GIF -> applicationContext.getString(R.string.unable_to_generate_gif)
            ApplicationText.CURRENT_PGN_DOES_NOT_CONTAIN_ANY_GAME -> applicationContext.getString(R.string.current_pgn_does_not_contain_any_game)
        }
    }
}