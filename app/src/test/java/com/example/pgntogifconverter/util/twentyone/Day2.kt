//package com.example.pgntogifconverter.util.twentyone
//
//import org.junit.Test
//import java.io.File
//
//class Day2 {
//    private val input =
//        File("/Users/oziomaogbe/AndroidStudioProjects/PgnToGifConverter/app/src/test/java/com/example/pgntogifconverter/util/twentyone/input.txt").readLines()
//            .map {
//                val line = it.split(' ')
//                val instruction = when (val instructionString = line.first()) {
//                    "down" -> {
//                        Down(instructionString)
//                    }
//                    "up" -> {
//                        Up(instructionString)
//                    }
//                    "forward" -> {
//                        Forward(instructionString)
//                    }
//                    else -> {
//                        error("Invalid instruction")
//                    }
//                }
//                Input(instruction, line[1].toLong())
//            }
//
//    private fun part1(): Long {
//        val totalY = input.map {
//            when (it.instruction) {
//                is Down -> {
//                    it.value
//                }
//                is Up -> {
//                    -it.value
//                }
//                else -> {
//                    0L
//                }
//            }
//        }.sum()
//
//        val totalX =
//            input.map {
//                when (it.instruction) {
//                    is Forward -> {
//                        it.value
//                    }
//                    else -> {
//                        0L
//                    }
//                }
//            }.sum()
//
//        return totalX * totalY
//    }
//
//    private fun part2(): Long {
//
//        val totalAim = input.map {
//            when (it.instruction) {
//                is Down -> {
//                    Input(Aim("aim"), it.value)
//                }
//                is Up -> {
//                    Input(Aim("aim"), -it.value)
//                }
//                is Forward -> {
//                    Input(Forward("forward"), -it.value)
//                }
//                else -> {
//                    error("invalid input")
//                }
//            }
//        }.chu
//        val totalX = input.map {
//            when (it.instruction) {
//                is Forward -> {
//                    it.value
//                }
//                else -> {
//                    0L
//                }
//            }
//        }.sum()
//
//        val totalY = input.map {
//            when (it.instruction) {
//                is Forward -> {
//                    it.value
//                }
//                else -> {
//                    0L
//                }
//
//            }
//        }.sum() * totalAim
//
//        return totalX * totalY
//    }
//
//    @Test
//    fun test() {
//        println(part1())
//        println(part2())
//    }
//}
//
//data class Input(val instruction: Instruction, val value: Long)
//
//sealed interface Instruction
//class Forward(val value: String) : Instruction
//class Down(val value: String) : Instruction
//class Up(val value: String) : Instruction
//class Aim(val value: String) : Instruction