package com.chunkymonkey.pgntogifconverter.lichess

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Downloads Lichess SVG piece sets from `lichess.org` (same assets as the web app).
 */
class LichessPieceDownloader(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun pieceFamilyDir(familyId: String): File =
        File(context.filesDir, "lichess/pieces/$familyId").apply { mkdirs() }

    fun isPieceFamilyInstalled(familyId: String): Boolean {
        val dir = pieceFamilyDir(familyId)
        if (!dir.isDirectory) return false
        return PIECE_SVG_NAMES.all { File(dir, it).isFile }
    }

    suspend fun downloadPieceFamily(familyId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = pieceFamilyDir(familyId)
            for (name in PIECE_SVG_NAMES) {
                val url = "$BASE/piece/$familyId/$name"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        error("HTTP ${response.code} for $url")
                    }
                    val body = response.body ?: error("Empty body for $url")
                    body.byteStream().use { input ->
                        File(dir, name).outputStream().use { output -> input.copyTo(output) }
                    }
                }
            }
        }
    }

    companion object {
        private const val BASE = "https://lichess.org/assets"
        private const val USER_AGENT = "PgnToGifConverter/1.0 (Android; Lichess piece SVGs)"

        private val PIECE_SVG_NAMES = listOf(
            "wK.svg", "wQ.svg", "wR.svg", "wB.svg", "wN.svg", "wP.svg",
            "bK.svg", "bQ.svg", "bR.svg", "bB.svg", "bN.svg", "bP.svg",
        )
    }
}
