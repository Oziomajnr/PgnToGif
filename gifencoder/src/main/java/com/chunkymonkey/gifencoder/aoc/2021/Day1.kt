package com.chunkymonkey.gifencoder.aoc.`2021`

import java.io.File


class Day1 {
    val input =
        File("input.txt").readLines().map {
            it.toInt()
        }
    fun getNumberOfIncrements() {
        println(input.filterIndexed { index, value ->
            index > 1 && input[index-1] < input[index]
        }.size)
    }

}