package com.example.pgntogifconverter

import android.content.Context
import com.chunkymonkey.pgntogifconverter.R
import com.chunkymonkey.pgntogifconverter.ui.ApplicationText
import com.chunkymonkey.pgntogifconverter.ui.error.ApplicationStringProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test


class ApplicationStringProviderImplTest {
    private val stringProvider: ApplicationStringProvider = FakeApplicationStringProvider()

    @Before
    fun setup() {

    }

    @Test
    fun getErrorMessage_withPleaseLoadInPgnText_returnsCorrectString() {
        val applicationText = ApplicationText.PLEASE_LOAD_IN_PGN

        val errorMessage = stringProvider.getErrorMessage(applicationText)

        assertEquals("PLEASE_LOAD_IN_PGN", errorMessage)
    }

    @Test
    fun getErrorMessage_withUnableToGenerateGifText_returnsCorrectString() {
        val applicationText = ApplicationText.UNABLE_TO_GENERATE_GIF

        val errorMessage = stringProvider.getErrorMessage(applicationText)

        assertEquals("UNABLE_TO_GENERATE_GIF", errorMessage)
    }

    @Test
    fun getErrorMessage_withCurrentPgnDoesNotContainAnyGameText_returnsCorrectString() {
        val applicationText = ApplicationText.CURRENT_PGN_DOES_NOT_CONTAIN_ANY_GAME

        val errorMessage = stringProvider.getErrorMessage(applicationText)

        assertEquals("CURRENT_PGN_DOES_NOT_CONTAIN_ANY_GAME", errorMessage)
    }

    @Test
    fun getErrorMessage_withPLEASE_LOAD_IN_GIF_returnsCorrectString() {
        val applicationText = ApplicationText.PLEASE_LOAD_IN_GIF

        val errorMessage = stringProvider.getErrorMessage(applicationText)

        assertEquals("PLEASE_LOAD_IN_GIF", errorMessage)
    }
}
