package com.example.pgntogifconverter.benchmark

import com.github.bhlangonijr.chesslib.pgn.PgnHolder
import org.junit.Assert.assertEquals
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
    fun benchmark_pgnParsing_singleRun_parsesSuccessfully() {
        val holder = PgnHolder("benchmark")
        val startMs = System.currentTimeMillis()
        holder.loadPgn(longPgnString)
        val elapsedMs = System.currentTimeMillis() - startMs

        assertEquals(1, holder.games.size)
        val game = holder.games.first()
        assertNotNull(game)
        assertNotNull(game.halfMoves)
        assert(game.halfMoves.size >= 200) { "Expected 200+ half-moves in long game, got ${game.halfMoves.size}" }

        println("[BENCHMARK] Single parse: ${game.halfMoves.size} half-moves in ${elapsedMs}ms (${game.halfMoves.size / maxOf(1, elapsedMs)} moves/ms)")
    }

    @Test
    fun benchmark_pgnParsing_multipleRuns_reportsAverageTime() {
        val warmupRuns = 3
        val benchmarkRuns = 10

        repeat(warmupRuns) {
            val h = PgnHolder("warmup")
            h.loadPgn(longPgnString)
        }

        val timesMs = mutableListOf<Long>()
        repeat(benchmarkRuns) { i ->
            val holder = PgnHolder("run_$i")
            val startMs = System.currentTimeMillis()
            holder.loadPgn(longPgnString)
            val elapsedMs = System.currentTimeMillis() - startMs
            timesMs.add(elapsedMs)
            assertEquals(1, holder.games.size)
        }

        val avgMs = timesMs.average()
        val minMs = timesMs.minOrNull() ?: 0L
        val maxMs = timesMs.maxOrNull() ?: 0L
        val halfMoveCount = PgnHolder("count").apply { loadPgn(longPgnString) }.games.first().halfMoves.size

        println("[BENCHMARK] PGN parsing (Hikaru vs Rybka long game):")
        println("  Runs: $benchmarkRuns | Half-moves: $halfMoveCount")
        println("  Average: ${"%.2f".format(avgMs)} ms")
        println("  Min: $minMs ms | Max: $maxMs ms")
        println("  Throughput: ${"%.1f".format(halfMoveCount / maxOf(0.001, avgMs / 1000.0))} moves/sec")

        assert(avgMs < 5_000) { "Parsing should complete in under 5s on average (got ${avgMs}ms)" }
    }

    @Test
    fun benchmark_pgnParsing_loadMoveText_measuresFullParseAndLoad() {
        val holder = PgnHolder("loadMoveText")
        holder.loadPgn(longPgnString)
        val game = holder.games.first()

        val startMs = System.currentTimeMillis()
        game.loadMoveText()
        val elapsedMs = System.currentTimeMillis() - startMs
        val moveCount = game.halfMoves.size

        println("[BENCHMARK] loadMoveText(): $moveCount half-moves loaded in ${elapsedMs}ms")

        assert(moveCount >= 200)
        assert(elapsedMs < 5_000) { "loadMoveText should complete in under 5s (got ${elapsedMs}ms)" }
    }
}
