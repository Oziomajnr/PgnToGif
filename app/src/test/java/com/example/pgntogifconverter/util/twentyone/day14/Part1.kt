package com.example.pgntogifconverter.util.twentyone.day14

import org.junit.Test
import java.io.File

class Part1 {
    private val fileInput =
        File("/Users/oziomaogbe/AndroidStudioProjects/PgnToGifConverter/app/src/test/java/com/example/pgntogifconverter/util/twentyone/day14/input.txt").readLines()
            .map {
                val pairString = it.split(" -> ")
                pairString[0] to pairString[1].toCharArray().first()
            }.toMap()

    val cache = mutableMapOf<String, Int>()

//    fun solvePart1(input: String): String {
//        val pair = input[0].toString() + input[1]
//        val result =  fileInput[pair]?.let {
//            (input[0].toString() + it + input[1])
//        } ?: input
//
//        return result
//    }

    fun solvePart1(input: String, index: Int = 1): String {
        if (index > 25) return input
        var currentIndex = 1
        val result = input.toCharArray().toMutableList()
        for (x in 0 until input.lastIndex) {
            val pair = input[x].toString() + input[x + 1]
            fileInput[pair]?.let {
                result.add(currentIndex, it)
                currentIndex += 2
            }
        }
        return solvePart1(result.toCharArray().concatToString(), index + 1)
    }


    @Test
    fun test() {
        val result1 = solvePart1("NN").groupBy {
            it
        }.mapValues {
            it.value.size
        }
//        val result2 = solvePart1("NC").groupBy {
//            it
//        }.mapValues {
//            it.value.size
//        }
//        val result3 = solvePart1("CB").groupBy {
//            it
//        }.mapValues {
//            it.value.size
//        }


        println(getMax(result1))
//        println(fileInput.values.groupBy { it }.mapValues {
//            it.value.size
//        })
    }

    fun getMax(map: Map<Char, Int>): Int {
        val max = map.maxOf {
            it.value
        }

        return max
    }
}
//{K=5, B=10, V=10, S=12, N=12, H=12, P=13, C=6, F=11, O=9}
