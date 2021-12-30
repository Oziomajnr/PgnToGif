package com.example.pgntogifconverter.util.twentyone.day25

import androidx.compose.ui.graphics.vector.EmptyPath
import org.junit.Test
import java.io.File

class Part1 {
    private val locations =
        File("/Users/oziomaogbe/AndroidStudioProjects/PgnToGifConverter/app/src/test/java/com/example/pgntogifconverter/util/twentyone/day25/input.txt").readLines()
            .map {
                it.map {
                    when (it) {
                        '>' -> {
                            EastFacing
                        }
                        'v' -> {
                            SouthFacing
                        }
                        '.' -> {
                            None
                        }
                        else -> {
                            error("Invalid input")
                        }
                    }
                }
            }

    @Test
    fun testPart1() {
        locations.forEach {
            it.forEach {
                when (it) {
                    EastFacing -> print(">")
                    None -> print(".")
                    SouthFacing -> print("v")
                }
            }
            println()
        }
        println()
        println(solve(locations.map { it.toMutableList() }.toMutableList(), 0))
    }

    fun solve(input: MutableList<MutableList<Location>>, numberOfSteps: Int): Int {
        val copyOfInput = input.map {
            it.map {
                it
            }
        }
        val seen = mutableSetOf<Pair<Int, Int>>()
        for (x in 0..input.lastIndex) {
            for (y in 0..input.first().lastIndex) {
                if (!seen.contains(Pair(x, y))) {
                    if (input[x][y] == EastFacing) {
                        if (y == input.first().lastIndex && input[x][0] == None) {
                            input[x][y] = None
                            input[x][0] = EastFacing
                        } else if (y + 1 <= input.first().lastIndex && input[x][y + 1] == None) {
                            input[x][y] = None
                            input[x][y + 1] = EastFacing
                            seen.add(Pair(x, y + 1))
                        }
                    }
                }

            }
        }
        for (x in 0..input.lastIndex) {
            for (y in 0..input.first().lastIndex) {
                if (input[x][y] == SouthFacing) {
                    if (!seen.contains(Pair(x, y))) {
                        if (x == input.lastIndex && input[0][y] == None) {
                            input[x][y] = None
                            input[0][y] = SouthFacing
                        } else if (x + 1 <= input.lastIndex && input[x + 1][y] == None) {
                            input[x][y] = None
                            input[x + 1][y] = SouthFacing
                        }
                        seen.add(Pair(x + 1, y))
                    }
                }
            }
        }
        input.forEach {
            it.forEach {
                when (it) {
                    EastFacing -> print(">")
                    None -> print(".")
                    SouthFacing -> print("v")
                }
            }
            println()
        }
        println()
        if (copyOfInput == input) return numberOfSteps
        return solve(input, numberOfSteps + 1)
    }

}

object EastFacing : Location
object SouthFacing : Location
object None : Location

sealed interface Location