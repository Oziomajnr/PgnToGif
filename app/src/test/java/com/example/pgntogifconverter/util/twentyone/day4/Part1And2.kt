package com.example.pgntogifconverter.util.twentyone.day4

import org.junit.Test
import java.io.File

class Day4 {
    private val fileInput =
        File("/Users/oziomaogbe/AndroidStudioProjects/PgnToGifConverter/app/src/test/java/com/example/pgntogifconverter/util/twentyone/day4/input.txt").readText()
            .split(',').map {
                it.toInt()
            }
    private val boardsInput =
        File("/Users/oziomaogbe/AndroidStudioProjects/PgnToGifConverter/app/src/test/java/com/example/pgntogifconverter/util/twentyone/day4/board.txt").readLines()
            .filter { line ->
                line.isNotBlank()
            }.chunked(5).map { chunk ->
                chunk.map { string ->
                    string.split(' ').filter {
                        it.isNotBlank()
                    }.map {
                        it.toInt()
                    }.map {
                        Square(it, false)
                    }
                }
            }

    private fun solvePart1(currentIndex: Int, input: List<List<List<Square>>>): Int {
        val markedInput = markNumbersInSquare(fileInput[currentIndex], input)
        val winner = findWinner(markedInput)
        return if (winner != null) {
            getBoardSum(winner) * fileInput[currentIndex]
        } else {
            solvePart1(currentIndex + 1, markedInput)
        }
    }

    private fun solvePart2(
        currentIndex: Int,
        input: List<List<List<Square>>>,
        latestWinner: List<List<Square>>?
    ): Int {
        if (currentIndex > fileInput.lastIndex) {
            return getBoardSum(latestWinner!!) * fileInput[currentIndex - 1]
        }
        val markedInput = markNumbersInSquare(fileInput[currentIndex], input)
        val currentWinner = markedInput.lastOrNull {
            isWinner(it)
        }
        val nextInput = markedInput.filter {
            !isWinner(it)
        }
        if (nextInput.isEmpty()) {
            return getBoardSum(currentWinner!!) * fileInput[currentIndex]
        }
        return solvePart2(currentIndex + 1, nextInput, currentWinner)
    }

    private fun findWinner(input: List<List<List<Square>>>): List<List<Square>>? {
        input.forEach {
            if (isWinner(it)) {
                return it
            }
        }
        return null
    }

    private fun getBoardSum(board: List<List<Square>>): Int {
        return board.flatMap {
            it.filter { square ->
                !square.isMarked
            }.map { markedSquare ->
                markedSquare.value
            }
        }.sum()
    }

    private fun isWinner(input: List<List<Square>>): Boolean {
        val inputAsMarked = input.map {
            it.map {
                it.isMarked
            }
        }
        val matchesForHorizontal = inputAsMarked.map {
            it.none {
                !it
            }
        }.any {
            it
        }



//        for (x in 0..inputAsMarked.lastIndex) {
//            for (y in 0..inputAsMarked.first().lastIndex) {
//                inputAsMarked.reduce { acc, list ->
//                    listOf(acc, listOf(list[y])).flatten()
//                }.filter {
//
//                }
//            }
//        }
        return matchesForHorizontal || matchesForHorizontal

    }

    private fun markNumbersInSquare(
        markedValue: Int,
        input: List<List<List<Square>>>
    ): List<List<List<Square>>> {
        return input.map {
            it.map {
                it.map {
                    if (it.value == markedValue) {
                        it.copy(isMarked = true)
                    } else {
                        it
                    }
                }
            }
        }
    }

    @Test
    fun test() {
        solvePart2(0, boardsInput, null)
        println(solvePart1(0, boardsInput))
    }
}

data class Square(val value: Int, val isMarked: Boolean = false)

