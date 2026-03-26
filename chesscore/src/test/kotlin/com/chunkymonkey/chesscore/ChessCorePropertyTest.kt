package com.chunkymonkey.chesscore

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

class ChessCorePropertyTest : FunSpec({

    val arbSquare = arbitrary {
        val squares = Square.allSquares.filter { it != Square.NONE }
        squares.random()
    }
    val arbSide = Arb.enum<Side>()
    val arbPiece = arbitrary {
        val pieces = Piece.allPieces.filter { it != Piece.NONE }
        pieces.random()
    }

    test("Square encode/decode round-trip") {
        checkAll(arbSquare) { sq ->
            val encoded = Square.encode(sq.rank, sq.file)
            encoded shouldBe sq
        }
    }

    test("Side flip is involution") {
        checkAll(arbSide) { side ->
            side.flip().flip() shouldBe side
        }
    }

    test("Piece.make round-trip") {
        checkAll(arbPiece) { piece ->
            val remade = Piece.make(piece.pieceSide, piece.pieceType)
            remade shouldBe piece
        }
    }

    test("Board doMove/undoMove preserves FEN") {
        checkAll(Arb.int(1..10)) { numMoves ->
            val board = Board()
            val originalFen = board.getFen()
            val madeMovesCount = makeRandomMoves(board, numMoves)

            repeat(madeMovesCount) { board.undoMove() }
            board.getFen() shouldBe originalFen
        }
    }

    test("Board state invariants hold after legal moves") {
        checkAll(Arb.int(1..20)) { numMoves ->
            val board = Board()
            makeRandomMoves(board, numMoves)

            val whiteKings = java.lang.Long.bitCount(board.getBitboard(Piece.WHITE_KING))
            val blackKings = java.lang.Long.bitCount(board.getBitboard(Piece.BLACK_KING))
            whiteKings shouldBe 1
            blackKings shouldBe 1

            val totalPieces = java.lang.Long.bitCount(board.getBitboard())
            (totalPieces <= 32) shouldBe true
        }
    }

    test("FEN round-trip") {
        checkAll(Arb.int(1..15)) { numMoves ->
            val board = Board()
            makeRandomMoves(board, numMoves)
            val fen = board.getFen()

            val board2 = Board()
            board2.loadFromFen(fen)
            board2.getFen() shouldBe fen
        }
    }

    test("SAN encode/decode round-trip") {
        checkAll(Arb.int(1..5)) { numMoves ->
            val board = Board()
            makeRandomMoves(board, numMoves)

            val legalMoves = board.legalMoves()
            if (legalMoves.isNotEmpty()) {
                val move = legalMoves.first()
                val boardCopy = Board()
                boardCopy.loadFromFen(board.getFen())

                val san = SanDecoder.encodeSan(boardCopy, move)
                san shouldNotBe ""

                boardCopy.undoMove()
                val decoded = SanDecoder.decodeSan(boardCopy, san, boardCopy.sideToMove)
                decoded.from shouldBe move.from
                decoded.to shouldBe move.to
                decoded.promotion shouldBe move.promotion
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
