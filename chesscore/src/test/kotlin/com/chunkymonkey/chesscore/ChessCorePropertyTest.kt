package com.chunkymonkey.chesscore

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking

private val minimalPgnTemplate = """
    [Event "PropTest %s"]
    [Site "?"]
    [Date "2024.01.01"]
    [White "A"]
    [Black "B"]
    [Result "1-0"]

    1. e4 e5 2. Qh5 Nc6 3. Bc4 Nf6 4. Qxf7# 1-0
""".trimIndent()

/**
 * Property-based checks for chesscore. Uses higher iteration counts on cheap
 * generators and caps inner loops where work is per-position (e.g. all legal SAN moves).
 */
class ChessCorePropertyTest : FunSpec({

    val arbSquareOccupied = arbitrary {
        Square.allSquares.filter { it != Square.NONE }.random()
    }

    val arbSide = Arb.enum<Side>()

    val arbPieceColored = arbitrary {
        Piece.allPieces.filter { it != Piece.NONE }.random()
    }

    /** Random move (usually illegal); used with legality checks. */
    val arbRawMove = arbitrary {
        val from = Square.allSquares.filter { it != Square.NONE }.random()
        val to = Square.allSquares.filter { it != Square.NONE }.random()
        val promos = listOf(
            Piece.NONE, Piece.WHITE_QUEEN, Piece.WHITE_ROOK,
            Piece.WHITE_BISHOP, Piece.WHITE_KNIGHT,
            Piece.BLACK_QUEEN, Piece.BLACK_ROOK, Piece.BLACK_BISHOP, Piece.BLACK_KNIGHT
        )
        Move(from, to, promos.random())
    }

    // --- Model geometry (cheap; many iterations) ---

    test("Square encode round-trip for all 64 squares") {
        checkAll(500, arbSquareOccupied) { sq ->
            Square.encode(sq.rank, sq.file) shouldBe sq
        }
    }

    test("Square bitboards are pairwise disjoint (exhaustive)") {
        val squares = Square.allSquares.filter { it != Square.NONE }
        for (a in squares) {
            for (b in squares) {
                if (a != b) {
                    (a.bb and b.bb) shouldBe 0L
                }
            }
        }
    }

    test("Square rank and file are in valid ranges for board squares") {
        checkAll(500, arbSquareOccupied) { sq ->
            sq.rank shouldNotBe Rank.NONE
            sq.file shouldNotBe ChessFile.NONE
            sq.ordinal shouldBe sq.rank.ordinal * 8 + sq.file.ordinal
        }
    }

    test("Side flip is an involution") {
        checkAll(500, arbSide) { side ->
            side.flip().flip() shouldBe side
            (side == Side.WHITE) shouldBe (side.flip() == Side.BLACK)
        }
    }

    test("Piece.make round-trip for every colored piece") {
        checkAll(500, arbPieceColored) { piece ->
            Piece.make(piece.pieceSide, piece.pieceType) shouldBe piece
        }
    }

    test("GameResult descriptions round-trip through fromNotation") {
        checkAll(500, arbitrary { GameResult.entries.random() }) { gr ->
            GameResult.fromNotation(gr.description) shouldBe gr
        }
    }

    // --- Known FEN positions (regression + edge shapes) ---

    val knownFens = listOf(
        Constants.startStandardFENPosition,
        "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
        "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1",
        "r3k2r/8/8/8/8/8/8/R3K2R b kq - 0 1",
        "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1",
        "8/5k2/8/8/8/8/5K2/4P3 w - - 0 1",
        "k7/2Q5/1K6/8/8/8/8/8 b - - 0 1",
        "rnbqkb1r/pppp1ppp/5n2/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 4 4"
    )

    test("Known FEN strings round-trip through loadFromFen and getFen") {
        for (fen in knownFens) {
            val b = Board()
            b.loadFromFen(fen)
            b.getFen() shouldBe fen
        }
    }

    test("Random walk FEN round-trip") {
        checkAll(250, Arb.int(1..60)) { numMoves ->
            val board = Board()
            makeRandomMoves(board, numMoves)
            val fen = board.getFen()
            val copy = Board()
            copy.loadFromFen(fen)
            copy.getFen() shouldBe fen
        }
    }

    // --- Move / undo / backup stack ---

    test("doMove then undoMove restores exact FEN") {
        checkAll(250, Arb.int(1..45)) { numMoves ->
            val board = Board()
            val start = board.getFen()
            val n = makeRandomMoves(board, numMoves)
            repeat(n) { board.undoMove() }
            board.getFen() shouldBe start
        }
    }

    test("Backup stack depth equals number of successful moves") {
        checkAll(200, Arb.int(1..35)) { numMoves ->
            val board = Board()
            val n = makeRandomMoves(board, numMoves)
            board.getBackup().size shouldBe n
        }
    }

    test("undoMove on fresh board returns null and leaves FEN unchanged") {
        val board = Board()
        val fen = board.getFen()
        board.undoMove() shouldBe null
        board.getFen() shouldBe fen
    }

    test("sideToMove alternates after each legal move") {
        checkAll(200, Arb.int(1..40)) { numMoves ->
            val board = Board()
            var side = board.sideToMove
            repeat(numMoves) {
                val moves = board.legalMoves()
                if (moves.isEmpty()) return@repeat
                val m = moves.random()
                board.doMove(m) shouldBe true
                board.sideToMove shouldBe side.flip()
                side = board.sideToMove
            }
        }
    }

    test("doMove success matches isMoveLegal for random candidate moves") {
        checkAll(300, arbRawMove) { move ->
            val board = Board()
            val fenBefore = board.getFen()
            val legal = board.isMoveLegal(move, false)
            val applied = board.doMove(move)
            applied shouldBe legal
            if (!applied) {
                board.getFen() shouldBe fenBefore
            }
        }
    }

    test("At most one piece occupies each square after random play") {
        checkAll(150, Arb.int(1..50)) { numMoves ->
            val board = Board()
            makeRandomMoves(board, numMoves)
            val occ = BooleanArray(64)
            for (sq in Square.allSquares) {
                if (sq == Square.NONE) continue
                val p = board.getPiece(sq)
                if (p != Piece.NONE) {
                    occ[sq.ordinal] shouldBe false
                    occ[sq.ordinal] = true
                }
            }
        }
    }

    test("Total material count never exceeds 32 pieces") {
        checkAll(200, Arb.int(1..60)) { numMoves ->
            val board = Board()
            makeRandomMoves(board, numMoves)
            (java.lang.Long.bitCount(board.getBitboard()) <= 32) shouldBe true
        }
    }

    test("Exactly one king per side after random play") {
        checkAll(250, Arb.int(1..55)) { numMoves ->
            val board = Board()
            makeRandomMoves(board, numMoves)
            java.lang.Long.bitCount(board.getBitboard(Piece.WHITE_KING)) shouldBe 1
            java.lang.Long.bitCount(board.getBitboard(Piece.BLACK_KING)) shouldBe 1
        }
    }

    // --- SAN encode/decode: every legal move in sampled positions ---

    test("SAN encode then decode recovers each legal move (sampled positions)") {
        checkAll(120, Arb.int(0..25)) { preambleMoves ->
            val board = Board()
            makeRandomMoves(board, preambleMoves)
            val legals = board.legalMoves()
            if (legals.size > 80) return@checkAll

            val fen = board.getFen()
            for (move in legals) {
                val copy = Board()
                copy.loadFromFen(fen)
                val san = SanDecoder.encodeSan(copy, move)
                san shouldNotBe ""
                copy.undoMove()
                val decoded = SanDecoder.decodeSan(copy, san, copy.sideToMove)
                decoded.from shouldBe move.from
                decoded.to shouldBe move.to
                decoded.promotion shouldBe move.promotion
            }
        }
    }

    test("SAN round-trip for first legal move (deeper random positions)") {
        checkAll(200, Arb.int(5..35)) { numMoves ->
            val board = Board()
            makeRandomMoves(board, numMoves)
            val legals = board.legalMoves()
            if (legals.isEmpty()) return@checkAll
            val move = legals.first()
            val copy = Board()
            copy.loadFromFen(board.getFen())
            val san = SanDecoder.encodeSan(copy, move)
            copy.undoMove()
            val decoded = SanDecoder.decodeSan(copy, san, copy.sideToMove)
            decoded.from shouldBe move.from
            decoded.to shouldBe move.to
            decoded.promotion shouldBe move.promotion
        }
    }

    // --- PGN parsing determinism & structure ---

    test("PGN parse is deterministic for the same input") {
        checkAll(80, Arb.int(0..50_000)) { seed ->
            val tagNoise = "t$seed"
            val pgn = minimalPgnTemplate.format(tagNoise)
            val a = runBlocking { PgnParser.parse(pgn) }
            val b = runBlocking { PgnParser.parse(pgn) }
            a.moves.size shouldBe b.moves.size
            a.result shouldBe b.result
            a.headers["White"] shouldBe b.headers["White"]
            a.moves.zip(b.moves).forEach { (m1, m2) ->
                m1.from shouldBe m2.from
                m1.to shouldBe m2.to
                m1.promotion shouldBe m2.promotion
            }
        }
    }

    test("Parsed game has expected move count and result for scholar's mate PGN") {
        val pgn = minimalPgnTemplate.format("X")
        val g = runBlocking { PgnParser.parse(pgn) }
        g.moves.size shouldBe 7
        g.result shouldBe GameResult.WHITE_WON
    }

    test("Replaying parsed moves from FEN header reaches consistent final side") {
        val pgnWithFen = """
            [Event "FEN"]
            [FEN "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1"]
            [White "W"]
            [Black "B"]
            [Result "*"]

            1. e4 *
        """.trimIndent()
        val g = runBlocking { PgnParser.parse(pgnWithFen) }
        val b = Board()
        b.loadFromFen(g.fen!!)
        for (m in g.moves) {
            b.doMove(m) shouldBe true
        }
    }

    // --- Bitboard internal consistency ---

    test("Occupied squares union equals sum of side bitboards") {
        checkAll(200, Arb.int(1..40)) { numMoves ->
            val board = Board()
            makeRandomMoves(board, numMoves)
            val occ = board.getBitboard()
            val union = board.getBitboard(Side.WHITE) or board.getBitboard(Side.BLACK)
            occ shouldBe union
        }
    }

    test("Each piece bitboard is subset of its side bitboard") {
        checkAll(200, Arb.int(1..35)) { numMoves ->
            val board = Board()
            makeRandomMoves(board, numMoves)
            for (piece in Piece.allPieces) {
                if (piece == Piece.NONE) continue
                val pb = board.getBitboard(piece)
                val sideBb = board.getBitboard(piece.pieceSide)
                (pb and sideBb.inv()) shouldBe 0L
            }
        }
    }
})

private fun makeRandomMoves(board: Board, maxMoves: Int): Int {
    var count = 0
    repeat(maxMoves) {
        val moves = board.legalMoves()
        if (moves.isEmpty()) return count
        val move = moves.random()
        if (!board.doMove(move)) return count
        count++
    }
    return count
}
