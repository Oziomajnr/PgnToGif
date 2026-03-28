package com.example.pgntogifconverter

import android.content.ClipData
import android.content.Intent
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.chunkymonkey.pgntogifconverter.ui.home.HomeActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class MoveNavigationInstrumentedTest {

    private lateinit var pgnString: String

    @Before
    fun loadPgn() {
        val ctx = InstrumentationRegistry.getInstrumentation().context
        // Short game: CI emulators OOM or time out generating a GIF for the full ICC benchmark PGN.
        ctx.assets.open("pgn/short_game_for_ui_tests.pgn").use {
            pgnString = it.bufferedReader().readText()
        }
    }

    @Test
    fun loadPgn_populatesMoveList() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), HomeActivity::class.java).apply {
            clipData = ClipData.newPlainText("application/vnd.chess-pgn", pgnString)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val activityRef = AtomicReference<HomeActivity?>(null)

        ActivityScenario.launch<HomeActivity>(intent).use { scenario ->
            scenario.onActivity { activityRef.set(it) }

            waitUntil(120_000) {
                var size = 0
                InstrumentationRegistry.getInstrumentation().runOnMainSync {
                    size = activityRef.get()?.moveList?.size ?: 0
                }
                size > 0
            }

            val activity = activityRef.get()!!
            assertTrue("Move list should have been populated", activity.moveList.isNotEmpty())
        }
    }

    @Test
    fun loadPgn_boardBitmapRenderedAtInitialPosition() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), HomeActivity::class.java).apply {
            clipData = ClipData.newPlainText("application/vnd.chess-pgn", pgnString)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val activityRef = AtomicReference<HomeActivity?>(null)

        ActivityScenario.launch<HomeActivity>(intent).use { scenario ->
            scenario.onActivity { activityRef.set(it) }

            waitUntil(120_000) {
                var hasBitmap = false
                InstrumentationRegistry.getInstrumentation().runOnMainSync {
                    hasBitmap = activityRef.get()?.boardBitmap != null
                }
                hasBitmap
            }

            val activity = activityRef.get()!!
            assertNotNull("Board bitmap should be rendered", activity.boardBitmap)
            assertEquals("Initial move index should be -1", -1, activity.currentMoveIndex)
        }
    }

    @Test
    fun moveListEntries_haveCorrectSanNotation() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), HomeActivity::class.java).apply {
            clipData = ClipData.newPlainText("application/vnd.chess-pgn", pgnString)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val activityRef = AtomicReference<HomeActivity?>(null)

        ActivityScenario.launch<HomeActivity>(intent).use { scenario ->
            scenario.onActivity { activityRef.set(it) }

            waitUntil(120_000) {
                var size = 0
                InstrumentationRegistry.getInstrumentation().runOnMainSync {
                    size = activityRef.get()?.moveList?.size ?: 0
                }
                size > 0
            }

            val activity = activityRef.get()!!
            val firstMove = activity.moveList.first()
            assertTrue("First move SAN should not be empty", firstMove.san.isNotBlank())
            assertEquals("First move should be white's", true, firstMove.isWhite)
            assertEquals("First move number should be 1", 1, firstMove.moveNumber)
        }
    }

    private fun waitUntil(timeoutMs: Long, condition: () -> Boolean) {
        val start = SystemClock.elapsedRealtime()
        while (SystemClock.elapsedRealtime() - start < timeoutMs) {
            if (condition()) return
            Thread.sleep(200)
        }
        assertTrue("Condition not met within ${timeoutMs}ms", condition())
    }
}
