package com.chunkymonkey.pgntogifconverter.converter

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteBuffer

/**
 * Muxes one video-only MP4 and one audio-only MP4 into a single MP4.
 */
object Mp4VideoAudioMerger {

    fun merge(videoFile: File, audioFile: File, outFile: File) {
        val videoExtractor = MediaExtractor()
        videoExtractor.setDataSource(videoFile.absolutePath)
        val videoInTrack = findTrack(videoExtractor, "video/")
        check(videoInTrack >= 0) { "No video track in ${videoFile.name}" }

        val audioExtractor = MediaExtractor()
        audioExtractor.setDataSource(audioFile.absolutePath)
        val audioInTrack = findTrack(audioExtractor, "audio/")
        check(audioInTrack >= 0) { "No audio track in ${audioFile.name}" }

        val muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val videoFormat = videoExtractor.getTrackFormat(videoInTrack)
        val audioFormat = audioExtractor.getTrackFormat(audioInTrack)
        val outVideoTrack = muxer.addTrack(videoFormat)
        val outAudioTrack = muxer.addTrack(audioFormat)
        muxer.start()

        videoExtractor.selectTrack(videoInTrack)
        copyTrack(videoExtractor, muxer, outVideoTrack)

        audioExtractor.selectTrack(audioInTrack)
        copyTrack(audioExtractor, muxer, outAudioTrack)

        try {
            muxer.stop()
        } catch (_: Exception) {
        }
        muxer.release()
        videoExtractor.release()
        audioExtractor.release()
    }

    private fun findTrack(extractor: MediaExtractor, mimePrefix: String): Int {
        for (i in 0 until extractor.trackCount) {
            val mime = extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith(mimePrefix)) return i
        }
        return -1
    }

    private fun copyTrack(
        extractor: MediaExtractor,
        muxer: MediaMuxer,
        muxerTrackIndex: Int,
    ) {
        val buffer = ByteBuffer.allocate(1024 * 1024)
        val bufferInfo = MediaCodec.BufferInfo()
        while (true) {
            buffer.clear()
            val sampleSize = extractor.readSampleData(buffer, 0)
            if (sampleSize < 0) break
            bufferInfo.offset = 0
            bufferInfo.size = sampleSize
            bufferInfo.presentationTimeUs = extractor.sampleTime
            bufferInfo.flags = extractor.sampleFlags
            muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
            if (!extractor.advance()) break
        }
    }
}
