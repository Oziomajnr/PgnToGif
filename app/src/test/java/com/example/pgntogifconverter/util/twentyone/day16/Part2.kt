//package com.example.pgntogifconverter.util.twentyone.day16
//
//import org.junit.Test
//import java.io.File
//import java.lang.Exception
//import java.util.*
//
//class Part2 {
//
//    private val binaryToDecimalMap = mapOf(
//        '0' to "0000",
//        '1' to "0001",
//        '2' to "0010",
//        '3' to "0011",
//        '4' to "0100",
//        '5' to "0101",
//        '6' to "0110",
//        '7' to "0111",
//        '8' to "1000",
//        '9' to "1001",
//        'A' to "1010",
//        'B' to "1011",
//        'C' to "1100",
//        'D' to "1101",
//        'E' to "1110",
//        'F' to "1111"
//    )
//    private val caves =
//        File("/Users/oziomaogbe/AndroidStudioProjects/PgnToGifConverter/app/src/test/java/com/example/pgntogifconverter/util/day16/input.txt").readText()
//            .map {
//                binaryToDecimalMap[it]!!
//            }.reduce { acc, s ->
//                acc + s
//            }
//
//
//    val packetInfoList = mutableListOf<PacketInfo>()
//
//    private val parentStack = Stack<Operator>()
//    private fun parsePack(
//        input: String,
//        currentIndex: Int,
//        endIndex: Int?,
//        subpPackets: Int?,
//        firstPacket: Boolean = false,
//
//        ): Int {
//        var index = currentIndex
//        val currentPacket: Operator? = if (parentStack.isNotEmpty()) parentStack.peek() else null
//        if (packetInfoList.isEmpty() && currentPacket != null) {
//            packetInfoList.add(currentPacket)
//        }
//        if (index <= input.lastIndex &&
//            ((endIndex != null && index <= endIndex)
//                    || (subpPackets != null && subpPackets > 0) || firstPacket)
//        ) {
//            val packetVersion = (input[index].toString() + input[++index] + input[++index]).toInt(2)
//            val packetTypeId =
//                (input[++index].toString() + input[++index] + input[++index]).toInt(2)
//            if (packetTypeId == 4) {
//                var literalPacket = ""
//                while (input[++index] != '0') {
//                    literalPacket += (input[++index].toString() + input[++index] + input[++index] + input[++index])
//                }
//                literalPacket += (input[++index].toString() + input[++index] + input[++index] + input[++index])
//                if (currentPacket == null) {
//                    if (!firstPacket) error("")
//                    packetInfoList.add(
//                        Literal(
//                            literalPacket.toLong(
//                                2
//                            ), packetVersion
//                        )
//                    )
//                } else {
//                    currentPacket.subPackets.add(
//                        Literal(
//                            literalPacket.toLong(
//                                2
//                            ), packetVersion
//                        )
//                    )
//                }
//                if (firstPacket) return index + 1
//                return parsePack(
//                    input,
//                    index + 1,
//                    endIndex,
//                    subpPackets?.minus(1),
//                    false
//                )
//            } else {
//                val lengthTypeId = input[++index]
//                if (lengthTypeId == '0') {
//                    var packet = ""
//                    for (x in 1..15) {
//                        packet += input[++index]
//                    }
//                    val lengthOfSubPacket = packet.toInt(2)
//                    val finalPacket = Operator(
//                        packetVersion,
//                        lengthOfSubPacket,
//                        packetTypeId,
//                        0
//                    )
//
//                    if (currentPacket == null) {
//                        packetInfoList.add(
//                            finalPacket
//                        )
//                    } else {
//                        currentPacket.subPackets.add(
//                            finalPacket
//                        )
//                    }
//                    parentStack.push(finalPacket)
//                    return parsePack(
//                        input,
//                        index + 1,
//                        lengthOfSubPacket - 1 + index,
//                        null,
//                        false
//                    )
//                } else {
//                    var packet = ""
//                    for (x in 1..11) {
//                        packet += input[++index]
//                    }
//                    val numberOfSubPackets = packet.toInt(2)
//                    val resulPacket = Operator(
//                        packetVersion,
//                        null,
//                        numberOfSubPackets,
//                        packetTypeId,
//                        1
//                    )
//                    parentStack.push(resulPacket)
//
//                    if (currentPacket == null) {
//                        packetInfoList.add(
//                            resulPacket
//                        )
//                    } else {
//                        currentPacket.subPackets.add(
//                            resulPacket
//                        )
//                    }
//
//                    return parsePack(input, index + 1, null, lengthOfSubPacket, false)
//                }
//            }
//        }
//        parentStack.pop()
//        return index
//    }
//
//
//    private fun solvePart1() {
//        try {
//            var index = parsePack(caves, 0, null, null, true)
//
//            while (index <= caves.lastIndex) {
//                index = parsePack(caves, index, null, null, true)
//            }
//        } catch (ex: Exception) {
////            throw  ex
//        }
//    }
//
//    private fun solvePart2(operator: Operator): Literal {
//        return evaluateExpression(operator, reduceSubPackets(operator.subPackets))
//    }
//
//    private fun reduceSubPackets(
//        subPackets: List<PacketInfo>
//    ): List<Literal> {
//        val literals = subPackets.filterIsInstance<Literal>()
//        val operators = subPackets.filterIsInstance<Operator>()
//
//        val result = literals + operators.map {
//            val reducedPackets = reduceSubPackets(it.subPackets)
//            evaluateExpression(it, reducedPackets)
//        }
//        return result
//    }
//
//    private fun evaluateExpression(operator: Operator, literals: List<Literal>): Literal {
//        return when (operator.packetTypeID) {
//            0 -> {
//                val result = Literal(literals.map {
//                    it.value
//                }.sum())
//                result
//            }
//            1 -> {
//
//                val result = Literal(literals.map { it.value }.reduce { acc, value ->
//                    (acc * value)
//                })
//                result
//            }
//            2 -> {
//                return literals.minByOrNull {
//                    it.value
//                }!!
//            }
//            3 -> {
//                return literals.maxByOrNull {
//                    it.value
//                }!!
//            }
//            5 -> {
//                val value = if (literals[0].value > literals[1].value) 1L else 0
//                Literal(value)
//            }
//            6 -> {
//                val value = if (literals[0].value < literals[1].value) 1L else 0
//                Literal(value)
//            }
//            7 -> {
//                val value = if (literals[0].value == literals[1].value) 1L else 0
//                Literal(value)
//            }
//            else -> {
//                error("invalidPacket")
//            }
//        }
//    }
//
//    var sum = 0
//
//    @Test
//    fun test() {
//
//        solvePart1()
//
////        packetInfoList.forEach {
////            println(it)
////        }
//
//        transverse(packetInfoList.first() as Operator)
//        println(sum)
//
//        println(solvePart2(packetInfoList.first() as Operator))
//    }
//
//    fun transverse(operator: Operator) {
//        if (operator.packetTypeID in 5..7) {
//            println(operator.subPackets.filterIsInstance<Literal>().size)
//        }
//        sum += operator.packetVersion + operator.subPackets.filterIsInstance<Literal>().map {
//            it.packetVersion
//        }.sum()
//
//        operator.subPackets.filterIsInstance<Operator>().map { it }.forEach {
//            transverse(it)
//        }
//    }
//
//    data class Operator(
//        override val packetVersion: Int,
//        val lengthOfSubPackets: Int,
//        val numberOfSubPackets: Int?,
//        override val packetTypeID: Int,
//        val mode: Int
//    ) : PacketInfo(packetTypeID, packetVersion) {
//        val subPackets = mutableListOf<PacketInfo>()
//        override val totalVersionNumber = packetVersion + subPackets.map { it.packetVersion }.sum()
//    }
//
//    data class Literal(
//        val value: Long,
//        override val packetVersion: Int = -1
//    ) : PacketInfo(4, packetVersion) {
//        override val totalVersionNumber: Int = packetVersion
//    }
//
//    sealed class PacketInfo(open val packetTypeID: Int, open val packetVersion: Int) {
//
//        abstract val totalVersionNumber: Int
//
//    }
//
//}
//
//
//
//
//
