package com.chunkymonkey.pgntogifconverter.util

import com.github.bhlangonijr.chesslib.Square

fun getCoordinateFromSquare(square: Square): Pair<Int, Int> {
    val array = square.value().toCharArray()
    val x = array[0].code
    val y = array[1].digitToInt()

    return Pair(x - 'A'.code, y - 1)
}

fun getCoordinateFromSquareWithFlippedBoard(square: Square): Pair<Int, Int> {
    val array = square.value().toCharArray()
    val x = array[0].code
    val y = array[1].digitToInt()

    return Pair('H'.code - x, 8 - y)
}