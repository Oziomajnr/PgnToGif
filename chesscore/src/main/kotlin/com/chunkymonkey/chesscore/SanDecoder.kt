package com.chunkymonkey.chesscore

class PgnParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

object SanDecoder {

    private val nullMove = Move(Square.NONE, Square.NONE)

    private val sanNotationR = mapOf(
        "" to PieceType.PAWN,
        "N" to PieceType.KNIGHT,
        "B" to PieceType.BISHOP,
        "R" to PieceType.ROOK,
        "Q" to PieceType.QUEEN,
        "K" to PieceType.KING,
        "NONE" to PieceType.NONE
    )

    fun decodeSan(board: Board, rawSan: String, side: Side): Move {
        if (rawSan.equals("Z0", ignoreCase = true)) return nullMove

        var san = normalizeSan(rawSan)

        var strPromotion = afterSequence(san, "=", 1)
        san = beforeSequence(san, "=")

        val lastChar = san.last()
        if (lastChar.isLetter() && lastChar.uppercaseChar() != 'O') {
            san = san.substring(0, san.length - 1)
            strPromotion = lastChar.toString()
        }

        if (san == "O-O" || san == "O-O-O") {
            return if (san == "O-O") board.context.getoo(side) else board.context.getooo(side)
        }

        if (san.length == 3 && san[2].isUpperCase()) {
            strPromotion = san.substring(2, 3)
            san = san.substring(0, 2)
        }

        var from = Square.NONE
        val to: Square
        try {
            to = Square.valueOf(lastSequence(san.uppercase(), 2))
        } catch (e: Exception) {
            throw PgnParseException("Couldn't parse destination square[$rawSan]: ${san.uppercase()}")
        }

        val promotion = if (strPromotion.isEmpty()) Piece.NONE else
            Constants.getPieceByNotation(
                if (side == Side.WHITE) strPromotion.uppercase() else strPromotion.lowercase()
            )

        if (san.length == 2) {
            val mask = Bitboard.getBbtable(to) - 1L
            val xfrom = (if (side == Side.WHITE) mask else mask.inv()) and
                    Bitboard.getFilebb(to) and
                    board.getBitboard(Piece.make(side, PieceType.PAWN))
            val f = if (side == Side.BLACK) Bitboard.bitScanForward(xfrom) else Bitboard.bitScanReverse(xfrom)
            if (f in 0..63) from = Square.squareAt(f)
        } else {
            val strFrom = if (san.contains("x"))
                san.substringBefore("x") else san.substring(0, san.length - 2)

            if (strFrom.isEmpty() || strFrom.length > 3) {
                throw PgnParseException("Couldn't parse 'from' square $rawSan: Too many/few characters.")
            }

            var fromPiece = PieceType.PAWN
            if (strFrom[0].isUpperCase()) {
                fromPiece = sanNotationR[strFrom[0].toString()] ?: PieceType.PAWN
            }

            if (strFrom.length == 3) {
                from = Square.valueOf(strFrom.substring(1, 3).uppercase())
            } else {
                var location = ""
                if (strFrom.length == 2) {
                    if (strFrom[0].isUpperCase()) {
                        location = strFrom.substring(1, 2)
                    } else {
                        location = strFrom.substring(0, 2)
                        from = Square.valueOf(location.uppercase())
                    }
                } else {
                    if (strFrom[0].isLowerCase()) location = strFrom
                }

                if (location.length < 2) {
                    var xfrom = board.squareAttackedByPieceType(to, board.sideToMove, fromPiece)
                    if (location.isNotEmpty()) {
                        if (location[0].isDigit()) {
                            val irank = location[0].digitToInt()
                            if (irank !in 1..8) throw PgnParseException("Couldn't parse rank: $location")
                            val rank = Rank.allRanks[irank - 1]
                            xfrom = xfrom and Bitboard.getRankbb(rank)
                        } else {
                            try {
                                val file = ChessFile.valueOf("FILE_${location.uppercase()}")
                                xfrom = xfrom and Bitboard.getFilebb(file)
                            } catch (e: Exception) {
                                throw PgnParseException("Couldn't parse file: $location")
                            }
                        }
                    }
                    if (xfrom != 0L) {
                        if (!Bitboard.hasOnly1Bit(xfrom)) {
                            xfrom = findLegalSquares(board, to, promotion, xfrom)
                        }
                        val f = Bitboard.bitScanForward(xfrom)
                        if (f in 0..63) from = Square.squareAt(f)
                    }
                }
            }
        }

        if (from == Square.NONE) {
            throw PgnParseException("Couldn't parse 'from' square $rawSan to setup: ${board.getFen()}")
        }
        return Move(from, to, promotion)
    }

    fun encodeSan(board: Board, move: Move): String {
        val san = StringBuilder()
        val piece = board.getPiece(move.from)
        val sanNotation = mapOf(
            Piece.WHITE_PAWN to "", Piece.BLACK_PAWN to "",
            Piece.WHITE_KNIGHT to "N", Piece.BLACK_KNIGHT to "N",
            Piece.WHITE_BISHOP to "B", Piece.BLACK_BISHOP to "B",
            Piece.WHITE_ROOK to "R", Piece.BLACK_ROOK to "R",
            Piece.WHITE_QUEEN to "Q", Piece.BLACK_QUEEN to "Q",
            Piece.WHITE_KING to "K", Piece.BLACK_KING to "K"
        )

        if (piece.pieceType == PieceType.KING) {
            val delta = move.to.file.ordinal - move.from.file.ordinal
            if (kotlin.math.abs(delta) >= 2) {
                if (!board.doMove(move, true)) {
                    throw PgnParseException("Invalid move [$move] for current setup: ${board.getFen()}")
                }
                san.append(if (delta > 0) "O-O" else "O-O-O")
                addCheckFlag(board, san)
                return san.toString()
            }
        }

        val pawnMove = piece.pieceType == PieceType.PAWN && move.from.file == move.to.file
        var ambResolved = false
        san.append(sanNotation[piece] ?: "")

        if (!pawnMove) {
            var amb = board.squareAttackedByPieceType(move.to, board.sideToMove, piece.pieceType)
            amb = amb and move.from.bb.inv()
            if (amb != 0L) {
                val fromList = Bitboard.bbToSquareList(amb)
                for (sq in fromList) {
                    if (!board.isMoveLegal(Move(sq, move.to), false)) {
                        amb = amb xor sq.bb
                    }
                }
            }
            if (amb != 0L) {
                if ((Bitboard.getFilebb(move.from) and amb) == 0L) {
                    san.append(move.from.file.notation.lowercase())
                } else if ((Bitboard.getRankbb(move.from) and amb) == 0L) {
                    san.append(move.from.rank.notation.lowercase())
                } else {
                    san.append(move.from.name.lowercase())
                }
                ambResolved = true
            }
        }

        if (!board.doMove(move, true)) {
            throw PgnParseException("Invalid move [$move] for current setup: ${board.getFen()}")
        }

        val captured = board.getBackup().last.capturedPiece
        val isCapture = captured != Piece.NONE
        if (isCapture) {
            if (!ambResolved && piece.pieceType == PieceType.PAWN) {
                san.append(move.from.file.notation.lowercase())
            }
            san.append("x")
        }
        san.append(move.to.name.lowercase())
        if (move.promotion != Piece.NONE) {
            san.append("=")
            san.append(sanNotation[move.promotion] ?: "")
        }
        addCheckFlag(board, san)
        return san.toString()
    }

    private fun addCheckFlag(board: Board, san: StringBuilder) {
        if (board.isKingAttacked) {
            san.append(if (board.isMated()) "#" else "+")
        }
    }

    private fun findLegalSquares(board: Board, to: Square, promotion: Piece, pieces: Long): Long {
        var result = 0L
        var remaining = pieces
        while (remaining != 0L) {
            val sqIdx = Bitboard.bitScanForward(remaining)
            remaining = Bitboard.extractLsb(remaining)
            val sqSource = Square.squareAt(sqIdx)
            val move = Move(sqSource, to, promotion)
            if (board.isMoveLegal(move, true)) {
                result = result or sqSource.bb
                break
            }
        }
        return result
    }

    private fun normalizeSan(san: String): String =
        san.replace("+", "").replace("#", "")
            .replace("!", "").replace("?", "")
            .replace("ep", "").replace("\n", " ").trim()

    private fun lastSequence(s: String, len: Int): String =
        if (s.length >= len) s.substring(s.length - len) else s

    private fun afterSequence(s: String, seq: String, len: Int = 0): String {
        val idx = s.indexOf(seq)
        return if (idx >= 0) s.substring(idx + seq.length + len) else ""
    }

    private fun beforeSequence(s: String, seq: String): String {
        val idx = s.indexOf(seq)
        return if (idx >= 0) s.substring(0, idx) else s
    }
}
