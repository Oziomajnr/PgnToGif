package com.chunkymonkey.pgntogifconverter.converter

import android.app.Application
import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import com.chunkymonkey.pgntogifconverter.audio.ChessSoundSynthesizer
import com.chunkymonkey.pgntogifconverter.data.SettingsData
import com.chunkymonkey.pgntogifconverter.dependency.DependencyFactory
import com.chunkymonkey.pgntogifconverter.resource.ChessPieceResourceProvider
import com.chunkymonkey.pgntogifconverter.resource.PaintResourceProvider
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.game.Game
import com.github.bhlangonijr.chesslib.move.Move
import java.io.File
import java.util.*
import kotlin.math.roundToInt

class PgnToMp4Converter(
    val context: Application,
    private val playerNameHelper: PlayerNameHelper
) {
    fun createMp4FileFromChessGame(
        game: Game,
        settingsData: SettingsData,
        startFromMove: Int = 0
    ): File {
        val board = Board()
        val settingsStorage = DependencyFactory.getSettingsStorage()
        val paintResourceProvider = PaintResourceProvider(context, settingsStorage)
        val chessPieceResourceProvider = ChessPieceResourceProvider(context, settingsStorage)
        val converter = ChessBoardToBitmapConverter(
            paintResourceProvider, chessPieceResourceProvider, settingsData.boardResolution
        )

        val shouldAddName = playerNameHelper.shouldShowPlayerName(game, settingsData)
        val blackPlayerName = playerNameHelper.getBlackPlayerName(game, settingsData)
        val whitePlayerName = playerNameHelper.getWhitePlayerName(game, settingsData)
        val topName: String?
        val bottomName: String?
        if (shouldAddName) {
            if (settingsData.shouldFlipBoard) {
                topName = whitePlayerName; bottomName = blackPlayerName
            } else {
                topName = blackPlayerName; bottomName = whitePlayerName
            }
        } else {
            topName = null; bottomName = null
        }

        game.loadMoveText()
        val moves = game.halfMoves
        val effectiveStart = startFromMove.coerceIn(0, moves.size)

        val frames = mutableListOf<Bitmap>()
        val moveSans = mutableListOf<String>()

        for (i in 0 until effectiveStart) {
            board.doMove(moves[i])
        }

        val noMove = Move(Square.NONE, Square.NONE)
        frames.add(
            converter.createBitmapFromChessBoard(
                board, noMove, settingsData.shouldFlipBoard,
                settingsData.showBoardCoordinates, topName, bottomName
            )
        )

        val gameResult = if (settingsData.showGameResult) extractGameResult(game) else null

        for (i in effectiveStart until moves.size) {
            board.doMove(moves[i])
            moveSans.add(moves[i].san ?: "")
            val isLastMove = i == moves.lastIndex
            frames.add(
                converter.createBitmapFromChessBoard(
                    board, moves[i], settingsData.shouldFlipBoard,
                    settingsData.showBoardCoordinates, topName, bottomName,
                    gameResult = if (isLastMove) gameResult else null
                )
            )
        }

        return encodeFramesToMp4(frames, settingsData, moveSans)
    }

    private fun encodeFramesToMp4(
        frames: List<Bitmap>,
        settingsData: SettingsData,
        moveSans: List<String>,
    ): File {
        val sampleBitmap = frames.first()
        val width = alignTo16(sampleBitmap.width)
        val height = alignTo16(sampleBitmap.height)

        val finalOut = File(
            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
            "ChessGame_${Date().time}.mp4"
        )

        val tempVideo = File(context.cacheDir, "ptg_vid_${System.nanoTime()}.mp4")
        try {
            encodeVideoOnly(frames, settingsData, width, height, tempVideo)

            if (!settingsData.mp4AudioEnabled) {
                tempVideo.copyTo(finalOut, overwrite = true)
                return finalOut
            }

            val moveDelayMs = (settingsData.moveDelay * 1000).roundToInt().toLong()
            val lastMoveDelayMs = (settingsData.lastMoveDelay * 1000).roundToInt().toLong()
            val framesPerMove = ((moveDelayMs * FPS) / 1000).toInt().coerceAtLeast(1)
            val framesPerLastMove = ((lastMoveDelayMs * FPS) / 1000).toInt().coerceAtLeast(1)
            val frameIntervalNs = 1_000_000_000L / FPS

            val totalNs = Mp4AudioTimeline.totalDurationNs(
                frames.size, framesPerMove, framesPerLastMove, frameIntervalNs
            )
            val sampleRate = ChessSoundSynthesizer.SAMPLE_RATE
            val totalSamples = ((totalNs * sampleRate) / 1_000_000_000L).toInt() + sampleRate / 10
            val events = mutableListOf<Pair<Int, ShortArray>>()
            for (f in 1 until frames.size) {
                val san = moveSans.getOrNull(f - 1) ?: continue
                val pcm = ChessSoundSynthesizer.pcmForSanIfEnabled(san, settingsData, context) ?: continue
                val startNs = Mp4AudioTimeline.frameStartNs(
                    f, frames.size, framesPerMove, framesPerLastMove, frameIntervalNs
                )
                val startSample = ((startNs * sampleRate) / 1_000_000_000L).toInt().coerceAtLeast(0)
                if (startSample < totalSamples) {
                    events.add(startSample to pcm)
                }
            }
            val pcm = ChessSoundSynthesizer.mixAtOffsets(totalSamples, events)

            val tempAudio = File(context.cacheDir, "ptg_aud_${System.nanoTime()}.mp4")
            try {
                AudioPcmToAacM4a.encode(pcm, sampleRate, tempAudio)
                Mp4VideoAudioMerger.merge(tempVideo, tempAudio, finalOut)
            } finally {
                tempAudio.delete()
            }
            return finalOut
        } finally {
            tempVideo.delete()
        }
    }

    private fun encodeVideoOnly(
        frames: List<Bitmap>,
        settingsData: SettingsData,
        width: Int,
        height: Int,
        outputFile: File,
    ) {
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        format.setInteger(MediaFormat.KEY_BIT_RATE, 4_000_000)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FPS)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)

        val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val codecInputSurface = CodecInputSurface(codec.createInputSurface())
        codec.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var trackIndex = -1
        var muxerStarted = false

        val moveDelayMs = (settingsData.moveDelay * 1000).roundToInt().toLong()
        val lastMoveDelayMs = (settingsData.lastMoveDelay * 1000).roundToInt().toLong()
        val framesPerMove = ((moveDelayMs * FPS) / 1000).toInt().coerceAtLeast(1)
        val framesPerLastMove = ((lastMoveDelayMs * FPS) / 1000).toInt().coerceAtLeast(1)

        var presentationTimeNs = 0L
        val frameIntervalNs = 1_000_000_000L / FPS

        try {
            for ((frameIdx, bitmap) in frames.withIndex()) {
                val repeatCount = if (frameIdx == frames.lastIndex) framesPerLastMove else framesPerMove

                for (rep in 0 until repeatCount) {
                    codecInputSurface.drawBitmap(bitmap, width, height)
                    codecInputSurface.setPresentationTime(presentationTimeNs)
                    codecInputSurface.swapBuffers()

                    val result = drainEncoderOutput(codec, muxer, trackIndex, muxerStarted, drain = false)
                    trackIndex = result.first
                    muxerStarted = result.second

                    presentationTimeNs += frameIntervalNs
                }
            }

            codec.signalEndOfInputStream()
            drainEncoderOutput(codec, muxer, trackIndex, muxerStarted, drain = true)

        } finally {
            try { codec.stop() } catch (_: Exception) {}
            try { codec.release() } catch (_: Exception) {}
            try { codecInputSurface.release() } catch (_: Exception) {}
            try {
                if (muxerStarted) muxer.stop()
                muxer.release()
            } catch (_: Exception) {}
        }
    }

    private fun drainEncoderOutput(
        codec: MediaCodec,
        muxer: MediaMuxer,
        trackIndex: Int,
        muxerStarted: Boolean,
        drain: Boolean
    ): Pair<Int, Boolean> {
        var track = trackIndex
        var started = muxerStarted
        val bufferInfo = MediaCodec.BufferInfo()
        val timeoutUs = if (drain) 10_000L else 0L

        while (true) {
            val outputIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)
            when {
                outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    track = muxer.addTrack(codec.outputFormat)
                    muxer.start()
                    started = true
                }
                outputIndex >= 0 -> {
                    val outputBuffer = codec.getOutputBuffer(outputIndex)
                    if (outputBuffer != null && started &&
                        bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0
                    ) {
                        muxer.writeSampleData(track, outputBuffer, bufferInfo)
                    }
                    codec.releaseOutputBuffer(outputIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        return Pair(track, started)
                    }
                }
                else -> break
            }
        }
        return Pair(track, started)
    }

    private fun extractGameResult(game: Game): String? {
        return try {
            val resultProp = game.property?.get("Result")
            when (resultProp) {
                "1-0" -> "1-0"
                "0-1" -> "0-1"
                "1/2-1/2" -> "½-½"
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val FPS = 30
        private fun alignTo16(value: Int): Int {
            val remainder = value % 16
            return if (remainder == 0) value else value + (16 - remainder)
        }
    }
}
