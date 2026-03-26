package com.chunkymonkey.chesscore

class GameContext(val startFEN: String = Constants.startStandardFENPosition) {

    var whiteOO: Move = Constants.DEFAULT_WHITE_OO
    var whiteOOO: Move = Constants.DEFAULT_WHITE_OOO
    var blackOO: Move = Constants.DEFAULT_BLACK_OO
    var blackOOO: Move = Constants.DEFAULT_BLACK_OOO

    var whiteRookOO: Move = Constants.DEFAULT_WHITE_ROOK_OO
    var whiteRookOOO: Move = Constants.DEFAULT_WHITE_ROOK_OOO
    var blackRookOO: Move = Constants.DEFAULT_BLACK_ROOK_OO
    var blackRookOOO: Move = Constants.DEFAULT_BLACK_ROOK_OOO

    var whiteOOSquares: List<Square> = Constants.DEFAULT_WHITE_OO_SQUARES
    var whiteOOOSquares: List<Square> = Constants.DEFAULT_WHITE_OOO_SQUARES
    var blackOOSquares: List<Square> = Constants.DEFAULT_BLACK_OO_SQUARES
    var blackOOOSquares: List<Square> = Constants.DEFAULT_BLACK_OOO_SQUARES

    var whiteOOAllSquaresBb: Long = squareListToBb(Constants.DEFAULT_WHITE_OO_ALL_SQUARES)
    var whiteOOOAllSquaresBb: Long = squareListToBb(Constants.DEFAULT_WHITE_OOO_ALL_SQUARES)
    var blackOOAllSquaresBb: Long = squareListToBb(Constants.DEFAULT_BLACK_OO_ALL_SQUARES)
    var blackOOOAllSquaresBb: Long = squareListToBb(Constants.DEFAULT_BLACK_OOO_ALL_SQUARES)

    private fun squareListToBb(squares: List<Square>): Long =
        squares.fold(0L) { acc, sq -> acc or sq.bb }

    fun getoo(side: Side): Move = if (side == Side.WHITE) whiteOO else blackOO
    fun getooo(side: Side): Move = if (side == Side.WHITE) whiteOOO else blackOOO
    fun getRookoo(side: Side): Move = if (side == Side.WHITE) whiteRookOO else blackRookOO
    fun getRookooo(side: Side): Move = if (side == Side.WHITE) whiteRookOOO else blackRookOOO
    fun getooSquares(side: Side): List<Square> = if (side == Side.WHITE) whiteOOSquares else blackOOSquares
    fun getoooSquares(side: Side): List<Square> = if (side == Side.WHITE) whiteOOOSquares else blackOOOSquares
    fun getooAllSquaresBb(side: Side): Long = if (side == Side.WHITE) whiteOOAllSquaresBb else blackOOAllSquaresBb
    fun getoooAllSquaresBb(side: Side): Long = if (side == Side.WHITE) whiteOOOAllSquaresBb else blackOOOAllSquaresBb

    fun isCastleMove(move: Move): Boolean =
        move == whiteOO || move == whiteOOO || move == blackOO || move == blackOOO

    fun isKingSideCastle(move: Move): Boolean =
        move == whiteOO || move == blackOO

    fun isQueenSideCastle(move: Move): Boolean =
        move == whiteOOO || move == blackOOO

    fun hasCastleRight(move: Move, right: CastleRight): Boolean {
        if (right == CastleRight.NONE) return false
        if (isKingSideCastle(move)) {
            return right == CastleRight.KING_SIDE || right == CastleRight.KING_AND_QUEEN_SIDE
        }
        if (isQueenSideCastle(move)) {
            return right == CastleRight.QUEEN_SIDE || right == CastleRight.KING_AND_QUEEN_SIDE
        }
        return false
    }

    fun getRookCastleMove(side: Side, castleRight: CastleRight): Move = when (castleRight) {
        CastleRight.KING_SIDE -> getRookoo(side)
        CastleRight.QUEEN_SIDE -> getRookooo(side)
        else -> Constants.emptyMove
    }
}
