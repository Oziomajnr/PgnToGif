package com.chunkymonkey.chesslibrarywrapper.aoc

import java.io.File


class Day1 {
    val input =
        File("/Users/oziomaogbe/AndroidStudioProjects/PgnToGifConverter/chesslibrarywrapper/src/main/java/com/chunkymonkey/chesslibrarywrapper/aoc/2021/2021/input.txt").readLines().map {
            it.toInt()
        }
    fun getNumberOfIncrements() {
        println(input.filterIndexed { index, value ->
            index > 1 && input[index-1] < input[index]
        }.size)
    }


}