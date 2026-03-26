package com.chunkymonkey.pgntogifconverter.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer

class ChessSoundPlayer(private val context: Context) {

    private val app = context.applicationContext
    private var released = false
    private val useLichess = LichessSoundFiles.hasBundledSet(app)

    fun playMoveSound() = playType(MoveSoundType.Move)

    fun playCaptureSound() = playType(MoveSoundType.Capture)

    fun playCheckSound() = playType(MoveSoundType.Check)

    fun playCastleSound() = playType(MoveSoundType.Castle)

    fun playGameEndSound() {
        if (released) return
        if (useLichess) {
            playAsset(LichessSoundFiles.VICTORY)
        } else {
            playSamples(ChessSoundSynthesizer.tone(600.0, 150))
        }
    }

    fun release() {
        released = true
    }

    private fun playType(type: MoveSoundType) {
        if (released) return
        if (useLichess) {
            playAsset(LichessSoundFiles.assetPathFor(type))
        } else {
            playSamples(ChessSoundSynthesizer.synthesize(type))
        }
    }

    private fun playAsset(assetPath: String) {
        Thread {
            if (released) return@Thread
            try {
                val mp = MediaPlayer()
                val afd = app.assets.openFd(assetPath)
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                mp.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                mp.prepare()
                mp.setOnCompletionListener { it.release() }
                mp.start()
            } catch (_: Exception) {
            }
        }.start()
    }

    private fun playSamples(samples: ShortArray) {
        Thread {
            try {
                val sampleRate = ChessSoundSynthesizer.SAMPLE_RATE
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
                val durationMs = samples.size * 1000 / sampleRate
                Thread.sleep(durationMs.toLong() + 30)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
            }
        }.start()
    }
}
