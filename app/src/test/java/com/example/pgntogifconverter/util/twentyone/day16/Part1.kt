package com.example.pgntogifconverter.util.twentyone.day16

import org.junit.Test
import java.io.File
import java.lang.Exception

class Part1 {

    private val binaryToDecimalMap = mapOf(
        '0' to "0000",
        '1' to "0001",
        '2' to "0010",
        '3' to "0011",
        '4' to "0100",
        '5' to "0101",
        '6' to "0110",
        '7' to "0111",
        '8' to "1000",
        '9' to "1001",
        'A' to "1010",
        'B' to "1011",
        'C' to "1100",
        'D' to "1101",
        'E' to "1110",
        'F' to "1111"
    )
    private val caves =
        File("/Users/oziomaogbe/AndroidStudioProjects/PgnToGifConverter/app/src/test/java/com/example/pgntogifconverter/util/day16/input.txt").readText()
            .map {
                binaryToDecimalMap[it]!!
            }.reduce { acc, s ->
                acc + s
            }


    val packetInfoList = mutableListOf<PacketInfo>()

    private fun parsePack(
        input: String,
        currentIndex: Int,
        endIndex: Int?,
        subpPackets: Int?,
        firstPacket: Boolean = false
    ): Int {
        var index = currentIndex

        if (index <= input.lastIndex &&
            ((endIndex != null && index <= endIndex)
                    || (subpPackets != null && subpPackets > 0) || firstPacket)
        ) {
            val packetVersion = (input[index].toString() + input[++index] + input[++index]).toInt(2)
            val packetTypeId =
                (input[++index].toString() + input[++index] + input[++index]).toInt(2)
            if (packetTypeId == 4) {
                var literalPacket = ""
                while (input[++index] != '0') {
                    literalPacket += (input[++index].toString() + input[++index] + input[++index] + input[++index])
                }
                literalPacket += (input[++index].toString() + input[++index] + input[++index] + input[++index])
                packetInfoList.add(
                    Literal(
                        literalPacket.toLong(
                            2
                        ), packetVersion, packetTypeId
                    )
                )
                if (firstPacket) return index + 1
                return parsePack(input, index + 1, endIndex, subpPackets?.minus(1))
            } else {
                val lengthTypeId = input[++index]
                if (lengthTypeId == '0') {
                    var packet = ""
                    for (x in 1..15) {
                        packet += input[++index]
                    }
                    val lengthOfSubPacket = packet.toInt(2)
                    packetInfoList.add(Operator(packetVersion, lengthOfSubPacket, packetTypeId, 0))
                    return parsePack(input, index + 1, lengthOfSubPacket - 1 + index, null)
                } else {
                    var packet = ""
                    for (x in 1..11) {
                        packet += input[++index]
                    }
                    val lengthOfSubPacket = packet.toInt(2)
                    packetInfoList.add(Operator(packetVersion, lengthOfSubPacket, packetTypeId, 1))
                    return parsePack(input, index + 1, null, lengthOfSubPacket)
                }
            }
        }
        return index
    }


    private fun solvePart1() {
        try {
            var index = parsePack(caves, 0, null, null, true)
            while (index <= caves.lastIndex) {
//                while (caves[index] == '0'&& (index) % 4 != 0) {
//                    index++
//                    if (index > caves.lastIndex) {
//                        return
//                    }
//                }
//                if ((index + 1) % 4 != 0) {
//                    index -= (index + 1) % 4
//                }
//                while ((index + 1) % 4 != 0) {
//                    println(caves[index])
//                    index++
//                }
//                if (index > caves.lastIndex) {
//                    return
//                }
                index = parsePack(caves, index, null, null, true)
            }
        } catch (ex: Exception) {
//            throw  ex
        }
    }


    @Test
    fun test() {
//        println(caves.length)

        solvePart1()

        packetInfoList.forEach {
            println(it)
        }
        println(packetInfoList.map {
            it.packetTypeID
        }.sum())
    }
    data class Operator(
        override val packetVersion: Int,
        val lengthOfSubPackets: Int,
        override val packetTypeID: Int,
        val mode: Int
    ) : PacketInfo(packetVersion, packetTypeID)

    data class Literal(
        val value: Long,
        override val packetVersion: Int,
        override val packetTypeID: Int
    ) : PacketInfo(packetVersion, packetTypeID)

    sealed class PacketInfo(open val packetVersion: Int, open val packetTypeID: Int)

}





