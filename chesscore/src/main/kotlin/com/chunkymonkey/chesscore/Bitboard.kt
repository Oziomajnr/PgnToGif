@file:Suppress("ArrayInDataClass")

package com.chunkymonkey.chesscore

object Bitboard {

    const val lightSquares: Long = 0x55AA55AA55AA55AAL
    const val darkSquares: Long = -0x55AA55AA55AA55ABL // 0xAA55AA55AA55AA55L

    val rankBB = longArrayOf(
        0x00000000000000FFL, 0x000000000000FF00L, 0x0000000000FF0000L, 0x00000000FF000000L,
        0x000000FF00000000L, 0x0000FF0000000000L, 0x00FF000000000000L, -0x100000000000000L
    )

    val fileBB = longArrayOf(
        0x0101010101010101L, 0x0202020202020202L, 0x0404040404040404L, 0x0808080808080808L,
        0x1010101010101010L, 0x2020202020202020L, 0x4040404040404040L, -0x7F7F7F7F7F7F7F80L
    )

    private val bbTable = Array(64) { LongArray(64) }

    private val squareToDiagonalA1H8 = arrayOf(
        DiagonalA1H8.H8_A1, DiagonalA1H8.B1_H7, DiagonalA1H8.C1_H6, DiagonalA1H8.D1_H5,
        DiagonalA1H8.E1_H4, DiagonalA1H8.F1_H3, DiagonalA1H8.G1_H2, DiagonalA1H8.H1_H1,
        DiagonalA1H8.G8_A2, DiagonalA1H8.H8_A1, DiagonalA1H8.B1_H7, DiagonalA1H8.C1_H6,
        DiagonalA1H8.D1_H5, DiagonalA1H8.E1_H4, DiagonalA1H8.F1_H3, DiagonalA1H8.G1_H2,
        DiagonalA1H8.F8_A3, DiagonalA1H8.G8_A2, DiagonalA1H8.H8_A1, DiagonalA1H8.B1_H7,
        DiagonalA1H8.C1_H6, DiagonalA1H8.D1_H5, DiagonalA1H8.E1_H4, DiagonalA1H8.F1_H3,
        DiagonalA1H8.E8_A4, DiagonalA1H8.F8_A3, DiagonalA1H8.G8_A2, DiagonalA1H8.H8_A1,
        DiagonalA1H8.B1_H7, DiagonalA1H8.C1_H6, DiagonalA1H8.D1_H5, DiagonalA1H8.E1_H4,
        DiagonalA1H8.D8_A5, DiagonalA1H8.E8_A4, DiagonalA1H8.F8_A3, DiagonalA1H8.G8_A2,
        DiagonalA1H8.H8_A1, DiagonalA1H8.B1_H7, DiagonalA1H8.C1_H6, DiagonalA1H8.D1_H5,
        DiagonalA1H8.C8_A6, DiagonalA1H8.D8_A5, DiagonalA1H8.E8_A4, DiagonalA1H8.F8_A3,
        DiagonalA1H8.G8_A2, DiagonalA1H8.H8_A1, DiagonalA1H8.B1_H7, DiagonalA1H8.C1_H6,
        DiagonalA1H8.B8_A7, DiagonalA1H8.C8_A6, DiagonalA1H8.D8_A5, DiagonalA1H8.E8_A4,
        DiagonalA1H8.F8_A3, DiagonalA1H8.G8_A2, DiagonalA1H8.H8_A1, DiagonalA1H8.B1_H7,
        DiagonalA1H8.A8_A8, DiagonalA1H8.B8_A7, DiagonalA1H8.C8_A6, DiagonalA1H8.D8_A5,
        DiagonalA1H8.E8_A4, DiagonalA1H8.F8_A3, DiagonalA1H8.G8_A2, DiagonalA1H8.H8_A1
    )

    private val squareToDiagonalH1A8 = arrayOf(
        DiagonalH1A8.A1_A1, DiagonalH1A8.B1_A2, DiagonalH1A8.C1_A3, DiagonalH1A8.D1_A4,
        DiagonalH1A8.E1_A5, DiagonalH1A8.F1_A6, DiagonalH1A8.G1_A7, DiagonalH1A8.H1_A8,
        DiagonalH1A8.B1_A2, DiagonalH1A8.C1_A3, DiagonalH1A8.D1_A4, DiagonalH1A8.E1_A5,
        DiagonalH1A8.F1_A6, DiagonalH1A8.G1_A7, DiagonalH1A8.H1_A8, DiagonalH1A8.B8_H2,
        DiagonalH1A8.C1_A3, DiagonalH1A8.D1_A4, DiagonalH1A8.E1_A5, DiagonalH1A8.F1_A6,
        DiagonalH1A8.G1_A7, DiagonalH1A8.H1_A8, DiagonalH1A8.B8_H2, DiagonalH1A8.C8_H3,
        DiagonalH1A8.D1_A4, DiagonalH1A8.E1_A5, DiagonalH1A8.F1_A6, DiagonalH1A8.G1_A7,
        DiagonalH1A8.H1_A8, DiagonalH1A8.B8_H2, DiagonalH1A8.C8_H3, DiagonalH1A8.D8_H4,
        DiagonalH1A8.E1_A5, DiagonalH1A8.F1_A6, DiagonalH1A8.G1_A7, DiagonalH1A8.H1_A8,
        DiagonalH1A8.B8_H2, DiagonalH1A8.C8_H3, DiagonalH1A8.D8_H4, DiagonalH1A8.E8_H5,
        DiagonalH1A8.F1_A6, DiagonalH1A8.G1_A7, DiagonalH1A8.H1_A8, DiagonalH1A8.B8_H2,
        DiagonalH1A8.C8_H3, DiagonalH1A8.D8_H4, DiagonalH1A8.E8_H5, DiagonalH1A8.F8_H6,
        DiagonalH1A8.G1_A7, DiagonalH1A8.H1_A8, DiagonalH1A8.B8_H2, DiagonalH1A8.C8_H3,
        DiagonalH1A8.D8_H4, DiagonalH1A8.E8_H5, DiagonalH1A8.F8_H6, DiagonalH1A8.G8_H7,
        DiagonalH1A8.H1_A8, DiagonalH1A8.B8_H2, DiagonalH1A8.C8_H3, DiagonalH1A8.D8_H4,
        DiagonalH1A8.E8_H5, DiagonalH1A8.F8_H6, DiagonalH1A8.G8_H7, DiagonalH1A8.H8_H8
    )

    private fun sq2Bb(sq: Square): Long = sq.bb

    private val diagonalH1A8BB: LongArray by lazy {
        longArrayOf(
            sq2Bb(Square.A1),
            sq2Bb(Square.B1) or sq2Bb(Square.A2),
            sq2Bb(Square.C1) or sq2Bb(Square.B2) or sq2Bb(Square.A3),
            sq2Bb(Square.D1) or sq2Bb(Square.C2) or sq2Bb(Square.B3) or sq2Bb(Square.A4),
            sq2Bb(Square.E1) or sq2Bb(Square.D2) or sq2Bb(Square.C3) or sq2Bb(Square.B4) or sq2Bb(Square.A5),
            sq2Bb(Square.F1) or sq2Bb(Square.E2) or sq2Bb(Square.D3) or sq2Bb(Square.C4) or sq2Bb(Square.B5) or sq2Bb(Square.A6),
            sq2Bb(Square.G1) or sq2Bb(Square.F2) or sq2Bb(Square.E3) or sq2Bb(Square.D4) or sq2Bb(Square.C5) or sq2Bb(Square.B6) or sq2Bb(Square.A7),
            sq2Bb(Square.H1) or sq2Bb(Square.G2) or sq2Bb(Square.F3) or sq2Bb(Square.E4) or sq2Bb(Square.D5) or sq2Bb(Square.C6) or sq2Bb(Square.B7) or sq2Bb(Square.A8),
            sq2Bb(Square.B8) or sq2Bb(Square.C7) or sq2Bb(Square.D6) or sq2Bb(Square.E5) or sq2Bb(Square.F4) or sq2Bb(Square.G3) or sq2Bb(Square.H2),
            sq2Bb(Square.C8) or sq2Bb(Square.D7) or sq2Bb(Square.E6) or sq2Bb(Square.F5) or sq2Bb(Square.G4) or sq2Bb(Square.H3),
            sq2Bb(Square.D8) or sq2Bb(Square.E7) or sq2Bb(Square.F6) or sq2Bb(Square.G5) or sq2Bb(Square.H4),
            sq2Bb(Square.E8) or sq2Bb(Square.F7) or sq2Bb(Square.G6) or sq2Bb(Square.H5),
            sq2Bb(Square.F8) or sq2Bb(Square.G7) or sq2Bb(Square.H6),
            sq2Bb(Square.G8) or sq2Bb(Square.H7),
            sq2Bb(Square.H8)
        )
    }

    private val diagonalA1H8BB: LongArray by lazy {
        longArrayOf(
            sq2Bb(Square.A8),
            sq2Bb(Square.B8) or sq2Bb(Square.A7),
            sq2Bb(Square.C8) or sq2Bb(Square.B7) or sq2Bb(Square.A6),
            sq2Bb(Square.D8) or sq2Bb(Square.C7) or sq2Bb(Square.B6) or sq2Bb(Square.A5),
            sq2Bb(Square.E8) or sq2Bb(Square.D7) or sq2Bb(Square.C6) or sq2Bb(Square.B5) or sq2Bb(Square.A4),
            sq2Bb(Square.F8) or sq2Bb(Square.E7) or sq2Bb(Square.D6) or sq2Bb(Square.C5) or sq2Bb(Square.B4) or sq2Bb(Square.A3),
            sq2Bb(Square.G8) or sq2Bb(Square.F7) or sq2Bb(Square.E6) or sq2Bb(Square.D5) or sq2Bb(Square.C4) or sq2Bb(Square.B3) or sq2Bb(Square.A2),
            sq2Bb(Square.H8) or sq2Bb(Square.G7) or sq2Bb(Square.F6) or sq2Bb(Square.E5) or sq2Bb(Square.D4) or sq2Bb(Square.C3) or sq2Bb(Square.B2) or sq2Bb(Square.A1),
            sq2Bb(Square.B1) or sq2Bb(Square.C2) or sq2Bb(Square.D3) or sq2Bb(Square.E4) or sq2Bb(Square.F5) or sq2Bb(Square.G6) or sq2Bb(Square.H7),
            sq2Bb(Square.C1) or sq2Bb(Square.D2) or sq2Bb(Square.E3) or sq2Bb(Square.F4) or sq2Bb(Square.G5) or sq2Bb(Square.H6),
            sq2Bb(Square.D1) or sq2Bb(Square.E2) or sq2Bb(Square.F3) or sq2Bb(Square.G4) or sq2Bb(Square.H5),
            sq2Bb(Square.E1) or sq2Bb(Square.F2) or sq2Bb(Square.G3) or sq2Bb(Square.H4),
            sq2Bb(Square.F1) or sq2Bb(Square.G2) or sq2Bb(Square.H3),
            sq2Bb(Square.G1) or sq2Bb(Square.H2),
            sq2Bb(Square.H1)
        )
    }

    @Suppress("LongLine")
    val knightAttacks = longArrayOf(
        0x0000000000020400L, 0x0000000000050800L, 0x00000000000a1100L, 0x0000000000142200L, 0x0000000000284400L, 0x0000000000508800L, 0x0000000000a01000L, 0x0000000000402000L,
        0x0000000002040004L, 0x0000000005080008L, 0x000000000a110011L, 0x0000000014220022L, 0x0000000028440044L, 0x0000000050880088L, 0x00000000a0100010L, 0x0000000040200020L,
        0x0000000204000402L, 0x0000000508000805L, 0x0000000a1100110aL, 0x0000001422002214L, 0x0000002844004428L, 0x0000005088008850L, 0x000000a0100010a0L, 0x0000004020002040L,
        0x0000020400040200L, 0x0000050800080500L, 0x00000a1100110a00L, 0x0000142200221400L, 0x0000284400442800L, 0x0000508800885000L, 0x0000a0100010a000L, 0x0000402000204000L,
        0x0002040004020000L, 0x0005080008050000L, 0x000a1100110a0000L, 0x0014220022140000L, 0x0028440044280000L, 0x0050880088500000L, 0x00a0100010a00000L, 0x0040200020400000L,
        0x0204000402000000L, 0x0508000805000000L, 0x0a1100110a000000L, 0x1422002214000000L, 0x2844004428000000L, 0x5088008850000000L, 0xA0100010A0000000uL.toLong(), 0x4020002040000000L,
        0x0400040200000000L, 0x0800080500000000L, 0x1100110a00000000L, 0x2200221400000000L, 0x4400442800000000L, -0x77FF77B000000000L, 0x100010a000000000L, 0x2000204000000000L,
        0x0004020000000000L, 0x0008050000000000L, 0x00110a0000000000L, 0x0022140000000000L, 0x0044280000000000L, 0x0088500000000000L, 0x0010a00000000000L, 0x0020400000000000L
    )

    @Suppress("LongLine")
    val whitePawnAttacks = longArrayOf(
        0x0000000000000200L, 0x0000000000000500L, 0x0000000000000a00L, 0x0000000000001400L, 0x0000000000002800L, 0x0000000000005000L, 0x000000000000a000L, 0x0000000000004000L,
        0x0000000000020000L, 0x0000000000050000L, 0x00000000000a0000L, 0x0000000000140000L, 0x0000000000280000L, 0x0000000000500000L, 0x0000000000a00000L, 0x0000000000400000L,
        0x0000000002000000L, 0x0000000005000000L, 0x000000000a000000L, 0x0000000014000000L, 0x0000000028000000L, 0x0000000050000000L, 0x00000000a0000000L, 0x0000000040000000L,
        0x0000000200000000L, 0x0000000500000000L, 0x0000000a00000000L, 0x0000001400000000L, 0x0000002800000000L, 0x0000005000000000L, 0x000000a000000000L, 0x0000004000000000L,
        0x0000020000000000L, 0x0000050000000000L, 0x00000a0000000000L, 0x0000140000000000L, 0x0000280000000000L, 0x0000500000000000L, 0x0000a00000000000L, 0x0000400000000000L,
        0x0002000000000000L, 0x0005000000000000L, 0x000a000000000000L, 0x0014000000000000L, 0x0028000000000000L, 0x0050000000000000L, 0x00a0000000000000L, 0x0040000000000000L,
        0x0200000000000000L, 0x0500000000000000L, 0x0a00000000000000L, 0x1400000000000000L, 0x2800000000000000L, 0x5000000000000000L, -0x6000000000000000L, 0x4000000000000000L,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L
    )

    @Suppress("LongLine")
    val blackPawnAttacks = longArrayOf(
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000002L, 0x0000000000000005L, 0x000000000000000aL, 0x0000000000000014L, 0x0000000000000028L, 0x0000000000000050L, 0x00000000000000a0L, 0x0000000000000040L,
        0x0000000000000200L, 0x0000000000000500L, 0x0000000000000a00L, 0x0000000000001400L, 0x0000000000002800L, 0x0000000000005000L, 0x000000000000a000L, 0x0000000000004000L,
        0x0000000000020000L, 0x0000000000050000L, 0x00000000000a0000L, 0x0000000000140000L, 0x0000000000280000L, 0x0000000000500000L, 0x0000000000a00000L, 0x0000000000400000L,
        0x0000000002000000L, 0x0000000005000000L, 0x000000000a000000L, 0x0000000014000000L, 0x0000000028000000L, 0x0000000050000000L, 0x00000000a0000000L, 0x0000000040000000L,
        0x0000000200000000L, 0x0000000500000000L, 0x0000000a00000000L, 0x0000001400000000L, 0x0000002800000000L, 0x0000005000000000L, 0x000000a000000000L, 0x0000004000000000L,
        0x0000020000000000L, 0x0000050000000000L, 0x00000a0000000000L, 0x0000140000000000L, 0x0000280000000000L, 0x0000500000000000L, 0x0000a00000000000L, 0x0000400000000000L,
        0x0002000000000000L, 0x0005000000000000L, 0x000a000000000000L, 0x0014000000000000L, 0x0028000000000000L, 0x0050000000000000L, 0x00a0000000000000L, 0x0040000000000000L
    )

    @Suppress("LongLine")
    val whitePawnMoves = longArrayOf(
        0x0000000000000100L, 0x0000000000000200L, 0x0000000000000400L, 0x0000000000000800L, 0x0000000000001000L, 0x0000000000002000L, 0x0000000000004000L, 0x0000000000008000L,
        0x0000000001010000L, 0x0000000002020000L, 0x0000000004040000L, 0x0000000008080000L, 0x0000000010100000L, 0x0000000020200000L, 0x0000000040400000L, 0x0000000080800000L,
        0x0000000001000000L, 0x0000000002000000L, 0x0000000004000000L, 0x0000000008000000L, 0x0000000010000000L, 0x0000000020000000L, 0x0000000040000000L, 0x0000000080000000L,
        0x0000000100000000L, 0x0000000200000000L, 0x0000000400000000L, 0x0000000800000000L, 0x0000001000000000L, 0x0000002000000000L, 0x0000004000000000L, 0x0000008000000000L,
        0x0000010000000000L, 0x0000020000000000L, 0x0000040000000000L, 0x0000080000000000L, 0x0000100000000000L, 0x0000200000000000L, 0x0000400000000000L, 0x0000800000000000L,
        0x0001000000000000L, 0x0002000000000000L, 0x0004000000000000L, 0x0008000000000000L, 0x0010000000000000L, 0x0020000000000000L, 0x0040000000000000L, 0x0080000000000000L,
        0x0100000000000000L, 0x0200000000000000L, 0x0400000000000000L, 0x0800000000000000L, 0x1000000000000000L, 0x2000000000000000L, 0x4000000000000000L, Long.MIN_VALUE,
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L
    )

    @Suppress("LongLine")
    val blackPawnMoves = longArrayOf(
        0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L, 0x0000000000000000L,
        0x0000000000000001L, 0x0000000000000002L, 0x0000000000000004L, 0x0000000000000008L, 0x0000000000000010L, 0x0000000000000020L, 0x0000000000000040L, 0x0000000000000080L,
        0x0000000000000100L, 0x0000000000000200L, 0x0000000000000400L, 0x0000000000000800L, 0x0000000000001000L, 0x0000000000002000L, 0x0000000000004000L, 0x0000000000008000L,
        0x0000000000010000L, 0x0000000000020000L, 0x0000000000040000L, 0x0000000000080000L, 0x0000000000100000L, 0x0000000000200000L, 0x0000000000400000L, 0x0000000000800000L,
        0x0000000001000000L, 0x0000000002000000L, 0x0000000004000000L, 0x0000000008000000L, 0x0000000010000000L, 0x0000000020000000L, 0x0000000040000000L, 0x0000000080000000L,
        0x0000000100000000L, 0x0000000200000000L, 0x0000000400000000L, 0x0000000800000000L, 0x0000001000000000L, 0x0000002000000000L, 0x0000004000000000L, 0x0000008000000000L,
        0x0000010100000000L, 0x0000020200000000L, 0x0000040400000000L, 0x0000080800000000L, 0x0000101000000000L, 0x0000202000000000L, 0x0000404000000000L, 0x0000808000000000L,
        0x0001000000000000L, 0x0002000000000000L, 0x0004000000000000L, 0x0008000000000000L, 0x0010000000000000L, 0x0020000000000000L, 0x0040000000000000L, 0x0080000000000000L
    )

    @Suppress("LongLine")
    val adjacentSquares = longArrayOf(
        0x0000000000000302L, 0x0000000000000705L, 0x0000000000000e0aL, 0x0000000000001c14L, 0x0000000000003828L, 0x0000000000007050L, 0x000000000000e0a0L, 0x000000000000c040L,
        0x0000000000030203L, 0x0000000000070507L, 0x00000000000e0a0eL, 0x00000000001c141cL, 0x0000000000382838L, 0x0000000000705070L, 0x0000000000e0a0e0L, 0x0000000000c040c0L,
        0x0000000003020300L, 0x0000000007050700L, 0x000000000e0a0e00L, 0x000000001c141c00L, 0x0000000038283800L, 0x0000000070507000L, 0x00000000e0a0e000L, 0x00000000c040c000L,
        0x0000000302030000L, 0x0000000705070000L, 0x0000000e0a0e0000L, 0x0000001c141c0000L, 0x0000003828380000L, 0x0000007050700000L, 0x000000e0a0e00000L, 0x000000c040c00000L,
        0x0000030203000000L, 0x0000070507000000L, 0x00000e0a0e000000L, 0x00001c141c000000L, 0x0000382838000000L, 0x0000705070000000L, 0x0000e0a0e0000000L, 0x0000c040c0000000L,
        0x0003020300000000L, 0x0007050700000000L, 0x000e0a0e00000000L, 0x001c141c00000000L, 0x0038283800000000L, 0x0070507000000000L, 0x00e0a0e000000000L, 0x00c040c000000000L,
        0x0302030000000000L, 0x0705070000000000L, 0x0e0a0e0000000000L, 0x1c141c0000000000L, 0x3828380000000000L, 0x7050700000000000L, -0x1F5F200000000000L, -0x3FBF400000000000L,
        0x0203000000000000L, 0x0507000000000000L, 0x0a0e000000000000L, 0x141c000000000000L, 0x2838000000000000L, 0x5070000000000000L, -0x5F20000000000000L, 0x40c0000000000000L
    )

    private val rankAttacks = LongArray(64)
    private val fileAttacks = LongArray(64)
    private val diagA1H8Attacks = LongArray(64)
    private val diagH1A8Attacks = LongArray(64)

    init {
        for (x in 0 until 64) {
            for (y in 0 until 64) {
                bbTable[x][y] = (1L shl y) or ((1L shl y) - (1L shl x))
            }
        }
        for (sq in Square.allSquares) {
            if (sq == Square.NONE) continue
            val i = sq.ordinal
            rankAttacks[i] = rankBB[sq.rank.ordinal] xor sq.bb
            fileAttacks[i] = fileBB[sq.file.ordinal] xor sq.bb
            diagA1H8Attacks[i] = diagonalA1H8BB[squareToDiagonalA1H8[i].ordinal] xor sq.bb
            diagH1A8Attacks[i] = diagonalH1A8BB[squareToDiagonalH1A8[i].ordinal] xor sq.bb
        }
    }

    fun bitScanForward(bb: Long): Int = java.lang.Long.numberOfTrailingZeros(bb)
    fun bitScanReverse(bb: Long): Int = 63 - java.lang.Long.numberOfLeadingZeros(bb)

    fun bitsBetween(bb: Long, sq1: Int, sq2: Int): Long = bbTable[sq1][sq2] and bb

    fun extractLsb(bb: Long): Long = bb and (bb - 1)
    fun hasOnly1Bit(bb: Long): Boolean = bb > 0L && extractLsb(bb) == 0L

    fun getBbtable(sq: Square): Long = 1L shl sq.ordinal

    private fun getSliderAttacks(attacks: Long, mask: Long, index: Int): Long {
        val occ = mask and attacks
        if (occ == 0L) return attacks
        val m = (1L shl index) - 1L
        val lowerMask = occ and m
        val upperMask = occ and m.inv()
        val minor = if (lowerMask == 0L) 0 else bitScanReverse(lowerMask)
        val major = if (upperMask == 0L) 63 else bitScanForward(upperMask)
        return bitsBetween(attacks, minor, major)
    }

    fun getBishopAttacks(mask: Long, square: Square): Long {
        val i = square.ordinal
        return getSliderAttacks(diagA1H8Attacks[i], mask, i) or
                getSliderAttacks(diagH1A8Attacks[i], mask, i)
    }

    fun getRookAttacks(mask: Long, square: Square): Long {
        val i = square.ordinal
        return getSliderAttacks(fileAttacks[i], mask, i) or
                getSliderAttacks(rankAttacks[i], mask, i)
    }

    fun getQueenAttacks(mask: Long, square: Square): Long =
        getRookAttacks(mask, square) or getBishopAttacks(mask, square)

    fun getKnightAttacks(square: Square, occupied: Long): Long =
        knightAttacks[square.ordinal] and occupied

    fun getPawnAttacks(side: Side, square: Square): Long =
        if (side == Side.WHITE) whitePawnAttacks[square.ordinal]
        else blackPawnAttacks[square.ordinal]

    fun getPawnCaptures(side: Side, square: Square, occupied: Long, enPassant: Square): Long {
        val pawnAttacks = if (side == Side.WHITE)
            whitePawnAttacks[square.ordinal] else blackPawnAttacks[square.ordinal]
        var occ = occupied
        if (enPassant != Square.NONE) {
            val ep = enPassant.bb
            occ = occ or if (side == Side.WHITE) ep shl 8 else ep ushr 8
        }
        return pawnAttacks and occ
    }

    fun getPawnMoves(side: Side, square: Square, occupied: Long): Long {
        val pawnMoves = if (side == Side.WHITE)
            whitePawnMoves[square.ordinal] else blackPawnMoves[square.ordinal]
        var occ = occupied
        if (square.rank == Rank.RANK_2 && side == Side.WHITE) {
            if ((square.bb shl 8) and occ != 0L) {
                occ = occ or (square.bb shl 16)
            }
        } else if (square.rank == Rank.RANK_7 && side == Side.BLACK) {
            if ((square.bb ushr 8) and occ != 0L) {
                occ = occ or (square.bb ushr 16)
            }
        }
        return pawnMoves and occ.inv()
    }

    fun getKingAttacks(square: Square, occupied: Long): Long =
        adjacentSquares[square.ordinal] and occupied

    fun bbToSquareList(pieces: Long): List<Square> {
        val squares = mutableListOf<Square>()
        var bb = pieces
        while (bb != 0L) {
            val sq = bitScanForward(bb)
            bb = extractLsb(bb)
            squares.add(Square.squareAt(sq))
        }
        return squares
    }

    fun getRankbb(sq: Square): Long = rankBB[sq.rank.ordinal]
    fun getFilebb(sq: Square): Long = fileBB[sq.file.ordinal]
    fun getRankbb(rank: Rank): Long = rankBB[rank.ordinal]
    fun getFilebb(file: ChessFile): Long = fileBB[file.ordinal]
}
