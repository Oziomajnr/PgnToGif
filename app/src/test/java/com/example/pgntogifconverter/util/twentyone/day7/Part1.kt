package com.example.pgntogifconverter.util.twentyone.day7

import org.junit.Test
import java.io.File
import kotlin.math.abs

class Part1 {
    private val fileInput =
        File("/Users/oziomaogbe/AndroidStudioProjects/PgnToGifConverter/app/src/test/java/com/example/pgntogifconverter/util/twentyone/day7/input.txt").readLines()
            .first().split(",")
            .map {
                it.toInt()
            }

    private fun solvePart1() {
        val max = fileInput.maxOrNull()!!
        val groupedInput = fileInput.groupBy {
            it
        }.mapValues {
            it.value.count()
        }

      val  costForPosition =  (0..max).map{
           it to groupedInput.map { (i, count) ->
                abs(i - it) * count
            }   .sum()
        }

        println(costForPosition.minByOrNull { it.second }!!)
    }

    @Test
    fun test() {
        solvePart1()
    }
}