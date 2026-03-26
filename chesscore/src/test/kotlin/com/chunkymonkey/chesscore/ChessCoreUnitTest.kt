package com.chunkymonkey.chesscore

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldNotBeEmpty

class ChessCoreUnitTest : FunSpec({

    test("Board initializes to standard position") {
        val board = Board()
        board.sideToMove shouldBe Side.WHITE
        board.getPiece(Square.E1) shouldBe Piece.WHITE_KING
        board.getPiece(Square.E8) shouldBe Piece.BLACK_KING
        board.getPiece(Square.D1) shouldBe Piece.WHITE_QUEEN
        board.getPiece(Square.A2) shouldBe Piece.WHITE_PAWN
        board.getPiece(Square.E4) shouldBe Piece.NONE
    }

    test("Board generates legal moves from starting position") {
        val board = Board()
        val moves = board.legalMoves()
        moves.size shouldBe 20
    }

    test("FEN loading and saving") {
        val fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
        val board = Board()
        board.loadFromFen(fen)
        board.sideToMove shouldBe Side.BLACK
        board.getPiece(Square.E4) shouldBe Piece.WHITE_PAWN
        board.getPiece(Square.E2) shouldBe Piece.NONE
        board.getFen() shouldBe fen
    }

    test("doMove executes e2-e4 correctly") {
        val board = Board()
        val move = Move(Square.E2, Square.E4)
        board.doMove(move) shouldBe true
        board.getPiece(Square.E4) shouldBe Piece.WHITE_PAWN
        board.getPiece(Square.E2) shouldBe Piece.NONE
        board.sideToMove shouldBe Side.BLACK
    }

    test("undoMove restores position") {
        val board = Board()
        val fenBefore = board.getFen()
        val move = Move(Square.E2, Square.E4)
        board.doMove(move)
        board.undoMove()
        board.getFen() shouldBe fenBefore
    }

    test("Scholar's mate - checkmate detection") {
        val board = Board()
        board.doMove(Move(Square.E2, Square.E4)) // 1. e4
        board.doMove(Move(Square.E7, Square.E5)) // 1... e5
        board.doMove(Move(Square.F1, Square.C4)) // 2. Bc4
        board.doMove(Move(Square.B8, Square.C6)) // 2... Nc6
        board.doMove(Move(Square.D1, Square.H5)) // 3. Qh5
        board.doMove(Move(Square.G8, Square.F6)) // 3... Nf6
        board.doMove(Move(Square.H5, Square.F7, Piece.NONE)) // 4. Qxf7#

        board.isMated() shouldBe true
    }

    test("Castling king-side") {
        val fen = "r1bqk1nr/ppppbppp/2n5/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4"
        val board = Board()
        board.loadFromFen(fen)
        val castleMove = Move(Square.E1, Square.G1)
        board.doMove(castleMove) shouldBe true
        board.getPiece(Square.G1) shouldBe Piece.WHITE_KING
        board.getPiece(Square.F1) shouldBe Piece.WHITE_ROOK
        board.getPiece(Square.E1) shouldBe Piece.NONE
        board.getPiece(Square.H1) shouldBe Piece.NONE
    }

    test("Castling queen-side") {
        val fen = "r3kbnr/pppqpppp/2n1b3/3p4/3P4/2N1BN2/PPPQPPPP/R3KB1R w KQkq - 6 6"
        val board = Board()
        board.loadFromFen(fen)

        val castleMove = Move(Square.E1, Square.C1)
        board.doMove(castleMove) shouldBe true
        board.getPiece(Square.C1) shouldBe Piece.WHITE_KING
        board.getPiece(Square.D1) shouldBe Piece.WHITE_ROOK
    }

    test("En passant capture") {
        val fen = "rnbqkbnr/pppp1ppp/8/4pP2/8/8/PPPPP1PP/RNBQKBNR w KQkq e6 0 3"
        val board = Board()
        board.loadFromFen(fen)

        val epMove = Move(Square.F5, Square.E6)
        board.doMove(epMove) shouldBe true
        board.getPiece(Square.E6) shouldBe Piece.WHITE_PAWN
        board.getPiece(Square.E5) shouldBe Piece.NONE
        board.getPiece(Square.F5) shouldBe Piece.NONE
    }

    test("Pawn promotion") {
        val fen = "8/4P3/8/8/8/8/4k3/4K3 w - - 0 1"
        val board = Board()
        board.loadFromFen(fen)

        val promoMove = Move(Square.E7, Square.E8, Piece.WHITE_QUEEN)
        board.doMove(promoMove) shouldBe true
        board.getPiece(Square.E8) shouldBe Piece.WHITE_QUEEN
    }

    test("SAN decode basic pawn move") {
        val board = Board()
        val move = SanDecoder.decodeSan(board, "e4", Side.WHITE)
        move.from shouldBe Square.E2
        move.to shouldBe Square.E4
    }

    test("SAN decode knight move") {
        val board = Board()
        val move = SanDecoder.decodeSan(board, "Nf3", Side.WHITE)
        move.from shouldBe Square.G1
        move.to shouldBe Square.F3
    }

    test("SAN decode castling O-O") {
        val board = Board()
        val move = SanDecoder.decodeSan(board, "O-O", Side.WHITE)
        move.from shouldBe Square.E1
        move.to shouldBe Square.G1
    }

    test("SAN decode castling O-O-O") {
        val board = Board()
        val move = SanDecoder.decodeSan(board, "O-O-O", Side.WHITE)
        move.from shouldBe Square.E1
        move.to shouldBe Square.C1
    }

    test("SAN encode/decode round-trip for e4") {
        val board = Board()
        val move = Move(Square.E2, Square.E4)
        val san = SanDecoder.encodeSan(board, move)
        san shouldBe "e4"

        board.undoMove()
        val decoded = SanDecoder.decodeSan(board, san, Side.WHITE)
        decoded.from shouldBe move.from
        decoded.to shouldBe move.to
    }

    test("PGN parsing - known game hikaru vs rybka") {
        val pgn = this::class.java.classLoader.getResource("pgn/hikaru_vs_rybka_long.pgn")!!.readText()
        val game = kotlinx.coroutines.runBlocking { PgnParser.parse(pgn) }
        game.whitePlayer shouldBe "Rybka"
        game.blackPlayer shouldBe "Hikaru Nakamura"
        game.result shouldBe GameResult.DRAW
        game.moves.shouldNotBeEmpty()
        game.whiteElo shouldBe 2800
        game.blackElo shouldBe 2697
    }

    test("PGN parsing - simple game") {
        val pgn = """
            [Event "Test"]
            [Site "?"]
            [Date "2024.01.01"]
            [White "Player1"]
            [Black "Player2"]
            [Result "1-0"]

            1. e4 e5 2. Qh5 Nc6 3. Bc4 Nf6 4. Qxf7# 1-0
        """.trimIndent()

        val game = kotlinx.coroutines.runBlocking { PgnParser.parse(pgn) }
        game.whitePlayer shouldBe "Player1"
        game.blackPlayer shouldBe "Player2"
        game.result shouldBe GameResult.WHITE_WON
        game.moves.size shouldBe 7
    }

    test("PGN parsing with comments and variations") {
        val pgn = """
            [Event "Test"]
            [White "A"]
            [Black "B"]
            [Result "*"]

            1. e4 {A common opening} e5 (1... c5 {Sicilian}) 2. Nf3 *
        """.trimIndent()

        val game = kotlinx.coroutines.runBlocking { PgnParser.parse(pgn) }
        game.moves.size shouldBe 3
    }

    test("stripBraceComments handles Lichess %clk and ECO comments without regex") {
        val stripped = PgnParser.stripBraceComments(
            "2. Bc4 { [%clk 0:00:59] } { C23 Bishop's Opening } 2... Nc6"
        )
        (stripped.contains("{")) shouldBe false
        (stripped.contains("clk")) shouldBe false
        (stripped.contains("C23")) shouldBe false
        stripped.replace(Regex("\\s+"), " ").trim() shouldBe "2. Bc4 2... Nc6"
    }

    test("PGN parses Lichess move text with %clk and brace comments") {
        val pgn = """
            [Event "Hourly Bullet Arena"]
            [White "W"]
            [Black "B"]
            [Result "0-1"]

            1. e4 { [%clk 0:01:00] } 1... e5 { [%clk 0:01:00] } 2. Bc4 { [%clk 0:00:59] } { C23 Bishop's Opening } 2... Nc6 { [%clk 0:01:00] } 0-1
        """.trimIndent()

        val game = kotlinx.coroutines.runBlocking { PgnParser.parse(pgn) }
        game.result shouldBe GameResult.BLACK_WON
        game.moves.size shouldBe 4
    }

    test("boardToArray returns 65 elements") {
        val board = Board()
        val arr = board.boardToArray()
        arr.size shouldBe 65
        arr[Square.E1.ordinal] shouldBe Piece.WHITE_KING
        arr[64] shouldBe Piece.NONE
    }

    test("Square file and rank") {
        Square.E4.file shouldBe ChessFile.FILE_E
        Square.E4.rank shouldBe Rank.RANK_4
        Square.A1.file shouldBe ChessFile.FILE_A
        Square.A1.rank shouldBe Rank.RANK_1
        Square.H8.file shouldBe ChessFile.FILE_H
        Square.H8.rank shouldBe Rank.RANK_8
    }

    test("Square NONE properties") {
        Square.NONE.file shouldBe ChessFile.NONE
        Square.NONE.rank shouldBe Rank.NONE
        Square.NONE.bb shouldBe 0L
    }

    test("Stalemate detection") {
        // Classic stalemate: black king trapped in corner by queen and king
        val fen = "k7/2Q5/1K6/8/8/8/8/8 b - - 0 1"
        val board = Board()
        board.loadFromFen(fen)
        board.isKingAttacked shouldBe false
        board.legalMoves().size shouldBe 0
        board.isStaleMate() shouldBe true
    }

    test("King attack detection") {
        val fen = "rnbqkbnr/pppp1ppp/8/4p3/5PP1/8/PPPPP2P/RNBQKBNR b KQkq g3 0 2"
        val board = Board()
        board.loadFromFen(fen)

        val moves = board.legalMoves()
        val qh4 = moves.find { it.from == Square.D8 && it.to == Square.H4 }
        qh4 shouldNotBe null
    }

    test("GameResult fromNotation") {
        GameResult.fromNotation("1-0") shouldBe GameResult.WHITE_WON
        GameResult.fromNotation("0-1") shouldBe GameResult.BLACK_WON
        GameResult.fromNotation("1/2-1/2") shouldBe GameResult.DRAW
        GameResult.fromNotation("*") shouldBe GameResult.ONGOING
    }

    test("Knight attack mask for G6 matches chess geometry (no attack on D4)") {
        (Bitboard.knightAttacks[Square.G6.ordinal] and Square.D4.bb) shouldBe 0L
    }

    test("SAN encode/decode all legals (property failure FEN)") {
        val fen = "r1bqkbn1/pp1p1p1r/n1p3Np/4p3/1P3P1P/8/P1PPP1P1/RNBQKB1R w KQq - 1 7"
        val board = Board()
        board.loadFromFen(fen)
        for (move in board.legalMoves()) {
            val copy = Board()
            copy.loadFromFen(fen)
            val san = SanDecoder.encodeSan(copy, move)
            copy.undoMove()
            val decoded = SanDecoder.decodeSan(copy, san, copy.sideToMove)
            decoded.from shouldBe move.from
            decoded.to shouldBe move.to
            decoded.promotion shouldBe move.promotion
        }
    }
})
