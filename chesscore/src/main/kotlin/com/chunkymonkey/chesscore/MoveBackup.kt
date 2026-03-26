package com.chunkymonkey.chesscore

class MoveBackup {
    val castleRight = mutableMapOf<Side, CastleRight>()
    var sideToMove: Side = Side.WHITE
    var enPassantTarget: Square = Square.NONE
    var enPassant: Square = Square.NONE
    var moveCounter: Int = 0
    var halfMoveCounter: Int = 0
    var move: Move = Constants.emptyMove
    var rookCastleMove: Move? = null
    var capturedPiece: Piece = Piece.NONE
    var capturedSquare: Square = Square.NONE
    var movingPiece: Piece = Piece.NONE
    var isCastleMove: Boolean = false
    var incrementalHashKey: Long = 0L

    constructor()

    constructor(board: Board, move: Move) {
        makeBackup(board, move)
    }

    fun makeBackup(board: Board, move: Move) {
        this.incrementalHashKey = board.incrementalHashKey
        this.sideToMove = board.sideToMove
        this.enPassantTarget = board.enPassantTarget
        this.enPassant = board.enPassant
        this.moveCounter = board.moveCounter
        this.halfMoveCounter = board.halfMoveCounter
        this.move = move
        castleRight[Side.WHITE] = board.getCastleRight(Side.WHITE)
        castleRight[Side.BLACK] = board.getCastleRight(Side.BLACK)
        capturedPiece = board.getPiece(move.to)
        capturedSquare = move.to
        movingPiece = board.getPiece(move.from)

        val ctx = board.context
        if (movingPiece.pieceType == PieceType.KING && ctx.isCastleMove(move)) {
            isCastleMove = true
            val c = if (ctx.isKingSideCastle(move)) CastleRight.KING_SIDE else CastleRight.QUEEN_SIDE
            rookCastleMove = ctx.getRookCastleMove(board.sideToMove, c)
        }
    }

    fun restore(board: Board) {
        board.incrementalHashKey = incrementalHashKey
        board.sideToMove = sideToMove
        board.enPassantTarget = enPassantTarget
        board.enPassant = enPassant
        board.moveCounter = moveCounter
        board.halfMoveCounter = halfMoveCounter
        board.castleRightMap[Side.WHITE] = castleRight[Side.WHITE] ?: CastleRight.NONE
        board.castleRightMap[Side.BLACK] = castleRight[Side.BLACK] ?: CastleRight.NONE

        if (move == Constants.emptyMove) return

        if (isCastleMove && rookCastleMove != null) {
            board.undoMovePiece(rookCastleMove!!)
        }

        val movedPieceTo = board.getPiece(move.to)
        board.unsetPiece(movedPieceTo, move.to)
        if (move.promotion != Piece.NONE) {
            board.setPiece(Piece.make(sideToMove, PieceType.PAWN), move.from)
        } else {
            board.setPiece(movingPiece, move.from)
        }

        if (capturedPiece != Piece.NONE) {
            board.setPiece(capturedPiece, capturedSquare)
        }
    }
}
