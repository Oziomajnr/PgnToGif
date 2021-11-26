package com.chunkymonkey.chesslibrarywrapper.aoc

class InputParser {

    fun toInput(input: String): List<Input> {
        val inputRanges = mutableListOf<Input>()
        input.split(": ")[1].split(" or ").forEach {
            val startAndEndRangeArray = it.split('-').map {
                it.toInt()
            }
            inputRanges.add(Input(startAndEndRangeArray[0], startAndEndRangeArray[1]))
        }
        return inputRanges
    }

    fun isValidNumber(number: Int, ranges: List<Input>) {

    }
}

data class Input(val start: Int, val end: Int) {}