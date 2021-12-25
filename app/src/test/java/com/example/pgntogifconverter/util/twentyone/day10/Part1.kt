package com.example.pgntogifconverter.util.twentyone.day10

import org.junit.Test
import java.io.File
import java.util.*

class Part1 {
    private val fileInput =
        File("/Users/oziomaogbe/AndroidStudioProjects/PgnToGifConverter/app/src/test/java/com/example/pgntogifconverter/util/twentyone/day10/input.txt").readLines()


    private fun solvePart1() {
        val openingBrackets = setOf('(', '[', '{', '<')

        val closingToOpeningBracketMap = mapOf(')' to '(', ']' to '[', '}' to '{', '>' to '<')
        val illegalCharacters = fileInput.map {
            val stack = Stack<Char>()
            it.forEach { currentCharacter ->
                if (openingBrackets.contains(currentCharacter)) {
                    stack.push(currentCharacter)
                } else {
                    if (stack.peek() == closingToOpeningBracketMap[currentCharacter]) {
                        stack.pop()
                    } else {
                        return@map currentCharacter
                    }
                }
            }
            return@map ' '
        }.filter {
            it != ' '
        }

        println(illegalCharacters.groupBy {
            it
        }.mapValues {
            it.value.size
        }.map {
            when (it.key) {
                ')' -> 3 * it.value
                ']' -> 57 * it.value
                '}' -> 1197 * it.value
                '>' -> 25137 * it.value
                else -> {
                    error("")
                }
            }
        }.sum())
    }

    @Test
    fun test() {
        solvePart1()
    }
}

