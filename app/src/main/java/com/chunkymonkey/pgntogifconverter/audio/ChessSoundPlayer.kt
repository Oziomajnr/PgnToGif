package com.chunkymonkey.pgntogifconverter.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.sin

class ChessSoundPlayer {

    private var released = false

    fun playMoveSound() {
        if (released) return
        playTone(500.0, 60)
    }

    fun playCaptureSound() {
        if (released) return
        playTone(350.0, 100)
    }

    fun playCheckSound() {
        if (released) return
        playTone(700.0, 120)
    }

    fun playCastleSound() {
        if (released) return
        playTone(400.0, 80)
        playTone(500.0, 80)
    }

    fun playGameEndSound() {
        if (released) return
        playTone(600.0, 150)
    }

    fun release() {
        released = true
    }

    private fun playTone(frequencyHz: Double, durationMs: Int) {
        Thread {
            try {
                val sampleRate = 22050
                val numSamples = sampleRate * durationMs / 1000
                val samples = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val fade = 1.0 - i.toDouble() / numSamples
                    samples[i] = (Short.MAX_VALUE * sin(2.0 * Math.PI * frequencyHz * t) * fade * 0.5).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                val bufferSize = samples.size * 2
                val audioTrack = AudioTrack(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                    bufferSize,
                    AudioTrack.MODE_STATIC,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
                )
                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()
                Thread.sleep(durationMs.toLong() + 30)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }.start()
    }
}
