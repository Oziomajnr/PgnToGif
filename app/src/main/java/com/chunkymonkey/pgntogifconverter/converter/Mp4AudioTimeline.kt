package com.chunkymonkey.pgntogifconverter.converter

/**
 * Video timeline helpers for MP4: frame repeat counts match [PgnToMp4Converter] encoding.
 */
object Mp4AudioTimeline {

    fun totalDurationNs(
        frameCount: Int,
        framesPerMove: Int,
        framesPerLastMove: Int,
        frameIntervalNs: Long,
    ): Long {
        var total = 0L
        for (frameIdx in 0 until frameCount) {
            val repeat = if (frameIdx == frameCount - 1) framesPerLastMove else framesPerMove
            total += repeat * frameIntervalNs
        }
        return total
    }

    /**
     * Start time (ns) of the first displayed sample of [frameIndex] (0-based), same as video PTS.
     */
    fun frameStartNs(
        frameIndex: Int,
        frameCount: Int,
        framesPerMove: Int,
        framesPerLastMove: Int,
        frameIntervalNs: Long,
    ): Long {
        var t = 0L
        for (frameIdx in 0 until frameIndex.coerceAtMost(frameCount)) {
            val repeat = if (frameIdx == frameCount - 1) framesPerLastMove else framesPerMove
            t += repeat * frameIntervalNs
        }
        return t
    }
}
