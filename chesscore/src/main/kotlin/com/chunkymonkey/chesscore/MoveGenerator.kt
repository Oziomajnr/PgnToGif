package com.chunkymonkey.chesscore

object MoveGenerator {

    fun generatePseudoLegalMoves(board: Board): MutableList<Move> {
        val moves = mutableListOf<Move>()
        generatePawnCaptures(board, moves)
        generatePawnMoves(board, moves)
        generateKnightMoves(board, moves, board.getBitboard(board.sideToMove).inv())
        generateBishopMoves(board, moves, board.getBitboard(board.sideToMove).inv())
        generateRookMoves(board, moves, board.getBitboard(board.sideToMove).inv())
        generateQueenMoves(board, moves, board.getBitboard(board.sideToMove).inv())
        generateKingMoves(board, moves, board.getBitboard(board.sideToMove).inv())
        generateCastleMoves(board, moves)
        return moves
    }

    fun generateLegalMoves(board: Board): List<Move> {
        val moves = generatePseudoLegalMoves(board)
        moves.removeAll { !board.isMoveLegal(it, false) }
        return moves
    }

    private fun generatePawnCaptures(board: Board, moves: MutableList<Move>) {
        val side = board.sideToMove
        var pieces = board.getBitboard(Piece.make(side, PieceType.PAWN))
        while (pieces != 0L) {
            val sourceIndex = Bitboard.bitScanForward(pieces)
            pieces = Bitboard.extractLsb(pieces)
            val sqSource = Square.squareAt(sourceIndex)
            var attacks = Bitboard.getPawnCaptures(
                side, sqSource, board.getBitboard(), board.enPassantTarget
            ) and board.getBitboard(side).inv()
            while (attacks != 0L) {
                val targetIndex = Bitboard.bitScanForward(attacks)
                attacks = Bitboard.extractLsb(attacks)
                val sqTarget = Square.squareAt(targetIndex)
                addPromotions(moves, side, sqTarget, sqSource)
            }
        }
    }

    private fun generatePawnMoves(board: Board, moves: MutableList<Move>) {
        val side = board.sideToMove
        var pieces = board.getBitboard(Piece.make(side, PieceType.PAWN))
        while (pieces != 0L) {
            val sourceIndex = Bitboard.bitScanForward(pieces)
            pieces = Bitboard.extractLsb(pieces)
            val sqSource = Square.squareAt(sourceIndex)
            var attacks = Bitboard.getPawnMoves(side, sqSource, board.getBitboard())
            while (attacks != 0L) {
                val targetIndex = Bitboard.bitScanForward(attacks)
                attacks = Bitboard.extractLsb(attacks)
                val sqTarget = Square.squareAt(targetIndex)
                addPromotions(moves, side, sqTarget, sqSource)
            }
        }
    }

    private fun addPromotions(moves: MutableList<Move>, side: Side, target: Square, source: Square) {
        if (side == Side.WHITE && target.rank == Rank.RANK_8) {
            moves.add(Move(source, target, Piece.WHITE_QUEEN))
            moves.add(Move(source, target, Piece.WHITE_ROOK))
            moves.add(Move(source, target, Piece.WHITE_BISHOP))
            moves.add(Move(source, target, Piece.WHITE_KNIGHT))
        } else if (side == Side.BLACK && target.rank == Rank.RANK_1) {
            moves.add(Move(source, target, Piece.BLACK_QUEEN))
            moves.add(Move(source, target, Piece.BLACK_ROOK))
            moves.add(Move(source, target, Piece.BLACK_BISHOP))
            moves.add(Move(source, target, Piece.BLACK_KNIGHT))
        } else {
            moves.add(Move(source, target))
        }
    }

    private fun generateKnightMoves(board: Board, moves: MutableList<Move>, mask: Long) {
        val side = board.sideToMove
        var pieces = board.getBitboard(Piece.make(side, PieceType.KNIGHT))
        while (pieces != 0L) {
            val idx = Bitboard.bitScanForward(pieces)
            pieces = Bitboard.extractLsb(pieces)
            val sqSource = Square.squareAt(idx)
            var attacks = Bitboard.getKnightAttacks(sqSource, mask)
            while (attacks != 0L) {
                val aidx = Bitboard.bitScanForward(attacks)
                attacks = Bitboard.extractLsb(attacks)
                moves.add(Move(sqSource, Square.squareAt(aidx)))
            }
        }
    }

    private fun generateBishopMoves(board: Board, moves: MutableList<Move>, mask: Long) {
        val side = board.sideToMove
        var pieces = board.getBitboard(Piece.make(side, PieceType.BISHOP))
        while (pieces != 0L) {
            val idx = Bitboard.bitScanForward(pieces)
            pieces = Bitboard.extractLsb(pieces)
            val sqSource = Square.squareAt(idx)
            var attacks = Bitboard.getBishopAttacks(board.getBitboard(), sqSource) and mask
            while (attacks != 0L) {
                val aidx = Bitboard.bitScanForward(attacks)
                attacks = Bitboard.extractLsb(attacks)
                moves.add(Move(sqSource, Square.squareAt(aidx)))
            }
        }
    }

    private fun generateRookMoves(board: Board, moves: MutableList<Move>, mask: Long) {
        val side = board.sideToMove
        var pieces = board.getBitboard(Piece.make(side, PieceType.ROOK))
        while (pieces != 0L) {
            val idx = Bitboard.bitScanForward(pieces)
            pieces = Bitboard.extractLsb(pieces)
            val sqSource = Square.squareAt(idx)
            var attacks = Bitboard.getRookAttacks(board.getBitboard(), sqSource) and mask
            while (attacks != 0L) {
                val aidx = Bitboard.bitScanForward(attacks)
                attacks = Bitboard.extractLsb(attacks)
                moves.add(Move(sqSource, Square.squareAt(aidx)))
            }
        }
    }

    private fun generateQueenMoves(board: Board, moves: MutableList<Move>, mask: Long) {
        val side = board.sideToMove
        var pieces = board.getBitboard(Piece.make(side, PieceType.QUEEN))
        while (pieces != 0L) {
            val idx = Bitboard.bitScanForward(pieces)
            pieces = Bitboard.extractLsb(pieces)
            val sqSource = Square.squareAt(idx)
            var attacks = Bitboard.getQueenAttacks(board.getBitboard(), sqSource) and mask
            while (attacks != 0L) {
                val aidx = Bitboard.bitScanForward(attacks)
                attacks = Bitboard.extractLsb(attacks)
                moves.add(Move(sqSource, Square.squareAt(aidx)))
            }
        }
    }

    private fun generateKingMoves(board: Board, moves: MutableList<Move>, mask: Long) {
        val side = board.sideToMove
        var pieces = board.getBitboard(Piece.make(side, PieceType.KING))
        while (pieces != 0L) {
            val idx = Bitboard.bitScanForward(pieces)
            pieces = Bitboard.extractLsb(pieces)
            val sqSource = Square.squareAt(idx)
            var attacks = Bitboard.getKingAttacks(sqSource, mask)
            while (attacks != 0L) {
                val aidx = Bitboard.bitScanForward(attacks)
                attacks = Bitboard.extractLsb(attacks)
                moves.add(Move(sqSource, Square.squareAt(aidx)))
            }
        }
    }

    private fun generateCastleMoves(board: Board, moves: MutableList<Move>) {
        val side = board.sideToMove
        if (board.isKingAttacked) return

        if (board.getCastleRight(side) == CastleRight.KING_AND_QUEEN_SIDE ||
            board.getCastleRight(side) == CastleRight.KING_SIDE
        ) {
            if ((board.getBitboard() and board.context.getooAllSquaresBb(side)) == 0L) {
                if (!board.isSquareAttackedBy(board.context.getooSquares(side), side.flip())) {
                    moves.add(board.context.getoo(side))
                }
            }
        }
        if (board.getCastleRight(side) == CastleRight.KING_AND_QUEEN_SIDE ||
            board.getCastleRight(side) == CastleRight.QUEEN_SIDE
        ) {
            if ((board.getBitboard() and board.context.getoooAllSquaresBb(side)) == 0L) {
                if (!board.isSquareAttackedBy(board.context.getoooSquares(side), side.flip())) {
                    moves.add(board.context.getooo(side))
                }
            }
        }
    }
}
