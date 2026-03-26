package com.chunkymonkey.pgntogifconverter.lichess

import android.content.Context

/**
 * Facade for [LichessCatalogRepository]: synced piece families and board themes.
 * Call [init] from [android.app.Application.onCreate] before use.
 */
object LichessThemeCatalog {

    data class PieceFamily(val id: String, val displayName: String)

    data class BoardTheme(
        val id: String,
        val displayName: String,
        /** ARGB packed */
        val lightArgb: Int,
        val darkArgb: Int,
    )

    @Volatile
    private var repository: LichessCatalogRepository? = null

    fun init(context: Context) {
        synchronized(this) {
            if (repository == null) {
                repository = LichessCatalogRepository(context.applicationContext)
            }
        }
    }

    fun pieceFamilies(): List<PieceFamily> = repository?.pieceFamilies() ?: embeddedPieces()

    fun boardThemes(): List<BoardTheme> = repository?.boardThemes() ?: embeddedBoards()

    fun getBoardTheme(themeId: String): BoardTheme? =
        repository?.getBoardTheme(themeId) ?: embeddedBoards().find { it.id == themeId }

    suspend fun syncIfStale(maxAgeMs: Long = 24L * 60 * 60 * 1000): Result<Unit> =
        repository?.syncIfStale(maxAgeMs) ?: Result.failure(IllegalStateException("LichessThemeCatalog not initialized"))

    suspend fun syncNow(): Result<Unit> =
        repository?.syncNow() ?: Result.failure(IllegalStateException("LichessThemeCatalog not initialized"))

    /** JVM / pre-init fallback (matches bundled [lichess_catalog_default.json] sample). */
    private fun embeddedPieces(): List<PieceFamily> = listOf(
        PieceFamily("cburnett", "Cburnett"),
        PieceFamily("merida", "Merida"),
        PieceFamily("alpha", "Alpha"),
    )

    private fun embeddedBoards(): List<BoardTheme> = listOf(
        BoardTheme("brown", "Brown", argb("#f0d9b5"), argb("#b58863")),
        BoardTheme("green", "Green", argb("#edf3e3"), argb("#8ca666")),
        BoardTheme("blue-marble", "Blue marble", argb("#dee3e6"), argb("#8ca2ad")),
    )

    private fun argb(hex: String): Int {
        val h = hex.removePrefix("#")
        val rgb = java.lang.Long.parseLong(h, 16).toInt() and 0xFFFFFF
        return (0xFF shl 24) or rgb
    }
}
