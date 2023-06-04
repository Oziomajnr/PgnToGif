package com.example.pgntogifconverter

import android.content.Context
import com.chunkymonkey.pgntogifconverter.R
import com.chunkymonkey.pgntogifconverter.ui.ApplicationText
import com.chunkymonkey.pgntogifconverter.ui.error.ApplicationStringProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test


class ApplicationStringProviderImplTest {
    private lateinit var context: Context
    private val stringProvider: ApplicationStringProvider = FakeApplicationStringProvider()

    @Before
    fun setup() {

    }

    @Test
    fun getErrorMessage_withPleaseLoadInPgnText_returnsCorrectString() {
        val applicationText = ApplicationText.PLEASE_LOAD_IN_PGN
        val expectedString = context.getString(R.string.please_enter_pgn)

        val errorMessage = stringProvider.getErrorMessage(applicationText)

        assertEquals(expectedString, errorMessage)
    }

    @Test
    fun getErrorMessage_withUnableToGenerateGifText_returnsCorrectString() {
        val applicationText = ApplicationText.UNABLE_TO_GENERATE_GIF
        val expectedString = context.getString(R.string.unable_to_generate_gif)

        val errorMessage = stringProvider.getErrorMessage(applicationText)

        assertEquals(expectedString, errorMessage)
    }

    @Test
    fun getErrorMessage_withCurrentPgnDoesNotContainAnyGameText_returnsCorrectString() {
        val applicationText = ApplicationText.CURRENT_PGN_DOES_NOT_CONTAIN_ANY_GAME
        val expectedString = context.getString(R.string.current_pgn_does_not_contain_any_game)

        val errorMessage = stringProvider.getErrorMessage(applicationText)

        assertEquals(expectedString, errorMessage)
    }
}
