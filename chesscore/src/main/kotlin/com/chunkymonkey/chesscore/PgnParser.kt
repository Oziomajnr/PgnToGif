package com.chunkymonkey.chesscore

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader

object PgnParser {

    private const val UTF8_BOM = '\uFEFF'
    private val propertyPattern = Regex("""\[.* ".*"\]""")

    suspend fun parse(pgn: String): ParsedGame = withContext(Dispatchers.Default) {
        val lines = pgn.lines().iterator()
        parseFromLines(lines) ?: throw PgnParseException("No game found in PGN")
    }

    suspend fun parseFile(path: String): List<ParsedGame> = withContext(Dispatchers.IO) {
        val games = mutableListOf<ParsedGame>()
        BufferedReader(FileReader(path)).use { reader ->
            val lines = reader.lineSequence().iterator()
            while (true) {
                val game = parseFromLines(lines) ?: break
                games.add(game)
            }
        }
        games
    }

    fun parseFlow(reader: BufferedReader): Flow<ParsedGame> = flow {
        val lines = reader.lineSequence().iterator()
        while (true) {
            val game = parseFromLines(lines) ?: break
            emit(game)
        }
    }.flowOn(Dispatchers.IO)

    internal fun parseFromLines(lines: Iterator<String>): ParsedGame? {
        val headers = mutableMapOf<String, String>()
        val moveTextBuilder = StringBuilder()
        var foundHeaders = false
        var moveTextParsing = false

        while (lines.hasNext()) {
            var line = lines.next().trim()
            if (line.startsWith(UTF8_BOM)) line = line.substring(1)

            if (isProperty(line)) {
                val (name, value) = parseProperty(line) ?: continue
                headers[name] = value
                foundHeaders = true
                continue
            }

            if (line.isNotBlank()) {
                if (!foundHeaders) continue
                moveTextBuilder.append(line)
                moveTextBuilder.append('\n')
                moveTextParsing = true

                if (line.endsWith("1-0") || line.endsWith("0-1") ||
                    line.endsWith("1/2-1/2") || line.endsWith("*")
                ) {
                    break
                }
            } else if (moveTextParsing) {
                break
            }
        }

        if (!foundHeaders && moveTextBuilder.isEmpty()) return null

        val result = inferResult(headers, moveTextBuilder.toString())
        val moves = parseMoveText(moveTextBuilder.toString(), headers["FEN"])
        return ParsedGame(headers, moves, result)
    }

    private fun parseMoveText(moveText: String, fen: String?): List<Move> {
        var text = moveText
        text = text.replace("1-0", "").replace("0-1", "")
            .replace("1/2-1/2", "").replace("*", "")

        text = normalizeText(text)

        val board = Board()
        if (fen != null) board.loadFromFen(fen)

        val tokens = text.split(" ").filter { it.isNotBlank() }
        val moves = mutableListOf<Move>()

        for (token in tokens) {
            var t = token.trim()
            if (t.startsWith("$")) continue
            if (t.contains("...")) continue
            if (t.contains(".")) {
                t = t.substringAfterLast(".")
            }
            if (t.isBlank()) continue

            // Skip variation and comment markers
            if (t.startsWith("(") || t.startsWith(")") ||
                t.startsWith("{") || t.startsWith("}")
            ) continue

            try {
                val move = SanDecoder.decodeSan(board, t, board.sideToMove)
                if (move.from == Square.NONE && move.to == Square.NONE) continue
                move.san = t
                if (!board.doMove(move)) {
                    throw PgnParseException("Illegal move: $t on ${board.getFen()}")
                }
                moves.add(move)
            } catch (e: PgnParseException) {
                throw e
            } catch (e: Exception) {
                throw PgnParseException("Error parsing move '$t': ${e.message}", e)
            }
        }
        return moves
    }

    private fun inferResult(headers: Map<String, String>, moveText: String): GameResult {
        val resultHeader = headers["Result"]
        if (resultHeader != null) return GameResult.fromNotation(resultHeader)
        return when {
            moveText.trimEnd().endsWith("1-0") -> GameResult.WHITE_WON
            moveText.trimEnd().endsWith("0-1") -> GameResult.BLACK_WON
            moveText.trimEnd().endsWith("1/2-1/2") -> GameResult.DRAW
            else -> GameResult.ONGOING
        }
    }

    private fun isProperty(line: String): Boolean = propertyPattern.matches(line)

    private fun parseProperty(line: String): Pair<String, String>? {
        return try {
            val content = line.removePrefix("[").removeSuffix("]").trim()
            val firstQuote = content.indexOf('"')
            if (firstQuote < 0) return null
            val name = content.substring(0, firstQuote).trim()
            val value = content.substring(firstQuote + 1, content.lastIndexOf('"'))
            Pair(name, value)
        } catch (e: Exception) {
            null
        }
    }

    private fun normalizeText(text: String): String {
        var result = text
        // Remove comments in braces
        result = result.replace(Regex("\\{[^}]*}"), " ")
        // Remove variations in parens (non-recursive for simplicity)
        result = removeVariations(result)
        // Collapse whitespace
        result = result.replace(Regex("\\s+"), " ").trim()
        return result
    }

    private fun removeVariations(text: String): String {
        val sb = StringBuilder()
        var depth = 0
        for (c in text) {
            if (c == '(') { depth++; continue }
            if (c == ')') { depth = maxOf(0, depth - 1); continue }
            if (depth == 0) sb.append(c)
        }
        return sb.toString()
    }
}
