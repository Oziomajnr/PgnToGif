package com.chunkymonkey.pgntogifconverter.converter

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import kotlin.math.min

/**
 * Encodes mono 16-bit PCM to a single-track MP4 (AAC) file.
 */
object AudioPcmToAacM4a {

    private const val TIMEOUT_US = 10_000L
    private const val SAMPLES_PER_FRAME = 1024

    fun encode(pcm: ShortArray, sampleRate: Int, outFile: File) {
        val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, 1)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 96_000)
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16 * 1024)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()

        val muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var audioTrack = -1
        var muxerStarted = false

        val bufferInfo = MediaCodec.BufferInfo()
        var inputDone = false
        var outputDone = false
        var sampleIndex = 0

        while (!outputDone) {
            if (!inputDone) {
                val inIndex = codec.dequeueInputBuffer(TIMEOUT_US)
                if (inIndex >= 0) {
                    val buf = codec.getInputBuffer(inIndex)!!
                    buf.clear()
                    if (sampleIndex >= pcm.size) {
                        codec.queueInputBuffer(inIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        inputDone = true
                    } else {
                        val samplesLeft = pcm.size - sampleIndex
                        val samplesThis = min(SAMPLES_PER_FRAME, samplesLeft)
                        val ptsUs = (sampleIndex * 1_000_000L) / sampleRate
                        for (i in 0 until samplesThis) {
                            buf.putShort(pcm[sampleIndex + i])
                        }
                        for (i in samplesThis until SAMPLES_PER_FRAME) {
                            buf.putShort(0)
                        }
                        val size = SAMPLES_PER_FRAME * 2
                        codec.queueInputBuffer(inIndex, 0, size, ptsUs, 0)
                        sampleIndex += samplesThis
                    }
                }
            }

            val outIndex = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
            when {
                outIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {}
                outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    audioTrack = muxer.addTrack(codec.outputFormat)
                    muxer.start()
                    muxerStarted = true
                }
                outIndex >= 0 -> {
                    val encoded = codec.getOutputBuffer(outIndex)!!
                    if (muxerStarted && bufferInfo.size > 0) {
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) {
                            muxer.writeSampleData(audioTrack, encoded, bufferInfo)
                        }
                    }
                    codec.releaseOutputBuffer(outIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        outputDone = true
                    }
                }
            }
        }

        try {
            codec.stop()
        } catch (_: Exception) {
        }
        codec.release()
        try {
            muxer.stop()
        } catch (_: Exception) {
        }
        muxer.release()
    }
}
