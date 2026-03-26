package com.chunkymonkey.chesscore

enum class Piece {
    WHITE_PAWN,
    WHITE_KNIGHT,
    WHITE_BISHOP,
    WHITE_ROOK,
    WHITE_QUEEN,
    WHITE_KING,
    BLACK_PAWN,
    BLACK_KNIGHT,
    BLACK_BISHOP,
    BLACK_ROOK,
    BLACK_QUEEN,
    BLACK_KING,
    NONE;

    val pieceType: PieceType
        get() = pieceTypeMap[this] ?: PieceType.NONE

    val pieceSide: Side
        get() = pieceSideMap[this] ?: Side.WHITE

    companion object {
        val allPieces: Array<Piece> = values()

        fun make(side: Side, type: PieceType): Piece = pieceMake[type.ordinal][side.ordinal]

        private val pieceTypeMap: Map<Piece, PieceType> = mapOf(
            WHITE_PAWN to PieceType.PAWN, BLACK_PAWN to PieceType.PAWN,
            WHITE_KNIGHT to PieceType.KNIGHT, BLACK_KNIGHT to PieceType.KNIGHT,
            WHITE_BISHOP to PieceType.BISHOP, BLACK_BISHOP to PieceType.BISHOP,
            WHITE_ROOK to PieceType.ROOK, BLACK_ROOK to PieceType.ROOK,
            WHITE_QUEEN to PieceType.QUEEN, BLACK_QUEEN to PieceType.QUEEN,
            WHITE_KING to PieceType.KING, BLACK_KING to PieceType.KING
        )

        private val pieceSideMap: Map<Piece, Side> = mapOf(
            WHITE_PAWN to Side.WHITE, WHITE_KNIGHT to Side.WHITE,
            WHITE_BISHOP to Side.WHITE, WHITE_ROOK to Side.WHITE,
            WHITE_QUEEN to Side.WHITE, WHITE_KING to Side.WHITE,
            BLACK_PAWN to Side.BLACK, BLACK_KNIGHT to Side.BLACK,
            BLACK_BISHOP to Side.BLACK, BLACK_ROOK to Side.BLACK,
            BLACK_QUEEN to Side.BLACK, BLACK_KING to Side.BLACK
        )

        // Rows: PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING, NONE
        // Cols: WHITE, BLACK
        private val pieceMake: Array<Array<Piece>> = arrayOf(
            arrayOf(WHITE_PAWN, BLACK_PAWN),
            arrayOf(WHITE_KNIGHT, BLACK_KNIGHT),
            arrayOf(WHITE_BISHOP, BLACK_BISHOP),
            arrayOf(WHITE_ROOK, BLACK_ROOK),
            arrayOf(WHITE_QUEEN, BLACK_QUEEN),
            arrayOf(WHITE_KING, BLACK_KING),
            arrayOf(NONE, NONE)
        )
    }
}
