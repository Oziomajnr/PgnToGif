package com.chunkymonkey.chesscore

object Constants {
    const val startStandardFENPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

    val emptyMove = Move(Square.NONE, Square.NONE)

    // Default castle moves
    val DEFAULT_WHITE_OO = Move(Square.E1, Square.G1)
    val DEFAULT_WHITE_OOO = Move(Square.E1, Square.C1)
    val DEFAULT_BLACK_OO = Move(Square.E8, Square.G8)
    val DEFAULT_BLACK_OOO = Move(Square.E8, Square.C8)

    val DEFAULT_WHITE_ROOK_OO = Move(Square.H1, Square.F1)
    val DEFAULT_WHITE_ROOK_OOO = Move(Square.A1, Square.D1)
    val DEFAULT_BLACK_ROOK_OO = Move(Square.H8, Square.F8)
    val DEFAULT_BLACK_ROOK_OOO = Move(Square.A8, Square.D8)

    val DEFAULT_WHITE_OO_SQUARES: List<Square> = listOf(Square.F1, Square.G1)
    val DEFAULT_WHITE_OOO_SQUARES: List<Square> = listOf(Square.D1, Square.C1)
    val DEFAULT_BLACK_OO_SQUARES: List<Square> = listOf(Square.F8, Square.G8)
    val DEFAULT_BLACK_OOO_SQUARES: List<Square> = listOf(Square.D8, Square.C8)

    val DEFAULT_WHITE_OO_ALL_SQUARES: List<Square> = listOf(Square.F1, Square.G1)
    val DEFAULT_WHITE_OOO_ALL_SQUARES: List<Square> = listOf(Square.D1, Square.C1, Square.B1)
    val DEFAULT_BLACK_OO_ALL_SQUARES: List<Square> = listOf(Square.F8, Square.G8)
    val DEFAULT_BLACK_OOO_ALL_SQUARES: List<Square> = listOf(Square.D8, Square.C8, Square.B8)

    private val pieceNotation: Map<Piece, String> = mapOf(
        Piece.WHITE_PAWN to "P", Piece.WHITE_KNIGHT to "N",
        Piece.WHITE_BISHOP to "B", Piece.WHITE_ROOK to "R",
        Piece.WHITE_QUEEN to "Q", Piece.WHITE_KING to "K",
        Piece.BLACK_PAWN to "p", Piece.BLACK_KNIGHT to "n",
        Piece.BLACK_BISHOP to "b", Piece.BLACK_ROOK to "r",
        Piece.BLACK_QUEEN to "q", Piece.BLACK_KING to "k"
    )

    private val pieceNotationR: Map<String, Piece> =
        pieceNotation.entries.associate { (k, v) -> v to k }

    fun getPieceNotation(piece: Piece): String? = pieceNotation[piece]

    fun getPieceByNotation(notation: String): Piece =
        pieceNotationR[notation] ?: Piece.NONE
}
