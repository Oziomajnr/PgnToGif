package com.example.pgntogifconverter.benchmark

import com.chunkymonkey.chesscore.PgnParser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.io.InputStreamReader

/**
 * Automated benchmarks for PGN parsing/generation speed.
 * Uses the long Hikaru Nakamura vs Rybka-style game (200+ moves) as the benchmark asset.
 */
class PgnGenerationBenchmarkTest {

    private lateinit var longPgnString: String

    @Before
    fun loadBenchmarkPgn() {
        val stream = javaClass.classLoader?.getResourceAsStream("pgn/hikaru_vs_rybka_long.pgn")
            ?: throw IllegalStateException("Benchmark PGN resource not found: pgn/hikaru_vs_rybka_long.pgn")
        longPgnString = InputStreamReader(stream).readText()
        stream.close()
    }

    @Test
    fun benchmark_pgnParsing_singleRun_parsesSuccessfully() = runBlocking {
        val startMs = System.currentTimeMillis()
        val parsed = PgnParser.parse(longPgnString)
        val elapsedMs = System.currentTimeMillis() - startMs

        assertNotNull(parsed)
        val moves = parsed.moves
        assert(moves.size >= 200) { "Expected 200+ half-moves in long game, got ${moves.size}" }

        println("[BENCHMARK] Single parse: ${moves.size} half-moves in ${elapsedMs}ms (${moves.size / maxOf(1, elapsedMs)} moves/ms)")
    }

    @Test
    fun benchmark_pgnParsing_multipleRuns_reportsAverageTime() = runBlocking {
        val warmupRuns = 3
        val benchmarkRuns = 10

        repeat(warmupRuns) {
            PgnParser.parse(longPgnString)
        }

        val timesMs = mutableListOf<Long>()
        repeat(benchmarkRuns) {
            val startMs = System.currentTimeMillis()
            PgnParser.parse(longPgnString)
            val elapsedMs = System.currentTimeMillis() - startMs
            timesMs.add(elapsedMs)
        }

        val avgMs = timesMs.average()
        val minMs = timesMs.minOrNull() ?: 0L
        val maxMs = timesMs.maxOrNull() ?: 0L
        val halfMoveCount = PgnParser.parse(longPgnString).moves.size

        println("[BENCHMARK] PGN parsing (Hikaru vs Rybka long game):")
        println("  Runs: $benchmarkRuns | Half-moves: $halfMoveCount")
        println("  Average: ${"%.2f".format(avgMs)} ms")
        println("  Min: $minMs ms | Max: $maxMs ms")
        println("  Throughput: ${"%.1f".format(halfMoveCount / maxOf(0.001, avgMs / 1000.0))} moves/sec")

        assert(avgMs < 5_000) { "Parsing should complete in under 5s on average (got ${avgMs}ms)" }
    }

    @Test
    fun benchmark_pgnParsing_movesAvailableAfterParse_noExtraLoadStep() = runBlocking {
        val startMs = System.currentTimeMillis()
        val parsed = PgnParser.parse(longPgnString)
        val elapsedMs = System.currentTimeMillis() - startMs
        val moveCount = parsed.moves.size

        println("[BENCHMARK] PgnParser.parse() (moves included): $moveCount half-moves in ${elapsedMs}ms")

        assert(moveCount >= 200)
        assert(elapsedMs < 5_000) { "parse should complete in under 5s (got ${elapsedMs}ms)" }
    }
}
