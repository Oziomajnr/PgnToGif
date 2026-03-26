package com.example.pgntogifconverter.ui.home

import com.chunkymonkey.pgntogifconverter.ui.home.MoveData
import com.chunkymonkey.chesscore.Move
import com.chunkymonkey.chesscore.Square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MoveDataTest {

    private fun moveData(index: Int, san: String = "e4") = MoveData(
        index = index,
        san = san,
        move = Move(Square.E2, Square.E4),
        isWhite = index % 2 == 0
    )

    @Test
    fun moveNumber_firstMove_returnsOne() {
        assertEquals(1, moveData(0).moveNumber)
    }

    @Test
    fun moveNumber_secondMove_returnsOne() {
        assertEquals(1, moveData(1).moveNumber)
    }

    @Test
    fun moveNumber_thirdMove_returnsTwo() {
        assertEquals(2, moveData(2).moveNumber)
    }

    @Test
    fun moveNumber_fourthMove_returnsTwo() {
        assertEquals(2, moveData(3).moveNumber)
    }

    @Test
    fun moveNumber_move10_returnsSix() {
        assertEquals(6, moveData(10).moveNumber)
    }

    @Test
    fun isWhite_evenIndex_true() {
        assertTrue(moveData(0).isWhite)
        assertTrue(moveData(2).isWhite)
        assertTrue(moveData(4).isWhite)
    }

    @Test
    fun isWhite_oddIndex_false() {
        assertFalse(moveData(1).isWhite)
        assertFalse(moveData(3).isWhite)
        assertFalse(moveData(5).isWhite)
    }

    @Test
    fun san_preservesValue() {
        val md = moveData(0, "Nf3")
        assertEquals("Nf3", md.san)
    }
}
