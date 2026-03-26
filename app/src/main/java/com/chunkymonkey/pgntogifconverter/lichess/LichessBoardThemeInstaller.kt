package com.chunkymonkey.pgntogifconverter.lichess

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LichessBoardThemeInstaller(private val context: Context) {

    suspend fun installBoardTheme(themeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val theme = LichessThemeCatalog.getBoardTheme(themeId)
                ?: error("Unknown board theme")
            val dir = File(context.filesDir, "lichess/boards").apply { mkdirs() }
            File(dir, "$themeId.json").writeText(
                """{"light":${theme.lightArgb},"dark":${theme.darkArgb}}"""
            )
        }
    }
}
