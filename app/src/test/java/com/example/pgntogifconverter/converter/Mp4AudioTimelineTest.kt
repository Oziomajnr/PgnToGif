package com.example.pgntogifconverter.converter

import com.chunkymonkey.pgntogifconverter.converter.Mp4AudioTimeline
import org.junit.Assert.assertEquals
import org.junit.Test

class Mp4AudioTimelineTest {

    @Test
    fun totalDurationNs_singleFrame_usesLastMoveMultiplier() {
        val frameIntervalNs = 1_000_000_000L / 30
        val framesPerMove = 5
        val framesPerLast = 7
        val total = Mp4AudioTimeline.totalDurationNs(1, framesPerMove, framesPerLast, frameIntervalNs)
        assertEquals(7L * frameIntervalNs, total)
    }

    @Test
    fun frameStartNs_secondFrame_offsetsByFirstSegment() {
        val frameIntervalNs = 1_000_000_000L / 30
        val framesPerMove = 5
        val framesPerLast = 7
        val start1 = Mp4AudioTimeline.frameStartNs(1, 3, framesPerMove, framesPerLast, frameIntervalNs)
        assertEquals(5L * frameIntervalNs, start1)
    }

    @Test
    fun frameStartNs_matches_cumulativeRepeats() {
        val frameIntervalNs = 1_000_000_000L / 30
        val framesPerMove = 5
        val framesPerLast = 7
        val total = Mp4AudioTimeline.totalDurationNs(3, framesPerMove, framesPerLast, frameIntervalNs)
        val s0 = Mp4AudioTimeline.frameStartNs(0, 3, framesPerMove, framesPerLast, frameIntervalNs)
        val s1 = Mp4AudioTimeline.frameStartNs(1, 3, framesPerMove, framesPerLast, frameIntervalNs)
        val s2 = Mp4AudioTimeline.frameStartNs(2, 3, framesPerMove, framesPerLast, frameIntervalNs)
        assertEquals(0L, s0)
        assertEquals(5L * frameIntervalNs, s1)
        assertEquals(10L * frameIntervalNs, s2)
        assertEquals(s2 + 7L * frameIntervalNs, total)
    }
}
