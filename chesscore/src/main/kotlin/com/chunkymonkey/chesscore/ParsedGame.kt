package com.chunkymonkey.chesscore

data class ParsedGame(
    val headers: Map<String, String>,
    val moves: List<Move>,
    val result: GameResult
) {
    val whitePlayer: String get() = headers["White"] ?: "?"
    val blackPlayer: String get() = headers["Black"] ?: "?"
    val whiteElo: Int get() = headers["WhiteElo"]?.toIntOrNull() ?: 0
    val blackElo: Int get() = headers["BlackElo"]?.toIntOrNull() ?: 0
    val event: String get() = headers["Event"] ?: "?"
    val site: String get() = headers["Site"] ?: "?"
    val date: String get() = headers["Date"] ?: "?"
    val fen: String? get() = headers["FEN"]
    val eco: String? get() = headers["ECO"]
    val opening: String? get() = headers["Opening"]
}
