package com.chunkymonkey.pgntogifconverter.lichess

import android.content.Context
import com.chunkymonkey.pgntogifconverter.preference.PreferenceService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * Syncs Lichess piece and 2D board theme lists from upstream (GitHub + lila sources),
 * caches locally, and merges board colors from [lichess_board_colors.json] with hash fallbacks.
 */
class LichessCatalogRepository(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = PreferenceService(appContext)
    private val cacheFile = File(appContext.filesDir, "lichess/catalog_cache.json")
    private val colorMap: Map<String, Pair<String, String>> by lazy { loadColorMapFromAssets() }

    @Volatile
    private var pieceFamiliesCache: List<LichessThemeCatalog.PieceFamily>? = null

    @Volatile
    private var boardThemesCache: List<LichessThemeCatalog.BoardTheme>? = null

    init {
        loadCacheIntoMemory()
    }

    fun pieceFamilies(): List<LichessThemeCatalog.PieceFamily> =
        pieceFamiliesCache ?: FALLBACK_PIECES

    fun boardThemes(): List<LichessThemeCatalog.BoardTheme> =
        boardThemesCache ?: FALLBACK_BOARDS

    fun getBoardTheme(themeId: String): LichessThemeCatalog.BoardTheme? =
        boardThemes().find { it.id == themeId }

    suspend fun syncIfStale(maxAgeMs: Long = DEFAULT_MAX_AGE_MS): Result<Unit> {
        val last = prefs.getLong(PREF_LAST_SYNC, 0L)
        if (last > 0 && System.currentTimeMillis() - last < maxAgeMs) {
            return Result.success(Unit)
        }
        return syncNow()
    }

    suspend fun syncNow(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val pieces = fetchPieceFamiliesFromGitHub()
            val boardIds = fetchBoardThemeIdsFromThemeScala()
            val boards = boardIds.map { id ->
                val colors = colorMap[id] ?: fallbackHexPair(id)
                LichessThemeCatalog.BoardTheme(
                    id = id,
                    displayName = boardDisplayName(id),
                    lightArgb = argb(colors.first),
                    darkArgb = argb(colors.second),
                )
            }
            val pieceList = pieces.map { (id, name) ->
                LichessThemeCatalog.PieceFamily(id, name)
            }.sortedBy { it.displayName.lowercase(Locale.US) }

            pieceFamiliesCache = pieceList
            boardThemesCache = boards
            writeCacheFile(pieceList, boards)
            prefs.saveData(PREF_LAST_SYNC, System.currentTimeMillis())
        }
    }

    private fun loadCacheIntoMemory() {
        runCatching {
            val json = readCacheJson()
            parseCatalogJson(json)?.let { (p, b) ->
                pieceFamiliesCache = p
                boardThemesCache = b
            }
        }
    }

    private fun readCacheJson(): String {
        if (cacheFile.isFile) {
            return cacheFile.readText()
        }
        return appContext.assets.open(ASSET_DEFAULT_CATALOG).bufferedReader().use { it.readText() }
    }

    private fun parseCatalogJson(json: String): Pair<
        List<LichessThemeCatalog.PieceFamily>,
        List<LichessThemeCatalog.BoardTheme>,
        >? {
        val root = JSONObject(json)
        val pArr = root.optJSONArray("pieceFamilies") ?: return null
        val bArr = root.optJSONArray("boardThemes") ?: return null
        val pieces = buildList {
            for (i in 0 until pArr.length()) {
                val o = pArr.getJSONObject(i)
                add(
                    LichessThemeCatalog.PieceFamily(
                        o.getString("id"),
                        o.optString("name", pieceDisplayName(o.getString("id"))),
                    )
                )
            }
        }
        val boards = buildList {
            for (i in 0 until bArr.length()) {
                val o = bArr.getJSONObject(i)
                val id = o.getString("id")
                add(
                    LichessThemeCatalog.BoardTheme(
                        id = id,
                        displayName = o.optString("name", boardDisplayName(id)),
                        lightArgb = argb(o.getString("light")),
                        darkArgb = argb(o.getString("dark")),
                    )
                )
            }
        }
        return pieces to boards
    }

    private fun writeCacheFile(
        pieces: List<LichessThemeCatalog.PieceFamily>,
        boards: List<LichessThemeCatalog.BoardTheme>,
    ) {
        cacheFile.parentFile?.mkdirs()
        val root = JSONObject()
        root.put("version", 1)
        root.put("syncedAt", System.currentTimeMillis())
        val pArr = JSONArray()
        pieces.forEach { pf ->
            pArr.put(JSONObject().apply {
                put("id", pf.id)
                put("name", pf.displayName)
            })
        }
        root.put("pieceFamilies", pArr)
        val bArr = JSONArray()
        boards.forEach { bt ->
            bArr.put(
                JSONObject().apply {
                    put("id", bt.id)
                    put("name", bt.displayName)
                    put("light", toHexRgb(bt.lightArgb))
                    put("dark", toHexRgb(bt.darkArgb))
                }
            )
        }
        root.put("boardThemes", bArr)
        cacheFile.writeText(root.toString())
    }

    private fun loadColorMapFromAssets(): Map<String, Pair<String, String>> {
        val text = appContext.assets.open(ASSET_BOARD_COLORS).bufferedReader().use { it.readText() }
        val o = JSONObject(text)
        val out = mutableMapOf<String, Pair<String, String>>()
        o.keys().forEach { key ->
            val c = o.getJSONObject(key)
            out[key] = c.getString("light") to c.getString("dark")
        }
        return out
    }

    private fun fetchPieceFamiliesFromGitHub(): List<Pair<String, String>> {
        val request = Request.Builder()
            .url(GITHUB_API_PIECE)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", USER_AGENT)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("GitHub ${response.code}")
            val body = response.body?.string() ?: error("Empty body")
            val arr = JSONArray(body)
            val out = mutableListOf<Pair<String, String>>()
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                if (item.optString("type") != "dir") continue
                val name = item.getString("name")
                if (name.startsWith(".")) continue
                out.add(name to pieceDisplayName(name))
            }
            return out
        }
    }

    private fun fetchBoardThemeIdsFromThemeScala(): List<String> {
        val request = Request.Builder()
            .url(RAW_THEME_SCALA)
            .header("User-Agent", USER_AGENT)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Theme.scala ${response.code}")
            val full = response.body?.string() ?: error("Empty Theme.scala")
            return LichessThemeScalaParser.parseTwoDimensionalThemeIds(full)
        }
    }

    companion object {
        private const val GITHUB_API_PIECE =
            "https://api.github.com/repos/lichess-org/lila/contents/public/piece?ref=master"
        private const val RAW_THEME_SCALA =
            "https://raw.githubusercontent.com/lichess-org/lila/master/modules/pref/src/main/Theme.scala"
        private const val ASSET_BOARD_COLORS = "lichess_board_colors.json"
        private const val ASSET_DEFAULT_CATALOG = "lichess_catalog_default.json"
        private const val PREF_LAST_SYNC = "LICHESS_CATALOG_LAST_SYNC_MS"
        private const val DEFAULT_MAX_AGE_MS = 24L * 60 * 60 * 1000
        private const val USER_AGENT = "PgnToGifConverter/1.0 (Lichess catalog sync)"

        private val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .build()

        private val FALLBACK_PIECES = listOf(
            LichessThemeCatalog.PieceFamily("cburnett", "Cburnett"),
            LichessThemeCatalog.PieceFamily("merida", "Merida"),
        )

        private val FALLBACK_BOARDS = listOf(
            LichessThemeCatalog.BoardTheme(
                "brown", "Brown",
                argb("#f0d9b5"), argb("#b58863"),
            ),
            LichessThemeCatalog.BoardTheme(
                "blue-marble", "Blue marble",
                argb("#dee3e6"), argb("#8ca2ad"),
            ),
        )

        fun pieceDisplayName(id: String): String =
            id.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }

        fun boardDisplayName(id: String): String =
            id.split("-").joinToString(" ") { part ->
                part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
            }

        private fun fallbackHexPair(id: String): Pair<String, String> {
            val h = id.hashCode()
            val l = 0xE8 + (abs(h) and 0x0f)
            val d = 0x50 + ((abs(h shr 8)) and 0x1f)
            val light = String.format(Locale.US, "#%02x%02x%02x", l, l - 8, l - 16)
            val dark = String.format(Locale.US, "#%02x%02x%02x", d, d - 10, d - 20)
            return light to dark
        }

        private fun argb(hex: String): Int {
            val h = hex.removePrefix("#")
            val rgb = java.lang.Long.parseLong(h, 16).toInt() and 0xFFFFFF
            return (0xFF shl 24) or rgb
        }

        private fun toHexRgb(argb: Int): String {
            val rgb = argb and 0xFFFFFF
            return String.format(Locale.US, "#%06X", rgb)
        }
    }
}
