package com.example.pgntogifconverter.benchmark

import android.content.ClipData
import android.content.Intent
import android.os.Environment
import android.os.SystemClock
import android.widget.ImageView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.chunkymonkey.pgntogifconverter.ui.home.HomeActivity
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import androidx.test.core.app.ActivityScenario

/**
 * Android instrumented benchmarks: load the long Hikaru vs Rybka game into the UI,
 * generate the GIF, and measure the full flow time (parse + UI update + GIF generation).
 *
 * Each test verifies:
 *  1. The GIF file was written to disk and is non-empty
 *  2. The GIF was loaded into the ImageView via Glide
 *
 * Requires a device/emulator where app storage (getExternalFilesDir) is writable.
 * Run: ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.pgntogifconverter.benchmark.PgnGenerationBenchmarkInstrumentedTest
 */
@RunWith(AndroidJUnit4::class)
class PgnGenerationBenchmarkInstrumentedTest {

    private lateinit var pgnString: String

    @Before
    fun loadBenchmarkPgnFromAssets() {
        val context = InstrumentationRegistry.getInstrumentation().context
        context.assets.open("pgn/hikaru_vs_rybka_long.pgn").use { input ->
            pgnString = input.bufferedReader().readText()
        }
        assertTrue("PGN should not be empty", pgnString.isNotBlank())
    }

    @Test
    fun benchmark_fullFlow_loadPgnViaIntent_andGenerateGif_measuresTime() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), HomeActivity::class.java).apply {
            clipData = ClipData.newPlainText("application/vnd.chess-pgn", pgnString)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val activityRef = AtomicReference<HomeActivity?>(null)
        val startMs = SystemClock.elapsedRealtime()

        ActivityScenario.launch<HomeActivity>(intent).use { scenario ->
            scenario.onActivity { activityRef.set(it) }

            val totalMs = waitForProgressBarCycle(activityRef, TIMEOUT_MS)
            assertTrue("Full flow should complete in under 90s", totalMs < 90_000)

            android.util.Log.i(TAG, "[BENCHMARK] Full flow (load PGN in UI + generate GIF): ${totalMs}ms for 200 half-moves")
            InstrumentationRegistry.getInstrumentation().sendStatus(0, android.os.Bundle().apply {
                putString("benchmark_full_flow_ms", totalMs.toString())
            })

            val activity = activityRef.get()!!
            verifyGifFileGenerated(activity, startMs)
            verifyGifLoadedIntoImageView(activity)
        }
    }

    @Test
    fun benchmark_fullFlow_pastePgnAndTapCreateGif_measuresTime() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val activityRef = AtomicReference<HomeActivity?>(null)

        ActivityScenario.launch<HomeActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                activityRef.set(activity)
                activity.pgnInputText = pgnString
            }

            val startMs: Long
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                activityRef.get()?.onCreateGifClicked()
            }
            startMs = SystemClock.elapsedRealtime()

            val totalMs = waitForProgressBarCycle(activityRef, TIMEOUT_MS)
            assertTrue("Flow should complete in under 90s", totalMs < 90_000)

            android.util.Log.i(TAG, "[BENCHMARK] Paste + Create GIF: ${totalMs}ms for 200 half-moves")

            val activity = activityRef.get()!!
            verifyGifFileGenerated(activity, startMs)
            verifyGifLoadedIntoImageView(activity)
        }
    }

    @Test
    fun benchmark_multipleRuns_fullFlow_reportsAverageTime() {
        val runs = 3
        val timesMs = mutableListOf<Long>()

        repeat(runs) { runIndex ->
            val intent = Intent(ApplicationProvider.getApplicationContext(), HomeActivity::class.java).apply {
                clipData = ClipData.newPlainText("application/vnd.chess-pgn", pgnString)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val activityRef = AtomicReference<HomeActivity?>(null)
            val startMs = SystemClock.elapsedRealtime()

            ActivityScenario.launch<HomeActivity>(intent).use { scenario ->
                scenario.onActivity { activityRef.set(it) }

                val totalMs = waitForProgressBarCycle(activityRef, TIMEOUT_MS)
                assertTrue("Run $runIndex should complete in under 90s", totalMs < 90_000)
                timesMs.add(totalMs)

                val activity = activityRef.get()!!
                verifyGifFileGenerated(activity, startMs)
                verifyGifLoadedIntoImageView(activity)
            }
        }

        val avgMs = timesMs.average()
        val minMs = timesMs.minOrNull() ?: 0L
        val maxMs = timesMs.maxOrNull() ?: 0L
        android.util.Log.i(TAG, "[BENCHMARK] Multiple runs (n=$runs): avg=${"%.0f".format(avgMs)}ms min=$minMs max=$maxMs")
        assertTrue("Average should be under 90s", avgMs < 90_000)
    }

    /**
     * Waits for the progress bar to become VISIBLE (generation started), then waits
     * for it to become GONE/INVISIBLE (generation finished). Returns elapsed ms.
     *
     * Uses the Compose-state-backed [HomeActivity.isProgressVisible] property
     * instead of View-based findViewById.
     */
    private fun waitForProgressBarCycle(
        activityRef: AtomicReference<HomeActivity?>,
        timeoutMs: Long
    ): Long {
        val cycleStart = SystemClock.elapsedRealtime()

        var sawVisible = false
        while (SystemClock.elapsedRealtime() - cycleStart < timeoutMs) {
            var visible = false
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                visible = activityRef.get()?.isProgressVisible == true
            }
            if (visible) {
                sawVisible = true
                break
            }
            Thread.sleep(POLL_INTERVAL_MS)
        }
        assertTrue("Progress bar should become visible (generation should start) within ${timeoutMs}ms", sawVisible)

        val generationStart = SystemClock.elapsedRealtime()
        var sawGone = false
        while (SystemClock.elapsedRealtime() - cycleStart < timeoutMs) {
            var visible = true
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                visible = activityRef.get()?.isProgressVisible == true
            }
            if (!visible) {
                sawGone = true
                break
            }
            Thread.sleep(POLL_INTERVAL_MS)
        }
        assertTrue("GIF generation should complete within ${timeoutMs}ms", sawGone)

        return SystemClock.elapsedRealtime() - generationStart
    }

    private fun verifyGifFileGenerated(activity: HomeActivity, testStartMs: Long) {
        val picturesDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        assertNotNull("Pictures directory should exist", picturesDir)
        val gifFiles = picturesDir!!.listFiles { file -> file.extension == "gif" }
        assertNotNull("Should be able to list files in pictures dir", gifFiles)
        assertTrue("At least one .gif file should exist after generation", gifFiles!!.isNotEmpty())

        val latestGif = gifFiles.maxByOrNull { it.lastModified() }!!
        assertTrue(
            "Most recent GIF should have been modified after test started " +
                    "(lastModified=${latestGif.lastModified()}, testStart=$testStartMs)",
            latestGif.lastModified() >= testStartMs - 5_000
        )
        assertTrue("Generated GIF should be non-empty (was ${latestGif.length()} bytes)", latestGif.length() > 0)
        android.util.Log.i(TAG, "[VERIFY] GIF file: ${latestGif.name}, size=${latestGif.length()} bytes")
    }

    private fun verifyGifLoadedIntoImageView(activity: HomeActivity) {
        val maxWaitMs = 10_000L
        val startMs = SystemClock.elapsedRealtime()
        var drawable: android.graphics.drawable.Drawable? = null

        while (SystemClock.elapsedRealtime() - startMs < maxWaitMs) {
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                drawable = activity.gifImageView?.drawable
            }
            if (drawable != null) break
            Thread.sleep(POLL_INTERVAL_MS)
        }

        assertNotNull("ImageView should have a drawable after GIF generation (Glide should have loaded it)", drawable)
        android.util.Log.i(TAG, "[VERIFY] ImageView drawable loaded: ${drawable!!::class.simpleName}")
    }

    companion object {
        private const val TAG = "PgnBenchmark"
        private const val TIMEOUT_MS = 120_000L
        private const val POLL_INTERVAL_MS = 200L
    }
}
