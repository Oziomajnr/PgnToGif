package com.chunkymonkey.pgntogifconverter.audio

import android.content.Context
import com.chunkymonkey.pgntogifconverter.data.SettingsData
import kotlin.math.sin

enum class MoveSoundType {
    Move,
    Capture,
    Check,
    Castle,
}

object ChessSoundSynthesizer {

    const val SAMPLE_RATE = 22050

    fun soundTypeFromSan(san: String): MoveSoundType {
        return when {
            san.contains("+") || san.contains("#") -> MoveSoundType.Check
            san.contains("x") -> MoveSoundType.Capture
            san.startsWith("O-O") -> MoveSoundType.Castle
            else -> MoveSoundType.Move
        }
    }

    /**
     * PCM for one move sound (same waveforms as [ChessSoundPlayer]).
     */
    fun synthesize(type: MoveSoundType): ShortArray {
        return when (type) {
            MoveSoundType.Move -> tone(500.0, 60)
            MoveSoundType.Capture -> tone(350.0, 100)
            MoveSoundType.Check -> tone(700.0, 120)
            MoveSoundType.Castle -> {
                val a = tone(400.0, 80)
                val b = tone(500.0, 80)
                a + b
            }
        }
    }

    fun tone(frequencyHz: Double, durationMs: Int): ShortArray {
        val numSamples = SAMPLE_RATE * durationMs / 1000
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val fade = 1.0 - i.toDouble() / numSamples
            samples[i] = (Short.MAX_VALUE * sin(2.0 * Math.PI * frequencyHz * t) * fade * 0.5).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    /**
     * Builds a buffer of [totalSamples] of silence, mixing in [synthesize] clips at [events] sample offsets.
     */
    fun mixAtOffsets(totalSamples: Int, events: List<Pair<Int, ShortArray>>): ShortArray {
        val out = ShortArray(totalSamples)
        for ((offset, clip) in events) {
            if (offset < 0 || offset >= totalSamples) continue
            for (i in clip.indices) {
                val j = offset + i
                if (j >= totalSamples) break
                val sum = out[j].toInt() + clip[i].toInt()
                out[j] = sum.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }
        return out
    }

    fun shouldIncludeSound(
        type: MoveSoundType,
        settings: SettingsData,
    ): Boolean {
        if (!settings.mp4AudioEnabled) return false
        return when (type) {
            MoveSoundType.Move -> settings.mp4SoundMove
            MoveSoundType.Capture -> settings.mp4SoundCapture
            MoveSoundType.Check -> settings.mp4SoundCheck
            MoveSoundType.Castle -> settings.mp4SoundCastle
        }
    }

    fun pcmForSanIfEnabled(san: String, settings: SettingsData): ShortArray? {
        val type = soundTypeFromSan(san)
        if (!shouldIncludeSound(type, settings)) return null
        return synthesize(type)
    }

    /**
     * PCM for MP4 export: prefers bundled Lichess (lila) MP3s, resampled to [SAMPLE_RATE] Hz.
     */
    fun pcmForSanIfEnabled(san: String, settings: SettingsData, context: Context): ShortArray? {
        val type = soundTypeFromSan(san)
        if (!shouldIncludeSound(type, settings)) return null
        if (LichessSoundFiles.hasBundledSet(context)) {
            try {
                val path = LichessSoundFiles.assetPathFor(type)
                val pcm = Mp3AssetDecoder.decodeToMono16(context, path)
                return resample44100To22050(pcm)
            } catch (_: Exception) {
                // fall through
            }
        }
        return synthesize(type)
    }

    /**
     * Downsample 44.1 kHz mono to 22.05 kHz (even indices).
     */
    fun resample44100To22050(input: ShortArray): ShortArray {
        if (input.isEmpty()) return input
        val n = input.size / 2
        if (n == 0) return input
        val out = ShortArray(n)
        for (i in 0 until n) out[i] = input[i * 2]
        return out
    }
}
