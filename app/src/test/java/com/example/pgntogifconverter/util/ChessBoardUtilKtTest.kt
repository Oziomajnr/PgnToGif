package com.example.pgntogifconverter.util

import com.chunkymonkey.pgntogifconverter.util.getCoordinateFromSquare
import com.chunkymonkey.pgntogifconverter.util.getCoordinateFromSquareWithFlippedBoard
import com.github.bhlangonijr.chesslib.Square
import org.junit.Assert.assertEquals
import org.junit.Test

class ChessBoardUtilKtTest {

    @Test
    fun testGetCoordinateFromSquare() {
        assertEquals(Pair(0, 0), getCoordinateFromSquare(Square.A1))
        assertEquals(Pair(0, 1), getCoordinateFromSquare(Square.A2))
        assertEquals(Pair(0, 2), getCoordinateFromSquare(Square.A3))
        assertEquals(Pair(0, 3), getCoordinateFromSquare(Square.A4))
        assertEquals(Pair(0, 4), getCoordinateFromSquare(Square.A5))
        assertEquals(Pair(0, 5), getCoordinateFromSquare(Square.A6))
        assertEquals(Pair(0, 6), getCoordinateFromSquare(Square.A7))
        assertEquals(Pair(0, 7), getCoordinateFromSquare(Square.A8))
        assertEquals(Pair(1, 0), getCoordinateFromSquare(Square.B1))
        assertEquals(Pair(1, 1), getCoordinateFromSquare(Square.B2))
        assertEquals(Pair(1, 2), getCoordinateFromSquare(Square.B3))
        assertEquals(Pair(1, 3), getCoordinateFromSquare(Square.B4))
        assertEquals(Pair(1, 4), getCoordinateFromSquare(Square.B5))
        assertEquals(Pair(1, 5), getCoordinateFromSquare(Square.B6))
        assertEquals(Pair(1, 6), getCoordinateFromSquare(Square.B7))
        assertEquals(Pair(1, 7), getCoordinateFromSquare(Square.B8))
        assertEquals(Pair(7, 7), getCoordinateFromSquare(Square.H8))
        assertEquals(Pair(3, 2), getCoordinateFromSquare(Square.D3))
        assertEquals(Pair(5, 5), getCoordinateFromSquare(Square.F6))
        assertEquals(Pair(5, 5), getCoordinateFromSquare(Square.F6))
    }

    @Test
    fun testGetCoordinateFromSquareWithFlippedBoard() {
        assertEquals(Pair(0, 0), getCoordinateFromSquareWithFlippedBoard(Square.H8))
        assertEquals(Pair(7, 0), getCoordinateFromSquareWithFlippedBoard(Square.A8))
        assertEquals(Pair(0, 7), getCoordinateFromSquareWithFlippedBoard(Square.H1))
        assertEquals(Pair(7, 7), getCoordinateFromSquareWithFlippedBoard(Square.A1))
    }
}