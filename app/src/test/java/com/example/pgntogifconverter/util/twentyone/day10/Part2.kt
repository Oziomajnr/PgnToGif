package com.example.pgntogifconverter.util.twentyone.day10

import org.junit.Test
import java.io.File
import java.util.*

class Part2 {
    private val fileInput =
        File("/Users/oziomaogbe/AndroidStudioProjects/PgnToGifConverter/app/src/test/java/com/example/pgntogifconverter/util/twentyone/day10/input.txt").readLines()


    private fun solvePart2() {
        val openingBrackets = setOf('(', '[', '{', '<')

        val openingToClosingBrackets = mapOf(')' to '(', ']' to '[', '}' to '{', '>' to '<')
        val inputStack = fileInput.map {
            val stack = Stack<Char>()
            it.forEach { x ->
                if (openingBrackets.contains(x)) {
                    stack.push(x)
                } else {
                    if (stack.peek() == openingToClosingBrackets[x]) {
                        stack.pop()
                    } else {
                        return@map Stack()
                    }
                }
            }
            stack
        }.filter {
            it.isNotEmpty()
        }

        val sumList = inputStack.map {
            it.map { character ->
                when (character) {
                    '(' -> 1L
                    '[' -> 2L
                    '{' -> 3L
                    '<' -> 4L
                    else -> {
                        error("")
                    }
                }
            }.reversed().reduce { acc, point ->
                (acc * 5) + point
            }
        }

        println(sumList.sorted()[sumList.size / 2])
    }

    @Test
    fun test() {
        solvePart2()
    }
}