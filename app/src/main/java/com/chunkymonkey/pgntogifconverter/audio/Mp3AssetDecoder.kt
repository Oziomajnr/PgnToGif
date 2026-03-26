package com.chunkymonkey.pgntogifconverter.audio

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Decodes a short MP3 from assets to 16-bit PCM mono [ShortArray] at the decoder output sample rate.
 */
object Mp3AssetDecoder {

    fun decodeToMono16(context: Context, assetPath: String): ShortArray {
        val extractor = MediaExtractor()
        context.assets.openFd(assetPath).use { afd ->
            extractor.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        }
        val track = findAudioTrackIndex(extractor)
        check(track >= 0) { "No audio track in asset: $assetPath" }
        extractor.selectTrack(track)
        val format = extractor.getTrackFormat(track)
        val mime = format.getString(MediaFormat.KEY_MIME)
            ?: throw IllegalStateException("Missing MIME for $assetPath")

        val decoder = MediaCodec.createDecoderByType(mime)
        decoder.configure(format, null, null, 0)
        decoder.start()

        val pcmOut = ByteArrayOutputStream()
        val bufferInfo = MediaCodec.BufferInfo()
        var inputDone = false
        var outputDone = false
        var pcmOutFormat: MediaFormat? = null

        try {
            while (!outputDone) {
                if (!inputDone) {
                    val inIndex = decoder.dequeueInputBuffer(10_000)
                    if (inIndex >= 0) {
                        val inBuf = decoder.getInputBuffer(inIndex)!!
                        val sampleSize = extractor.readSampleData(inBuf, 0)
                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(
                                inIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            inputDone = true
                        } else {
                            val pts = extractor.sampleTime
                            decoder.queueInputBuffer(inIndex, 0, sampleSize, pts, 0)
                            extractor.advance()
                        }
                    }
                }

                when (val outIndex = decoder.dequeueOutputBuffer(bufferInfo, 10_000)) {
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {}
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        pcmOutFormat = decoder.outputFormat
                    }
                    else -> {
                        if (outIndex >= 0) {
                            val outBuf = decoder.getOutputBuffer(outIndex)!!
                            if (bufferInfo.size > 0) {
                                val dup = ByteArray(bufferInfo.size)
                                outBuf.position(bufferInfo.offset)
                                outBuf.limit(bufferInfo.offset + bufferInfo.size)
                                outBuf.get(dup)
                                pcmOut.write(dup)
                            }
                            decoder.releaseOutputBuffer(outIndex, false)
                            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                outputDone = true
                            }
                        }
                    }
                }
            }
        } finally {
            try {
                decoder.stop()
            } catch (_: Exception) {
            }
            decoder.release()
            extractor.release()
        }

        val bytes = pcmOut.toByteArray()
        if (bytes.isEmpty()) return ShortArray(0)

        val outFmt = pcmOutFormat
        val channels = outFmt?.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            ?: format.getInteger(MediaFormat.KEY_CHANNEL_COUNT).coerceAtLeast(1)

        val encoding = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            outFmt?.getInteger(MediaFormat.KEY_PCM_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
                ?: AudioFormat.ENCODING_PCM_16BIT
        } else {
            AudioFormat.ENCODING_PCM_16BIT
        }
        if (encoding != AudioFormat.ENCODING_PCM_16BIT) {
            throw UnsupportedOperationException("Expected PCM 16-bit, got encoding $encoding")
        }

        val shorts = bytes.toShortArrayLE()
        return if (channels >= 2) {
            monoFromStereo(shorts)
        } else {
            shorts
        }
    }

    private fun findAudioTrackIndex(extractor: MediaExtractor): Int {
        for (i in 0 until extractor.trackCount) {
            val mime = extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("audio/")) return i
        }
        return -1
    }

    private fun ByteArray.toShortArrayLE(): ShortArray {
        val bb = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN)
        val n = this.size / 2
        val out = ShortArray(n)
        for (i in 0 until n) out[i] = bb.short
        return out
    }

    private fun monoFromStereo(interleaved: ShortArray): ShortArray {
        val n = interleaved.size / 2
        val out = ShortArray(n)
        for (i in 0 until n) {
            val l = interleaved[i * 2].toInt()
            val r = interleaved[i * 2 + 1].toInt()
            out[i] = ((l + r) / 2).toShort()
        }
        return out
    }
}
