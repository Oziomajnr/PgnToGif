package com.example.pgntogifconverter.lichess

import com.chunkymonkey.pgntogifconverter.data.BoardStyle
import com.chunkymonkey.pgntogifconverter.data.PieceSet
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Documents the persisted string shape for Lichess selections (must match [PreferenceSettingsStorage]).
 */
class LichessPreferenceKeyTest {

    @Test
    fun lichessPieceSet_name_usesPrefix() {
        assertEquals("lichess:cburnett", PieceSet.Lichess("cburnett").name)
    }

    @Test
    fun lichessBoardStyle_name_usesPrefix() {
        assertEquals("lichess:blue-marble", BoardStyle.Lichess("blue-marble").name)
    }
}
