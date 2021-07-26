package com.example.pgntogifconverter.util

import com.github.bhlangonijr.chesslib.Square
import junit.framework.TestCase

class ChessBoardUtilKtTest : TestCase() {

    fun testGetCoordinateFromSquare() {
        assertEquals(Pair(0,0), getCoordinateFromSquare(Square.A1))
        assertEquals(Pair(0,1), getCoordinateFromSquare(Square.A2))
        assertEquals(Pair(0,2), getCoordinateFromSquare(Square.A3))
        assertEquals(Pair(0,3), getCoordinateFromSquare(Square.A4))
        assertEquals(Pair(7,7), getCoordinateFromSquare(Square.H8))
        assertEquals(Pair(3,2), getCoordinateFromSquare(Square.D3))
        assertEquals(Pair(5,5), getCoordinateFromSquare(Square.F6))
    }
}