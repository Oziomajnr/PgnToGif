package com.chunkymonkey.pgntogifconverter.lichess

import android.content.Context
import org.json.JSONObject
import java.io.File

object LichessBoardThemeColors {

    fun readInstalled(context: Context, themeId: String): Pair<Int, Int>? {
        val f = File(context.filesDir, "lichess/boards/$themeId.json")
        if (!f.isFile) return null
        return runCatching {
            val o = JSONObject(f.readText())
            o.getInt("light") to o.getInt("dark")
        }.getOrNull()
    }

    fun isBoardThemeInstalled(context: Context, themeId: String): Boolean =
        File(context.filesDir, "lichess/boards/$themeId.json").isFile
}
