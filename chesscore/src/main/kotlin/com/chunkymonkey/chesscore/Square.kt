package com.chunkymonkey.chesscore

enum class Square {
    A1, B1, C1, D1, E1, F1, G1, H1,
    A2, B2, C2, D2, E2, F2, G2, H2,
    A3, B3, C3, D3, E3, F3, G3, H3,
    A4, B4, C4, D4, E4, F4, G4, H4,
    A5, B5, C5, D5, E5, F5, G5, H5,
    A6, B6, C6, D6, E6, F6, G6, H6,
    A7, B7, C7, D7, E7, F7, G7, H7,
    A8, B8, C8, D8, E8, F8, G8, H8,
    NONE;

    val file: ChessFile
        get() = if (this == NONE) ChessFile.NONE else ChessFile.allFiles[ordinal % 8]

    val rank: Rank
        get() = if (this == NONE) Rank.NONE else Rank.allRanks[ordinal / 8]

    val bb: Long
        get() = if (this == NONE) 0L else 1L shl ordinal

    val isLightSquare: Boolean
        get() = (bb and Bitboard.lightSquares) != 0L

    val sideSquares: Array<Square>
        get() = sideSquareMap[this] ?: emptyArray()

    fun value(): String = name

    companion object {
        val allSquares: Array<Square> = values()

        fun encode(rank: Rank, file: ChessFile): Square {
            val idx = rank.ordinal * 8 + file.ordinal
            return if (idx < 0 || idx >= 64) NONE else allSquares[idx]
        }

        fun squareAt(index: Int): Square =
            if (index < 0 || index >= 64) NONE else allSquares[index]

        private val sideSquareMap: Map<Square, Array<Square>> by lazy {
            val map = mutableMapOf<Square, Array<Square>>()
            for (sq in allSquares) {
                if (sq == NONE) continue
                val f = sq.file.ordinal
                val r = sq.rank.ordinal
                val sides = mutableListOf<Square>()
                if (f > 0) sides.add(encode(Rank.allRanks[r], ChessFile.allFiles[f - 1]))
                if (f < 7) sides.add(encode(Rank.allRanks[r], ChessFile.allFiles[f + 1]))
                map[sq] = sides.toTypedArray()
            }
            map
        }
    }
}
