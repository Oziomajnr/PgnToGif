package com.example.pgntogifconverter.util.twentyone.day8

import org.junit.Test
import java.io.File

class Part1 {
    private val fileInput =
        File("/Users/oziomaogbe/AndroidStudioProjects/PgnToGifConverter/app/src/test/java/com/example/pgntogifconverter/util/twentyone/day8/input.txt").readLines()


    fun solvePart1(): Int {
        return  fileInput.map {
            it.split(" | ")[1]
        }.map {
            it.split(" ")
        }.map { value ->
            value.map {
                it.length
            }.filter {
                it in listOf(2, 4, 3, 7)
            }
        }.flatten().count()

    }

    @Test
    fun test() {
        println(solvePart1())
    }
}