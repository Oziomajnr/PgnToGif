package com.chunkymonkey.chesscore

import java.util.LinkedList

class Board(
    val context: GameContext = GameContext(),
    private val updateHistory: Boolean = true
) {
    private val bitboard = LongArray(Piece.allPieces.size)
    private val bbSide = LongArray(Side.allSides.size)
    private val occupation = Array(Square.values().size) { Piece.NONE }
    internal val castleRightMap = mutableMapOf<Side, CastleRight>(
        Side.WHITE to CastleRight.NONE,
        Side.BLACK to CastleRight.NONE
    )
    private val backup = LinkedList<MoveBackup>()
    private val history = LinkedList<Long>()

    var sideToMove: Side = Side.WHITE
    var enPassantTarget: Square = Square.NONE
    var enPassant: Square = Square.NONE
    var moveCounter: Int = 1
    var halfMoveCounter: Int = 0
    var incrementalHashKey: Long = 0L

    init {
        loadFromFen(context.startFEN)
    }

    fun getCastleRight(side: Side): CastleRight = castleRightMap[side] ?: CastleRight.NONE

    fun getBackup(): LinkedList<MoveBackup> = backup

    fun getPiece(sq: Square): Piece = occupation[sq.ordinal]

    fun getBitboard(): Long = bbSide[0] or bbSide[1]
    fun getBitboard(piece: Piece): Long = bitboard[piece.ordinal]
    fun getBitboard(side: Side): Long = bbSide[side.ordinal]

    fun getPieceLocation(piece: Piece): List<Square> {
        val bb = getBitboard(piece)
        return if (bb != 0L) Bitboard.bbToSquareList(bb) else emptyList()
    }

    fun getFirstPieceLocation(piece: Piece): Square {
        val bb = getBitboard(piece)
        return if (bb != 0L) Square.squareAt(Bitboard.bitScanForward(bb)) else Square.NONE
    }

    fun hasPiece(piece: Piece, locations: Array<Square>): Boolean =
        locations.any { (getBitboard(piece) and it.bb) != 0L }

    fun setPiece(piece: Piece, sq: Square) {
        bitboard[piece.ordinal] = bitboard[piece.ordinal] or sq.bb
        bbSide[piece.pieceSide.ordinal] = bbSide[piece.pieceSide.ordinal] or sq.bb
        occupation[sq.ordinal] = piece
        if (piece != Piece.NONE && sq != Square.NONE) {
            incrementalHashKey = incrementalHashKey xor getPieceSquareKey(piece, sq)
        }
    }

    fun unsetPiece(piece: Piece, sq: Square) {
        bitboard[piece.ordinal] = bitboard[piece.ordinal] xor sq.bb
        bbSide[piece.pieceSide.ordinal] = bbSide[piece.pieceSide.ordinal] xor sq.bb
        occupation[sq.ordinal] = Piece.NONE
        if (piece != Piece.NONE && sq != Square.NONE) {
            incrementalHashKey = incrementalHashKey xor getPieceSquareKey(piece, sq)
        }
    }

    fun doMove(move: Move, fullValidation: Boolean = false): Boolean {
        if (!isMoveLegal(move, fullValidation)) return false

        val movingPiece = getPiece(move.from)
        val side = sideToMove

        val backupMove = MoveBackup(this, move)
        val isCastle = context.isCastleMove(move)

        incrementalHashKey = incrementalHashKey xor getSideKey(sideToMove)
        if (enPassantTarget != Square.NONE) {
            incrementalHashKey = incrementalHashKey xor getEnPassantKey(enPassantTarget)
        }

        if (movingPiece.pieceType == PieceType.KING) {
            if (isCastle) {
                if (context.hasCastleRight(move, getCastleRight(side))) {
                    val c = if (context.isKingSideCastle(move)) CastleRight.KING_SIDE else CastleRight.QUEEN_SIDE
                    val rookMove = context.getRookCastleMove(side, c)
                    movePiece(rookMove, backupMove)
                } else {
                    return false
                }
            }
            if (getCastleRight(side) != CastleRight.NONE) {
                incrementalHashKey = incrementalHashKey xor getCastleRightKey(side)
                castleRightMap[side] = CastleRight.NONE
            }
        } else if (movingPiece.pieceType == PieceType.ROOK && getCastleRight(side) != CastleRight.NONE) {
            val oo = context.getRookoo(side)
            val ooo = context.getRookooo(side)

            if (move.from == oo.from) {
                when (getCastleRight(side)) {
                    CastleRight.KING_AND_QUEEN_SIDE -> {
                        incrementalHashKey = incrementalHashKey xor getCastleRightKey(side)
                        castleRightMap[side] = CastleRight.QUEEN_SIDE
                        incrementalHashKey = incrementalHashKey xor getCastleRightKey(side)
                    }
                    CastleRight.KING_SIDE -> {
                        incrementalHashKey = incrementalHashKey xor getCastleRightKey(side)
                        castleRightMap[side] = CastleRight.NONE
                    }
                    else -> {}
                }
            } else if (move.from == ooo.from) {
                when (getCastleRight(side)) {
                    CastleRight.KING_AND_QUEEN_SIDE -> {
                        incrementalHashKey = incrementalHashKey xor getCastleRightKey(side)
                        castleRightMap[side] = CastleRight.KING_SIDE
                        incrementalHashKey = incrementalHashKey xor getCastleRightKey(side)
                    }
                    CastleRight.QUEEN_SIDE -> {
                        incrementalHashKey = incrementalHashKey xor getCastleRightKey(side)
                        castleRightMap[side] = CastleRight.NONE
                    }
                    else -> {}
                }
            }
        }

        val capturedPiece = movePiece(move, backupMove)

        if (capturedPiece.pieceType == PieceType.ROOK) {
            val otherSide = side.flip()
            val oo = context.getRookoo(otherSide)
            val ooo = context.getRookooo(otherSide)
            if (move.to == oo.from) {
                when (getCastleRight(otherSide)) {
                    CastleRight.KING_AND_QUEEN_SIDE -> {
                        incrementalHashKey = incrementalHashKey xor getCastleRightKey(otherSide)
                        castleRightMap[otherSide] = CastleRight.QUEEN_SIDE
                        incrementalHashKey = incrementalHashKey xor getCastleRightKey(otherSide)
                    }
                    CastleRight.KING_SIDE -> {
                        incrementalHashKey = incrementalHashKey xor getCastleRightKey(otherSide)
                        castleRightMap[otherSide] = CastleRight.NONE
                    }
                    else -> {}
                }
            } else if (move.to == ooo.from) {
                when (getCastleRight(otherSide)) {
                    CastleRight.KING_AND_QUEEN_SIDE -> {
                        incrementalHashKey = incrementalHashKey xor getCastleRightKey(otherSide)
                        castleRightMap[otherSide] = CastleRight.KING_SIDE
                        incrementalHashKey = incrementalHashKey xor getCastleRightKey(otherSide)
                    }
                    CastleRight.QUEEN_SIDE -> {
                        incrementalHashKey = incrementalHashKey xor getCastleRightKey(otherSide)
                        castleRightMap[otherSide] = CastleRight.NONE
                    }
                    else -> {}
                }
            }
        }

        halfMoveCounter = if (capturedPiece == Piece.NONE) halfMoveCounter + 1 else 0
        enPassantTarget = Square.NONE
        enPassant = Square.NONE

        if (movingPiece.pieceType == PieceType.PAWN) {
            if (kotlin.math.abs(move.to.rank.ordinal - move.from.rank.ordinal) == 2) {
                val otherPawn = Piece.make(side.flip(), PieceType.PAWN)
                enPassant = findEnPassant(move.to, side)
                if (hasPiece(otherPawn, move.to.sideSquares) &&
                    verifyNotPinnedPiece(side, enPassant, move.to)
                ) {
                    enPassantTarget = move.to
                    incrementalHashKey = incrementalHashKey xor getEnPassantKey(enPassantTarget)
                }
            }
            halfMoveCounter = 0
        }

        if (side == Side.BLACK) moveCounter++
        sideToMove = side.flip()
        incrementalHashKey = incrementalHashKey xor getSideKey(sideToMove)

        if (updateHistory) history.addLast(incrementalHashKey)
        backup.add(backupMove)
        return true
    }

    fun undoMove(): Move? {
        if (backup.isEmpty()) return null
        val b = backup.removeLast()
        if (updateHistory && history.isNotEmpty()) history.removeLast()
        b.restore(this)
        return b.move
    }

    internal fun movePiece(move: Move, backup: MoveBackup): Piece =
        movePiece(move.from, move.to, move.promotion, backup)

    private fun movePiece(from: Square, to: Square, promotion: Piece, backup: MoveBackup): Piece {
        val movingPiece = getPiece(from)
        var capturedPiece = getPiece(to)

        unsetPiece(movingPiece, from)
        if (capturedPiece != Piece.NONE) unsetPiece(capturedPiece, to)
        if (promotion != Piece.NONE) setPiece(promotion, to) else setPiece(movingPiece, to)

        if (movingPiece.pieceType == PieceType.PAWN &&
            enPassantTarget != Square.NONE &&
            to.file != from.file &&
            capturedPiece == Piece.NONE
        ) {
            capturedPiece = getPiece(enPassantTarget)
            if (capturedPiece != Piece.NONE) {
                unsetPiece(capturedPiece, enPassantTarget)
                backup.capturedSquare = enPassantTarget
                backup.capturedPiece = capturedPiece
            }
        }
        return capturedPiece
    }

    internal fun undoMovePiece(move: Move) {
        val movingPiece = getPiece(move.to)
        unsetPiece(movingPiece, move.to)
        if (move.promotion != Piece.NONE) {
            setPiece(Piece.make(sideToMove, PieceType.PAWN), move.from)
        } else {
            setPiece(movingPiece, move.from)
        }
    }

    fun squareAttackedBy(square: Square, side: Side, occ: Long = getBitboard()): Long {
        var result = Bitboard.getPawnAttacks(side.flip(), square) and
                getBitboard(Piece.make(side, PieceType.PAWN)) and occ
        result = result or (Bitboard.getKnightAttacks(square, occ) and
                getBitboard(Piece.make(side, PieceType.KNIGHT)))
        result = result or (Bitboard.getBishopAttacks(occ, square) and
                (getBitboard(Piece.make(side, PieceType.BISHOP)) or
                        getBitboard(Piece.make(side, PieceType.QUEEN))))
        result = result or (Bitboard.getRookAttacks(occ, square) and
                (getBitboard(Piece.make(side, PieceType.ROOK)) or
                        getBitboard(Piece.make(side, PieceType.QUEEN))))
        result = result or (Bitboard.getKingAttacks(square, occ) and
                getBitboard(Piece.make(side, PieceType.KING)))
        return result
    }

    fun squareAttackedByPieceType(square: Square, side: Side, type: PieceType): Long {
        val occ = getBitboard()
        return when (type) {
            PieceType.PAWN -> Bitboard.getPawnAttacks(side.flip(), square) and
                    getBitboard(Piece.make(side, PieceType.PAWN))
            PieceType.KNIGHT -> Bitboard.getKnightAttacks(square, occ) and
                    getBitboard(Piece.make(side, PieceType.KNIGHT))
            PieceType.BISHOP -> Bitboard.getBishopAttacks(occ, square) and
                    getBitboard(Piece.make(side, PieceType.BISHOP))
            PieceType.ROOK -> Bitboard.getRookAttacks(occ, square) and
                    getBitboard(Piece.make(side, PieceType.ROOK))
            PieceType.QUEEN -> Bitboard.getQueenAttacks(occ, square) and
                    getBitboard(Piece.make(side, PieceType.QUEEN))
            PieceType.KING -> Bitboard.getKingAttacks(square, occ) and
                    getBitboard(Piece.make(side, PieceType.KING))
            else -> 0L
        }
    }

    fun getKingSquare(side: Side): Square {
        val piece = getBitboard(Piece.make(side, PieceType.KING))
        return if (piece != 0L) Square.squareAt(Bitboard.bitScanForward(piece)) else Square.NONE
    }

    val isKingAttacked: Boolean
        get() = squareAttackedBy(getKingSquare(sideToMove), sideToMove.flip()) != 0L

    fun isSquareAttackedBy(squares: List<Square>, side: Side): Boolean =
        squares.any { squareAttackedBy(it, side) != 0L }

    fun isMoveLegal(move: Move, fullValidation: Boolean): Boolean {
        val fromPiece = getPiece(move.from)
        val side = sideToMove
        val fromType = fromPiece.pieceType
        val capturedPiece = getPiece(move.to)

        if (fullValidation) {
            if (fromPiece == Piece.NONE) return false
            if (fromPiece.pieceSide == capturedPiece.pieceSide && capturedPiece != Piece.NONE) return false
            if (side != fromPiece.pieceSide) return false

            val pawnPromoting = fromPiece.pieceType == PieceType.PAWN && isPromoRank(side, move)
            val hasPromoPiece = move.promotion != Piece.NONE
            if (hasPromoPiece != pawnPromoting) return false

            if (fromType == PieceType.KING) {
                if (context.isKingSideCastle(move)) {
                    if (getCastleRight(side) == CastleRight.KING_AND_QUEEN_SIDE ||
                        getCastleRight(side) == CastleRight.KING_SIDE
                    ) {
                        if ((getBitboard() and context.getooAllSquaresBb(side)) == 0L) {
                            return !isSquareAttackedBy(context.getooSquares(side), side.flip())
                        }
                    }
                    return false
                }
                if (context.isQueenSideCastle(move)) {
                    if (getCastleRight(side) == CastleRight.KING_AND_QUEEN_SIDE ||
                        getCastleRight(side) == CastleRight.QUEEN_SIDE
                    ) {
                        if ((getBitboard() and context.getoooAllSquaresBb(side)) == 0L) {
                            return !isSquareAttackedBy(context.getoooSquares(side), side.flip())
                        }
                    }
                    return false
                }
            }
        }

        if (fromType == PieceType.KING) {
            if (squareAttackedBy(move.to, side.flip()) != 0L) return false
        }

        val kingSq = if (fromType == PieceType.KING) move.to else getKingSquare(side)
        val other = side.flip()
        val moveTo = move.to.bb
        val moveFrom = move.from.bb
        val ep = if (enPassantTarget != Square.NONE && move.to == enPassant &&
            fromType == PieceType.PAWN
        ) enPassantTarget.bb else 0L
        val allPieces = (getBitboard() xor moveFrom xor ep) or moveTo

        val bishopAndQueens = (getBitboard(Piece.make(other, PieceType.BISHOP)) or
                getBitboard(Piece.make(other, PieceType.QUEEN))) and moveTo.inv()
        if (bishopAndQueens != 0L &&
            (Bitboard.getBishopAttacks(allPieces, kingSq) and bishopAndQueens) != 0L
        ) return false

        val rookAndQueens = (getBitboard(Piece.make(other, PieceType.ROOK)) or
                getBitboard(Piece.make(other, PieceType.QUEEN))) and moveTo.inv()
        if (rookAndQueens != 0L &&
            (Bitboard.getRookAttacks(allPieces, kingSq) and rookAndQueens) != 0L
        ) return false

        val knights = getBitboard(Piece.make(other, PieceType.KNIGHT)) and moveTo.inv()
        if (knights != 0L &&
            (Bitboard.getKnightAttacks(kingSq, allPieces) and knights) != 0L
        ) return false

        val pawns = getBitboard(Piece.make(other, PieceType.PAWN)) and moveTo.inv() and ep.inv()
        return pawns == 0L || (Bitboard.getPawnAttacks(side, kingSq) and pawns) == 0L
    }

    fun isMated(): Boolean {
        if (!isKingAttacked) return false
        return MoveGenerator.generateLegalMoves(this).isEmpty()
    }

    fun isStaleMate(): Boolean {
        if (isKingAttacked) return false
        return MoveGenerator.generateLegalMoves(this).isEmpty()
    }

    fun legalMoves(): List<Move> = MoveGenerator.generateLegalMoves(this)

    fun clear() {
        sideToMove = Side.WHITE
        enPassantTarget = Square.NONE
        enPassant = Square.NONE
        moveCounter = 0
        halfMoveCounter = 0
        history.clear()
        bitboard.fill(0L)
        bbSide.fill(0L)
        occupation.fill(Piece.NONE)
        backup.clear()
        incrementalHashKey = 0L
    }

    fun loadFromFen(fen: String) {
        clear()
        val spaceIdx = fen.indexOf(' ')
        val squares = fen.substring(0, spaceIdx)
        val state = fen.substring(spaceIdx + 1)

        val ranks = squares.split("/")
        var rank = 7
        for (r in ranks) {
            var file = 0
            for (c in r) {
                if (c.isDigit()) {
                    file += c.digitToInt()
                } else {
                    val sq = Square.encode(Rank.allRanks[rank], ChessFile.allFiles[file])
                    setPiece(Constants.getPieceByNotation(c.toString()), sq)
                    file++
                }
            }
            rank--
        }

        sideToMove = if (state.lowercase()[0] == 'w') Side.WHITE else Side.BLACK

        if (state.contains("KQ")) castleRightMap[Side.WHITE] = CastleRight.KING_AND_QUEEN_SIDE
        else if (state.contains("K")) castleRightMap[Side.WHITE] = CastleRight.KING_SIDE
        else if (state.contains("Q")) castleRightMap[Side.WHITE] = CastleRight.QUEEN_SIDE
        else castleRightMap[Side.WHITE] = CastleRight.NONE

        if (state.contains("kq")) castleRightMap[Side.BLACK] = CastleRight.KING_AND_QUEEN_SIDE
        else if (state.contains("k")) castleRightMap[Side.BLACK] = CastleRight.KING_SIDE
        else if (state.contains("q")) castleRightMap[Side.BLACK] = CastleRight.QUEEN_SIDE
        else castleRightMap[Side.BLACK] = CastleRight.NONE

        val flags = state.split(" ")
        if (flags.size >= 3) {
            val s = flags[2].uppercase().trim()
            if (s != "-") {
                val ep = Square.valueOf(s)
                enPassant = ep
                enPassantTarget = findEnPassantTarget(ep, sideToMove)
                if (!(squareAttackedByPieceType(enPassant, sideToMove, PieceType.PAWN) != 0L &&
                            verifyNotPinnedPiece(sideToMove.flip(), enPassant, enPassantTarget))
                ) {
                    enPassantTarget = Square.NONE
                }
            }
            if (flags.size >= 4) {
                halfMoveCounter = flags[3].toIntOrNull() ?: 0
                if (flags.size >= 5) {
                    moveCounter = flags[4].toIntOrNull() ?: 1
                }
            }
        }

        incrementalHashKey = getZobristKey()
        if (updateHistory) history.addLast(incrementalHashKey)
    }

    fun getFen(includeCounters: Boolean = true): String {
        val fen = StringBuilder()
        var count = 0
        var rankCounter = 1
        var sqCount = 0

        for (i in 7 downTo 0) {
            val r = Rank.allRanks[i]
            for (n in 0..7) {
                val f = ChessFile.allFiles[n]
                if (f != ChessFile.NONE && r != Rank.NONE) {
                    val sq = Square.encode(r, f)
                    val piece = getPiece(sq)
                    if (piece != Piece.NONE) {
                        if (count > 0) { fen.append(count); count = 0 }
                        fen.append(Constants.getPieceNotation(piece))
                    } else {
                        count++
                    }
                    if ((sqCount + 1) % 8 == 0) {
                        if (count > 0) { fen.append(count); count = 0 }
                        if (rankCounter < 8) fen.append("/")
                        rankCounter++
                    }
                    sqCount++
                }
            }
        }

        fen.append(if (sideToMove == Side.WHITE) " w" else " b")

        var rights = ""
        when (castleRightMap[Side.WHITE]) {
            CastleRight.KING_AND_QUEEN_SIDE -> rights += "KQ"
            CastleRight.KING_SIDE -> rights += "K"
            CastleRight.QUEEN_SIDE -> rights += "Q"
            else -> {}
        }
        when (castleRightMap[Side.BLACK]) {
            CastleRight.KING_AND_QUEEN_SIDE -> rights += "kq"
            CastleRight.KING_SIDE -> rights += "k"
            CastleRight.QUEEN_SIDE -> rights += "q"
            else -> {}
        }
        fen.append(if (rights.isEmpty()) " -" else " $rights")

        if (enPassant == Square.NONE) fen.append(" -")
        else fen.append(" ${enPassant.name.lowercase()}")

        if (includeCounters) {
            fen.append(" $halfMoveCounter $moveCounter")
        }
        return fen.toString()
    }

    fun boardToArray(): Array<Piece> {
        val pieces = Array(65) { Piece.NONE }
        for (sq in Square.allSquares) {
            if (sq != Square.NONE) pieces[sq.ordinal] = getPiece(sq)
        }
        return pieces
    }

    private fun getZobristKey(): Long {
        var hash = 0L
        if (getCastleRight(Side.WHITE) != CastleRight.NONE) hash = hash xor getCastleRightKey(Side.WHITE)
        if (getCastleRight(Side.BLACK) != CastleRight.NONE) hash = hash xor getCastleRightKey(Side.BLACK)
        for (sq in Square.allSquares) {
            val piece = getPiece(sq)
            if (piece != Piece.NONE && sq != Square.NONE) hash = hash xor getPieceSquareKey(piece, sq)
        }
        hash = hash xor getSideKey(sideToMove)
        if (enPassantTarget != Square.NONE &&
            squareAttackedByPieceType(enPassant, sideToMove, PieceType.PAWN) != 0L &&
            verifyNotPinnedPiece(sideToMove.flip(), enPassant, enPassantTarget)
        ) {
            hash = hash xor getEnPassantKey(enPassantTarget)
        }
        return hash
    }

    private fun verifyNotPinnedPiece(side: Side, enPassantSq: Square, target: Square): Boolean {
        val pawns = Bitboard.getPawnAttacks(side, enPassantSq) and
                getBitboard(Piece.make(side.flip(), PieceType.PAWN))
        return pawns != 0L && verifyAllPins(pawns, side, enPassantSq, target)
    }

    private fun verifyAllPins(pawns: Long, side: Side, enPassantSq: Square, target: Square): Boolean {
        val onePawn = Bitboard.extractLsb(pawns)
        val otherPawn = pawns xor onePawn
        if (onePawn != 0L && verifyKingNotAttackedWithoutPin(side, enPassantSq, target, onePawn)) return true
        return verifyKingNotAttackedWithoutPin(side, enPassantSq, target, otherPawn)
    }

    private fun verifyKingNotAttackedWithoutPin(
        side: Side, enPassantSq: Square, target: Square, pawns: Long
    ): Boolean {
        val removedPieces = (getBitboard() xor pawns xor target.bb) or enPassantSq.bb
        return squareAttackedBy(getKingSquare(side.flip()), side, removedPieces) == 0L
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (i in 7 downTo 0) {
            val r = Rank.allRanks[i]
            for (n in 0..7) {
                val f = ChessFile.allFiles[n]
                val sq = Square.encode(r, f)
                val piece = getPiece(sq)
                sb.append(if (piece == Piece.NONE) "." else Constants.getPieceNotation(piece))
            }
            sb.append("\n")
        }
        sb.append("Side: $sideToMove")
        return sb.toString()
    }

    companion object {
        private val keys: LongArray by lazy {
            val random = XorShiftRandom(49109794719L)
            LongArray(2000) { random.nextLong() }
        }

        private fun getCastleRightKey(board: Board, side: Side): Long =
            keys[3 * board.getCastleRight(side).ordinal + 300 + 3 * side.ordinal]

        private fun getSideKeyStatic(side: Side): Long = keys[3 * side.ordinal + 500]

        private fun isPromoRank(side: Side, move: Move): Boolean =
            (side == Side.WHITE && move.to.rank == Rank.RANK_8) ||
                    (side == Side.BLACK && move.to.rank == Rank.RANK_1)

        private fun findEnPassantTarget(sq: Square, side: Side): Square {
            if (sq == Square.NONE) return Square.NONE
            return if (side == Side.WHITE) Square.encode(Rank.RANK_5, sq.file)
            else Square.encode(Rank.RANK_4, sq.file)
        }

        private fun findEnPassant(sq: Square, side: Side): Square {
            if (sq == Square.NONE) return Square.NONE
            return if (side == Side.WHITE) Square.encode(Rank.RANK_3, sq.file)
            else Square.encode(Rank.RANK_6, sq.file)
        }
    }

    private fun getCastleRightKey(side: Side): Long =
        Companion.keys[3 * getCastleRight(side).ordinal + 300 + 3 * side.ordinal]

    private fun getSideKey(side: Side): Long = Companion.keys[3 * side.ordinal + 500]
    private fun getEnPassantKey(ep: Square): Long = Companion.keys[3 * ep.ordinal + 400]
    private fun getPieceSquareKey(piece: Piece, square: Square): Long =
        Companion.keys[57 * piece.ordinal + 13 * square.ordinal]
}

private class XorShiftRandom(private var seed: Long) {
    fun nextLong(): Long {
        seed = seed xor (seed shl 21)
        seed = seed xor (seed ushr 35)
        seed = seed xor (seed shl 4)
        return seed
    }
}
