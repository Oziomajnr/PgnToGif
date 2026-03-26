package com.chunkymonkey.pgntogifconverter.data

import com.chunkymonkey.pgntogifconverter.preference.PreferenceService
import org.json.JSONArray
import org.json.JSONObject

data class RecentGame(
    val pgn: String,
    val title: String,
    val timestamp: Long
)

class RecentGamesStorage(private val preferenceService: PreferenceService) {

    fun saveGame(pgn: String, title: String) {
        val games = getRecentGames().toMutableList()
        games.removeAll { it.pgn == pgn }
        games.add(0, RecentGame(pgn, title, System.currentTimeMillis()))
        if (games.size > MAX_RECENT_GAMES) {
            games.subList(MAX_RECENT_GAMES, games.size).clear()
        }
        val jsonArray = JSONArray()
        games.forEach { game ->
            val obj = JSONObject().apply {
                put("pgn", game.pgn)
                put("title", game.title)
                put("timestamp", game.timestamp)
            }
            jsonArray.put(obj)
        }
        preferenceService.saveData(RECENT_GAMES_KEY, jsonArray.toString())
    }

    fun getRecentGames(): List<RecentGame> {
        val json = preferenceService.getString(RECENT_GAMES_KEY, "[]")
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                RecentGame(
                    pgn = obj.getString("pgn"),
                    title = obj.optString("title", "Game"),
                    timestamp = obj.optLong("timestamp", 0L)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearRecentGames() {
        preferenceService.saveData(RECENT_GAMES_KEY, "[]")
    }

    companion object {
        private const val RECENT_GAMES_KEY = "RECENT_GAMES"
        private const val MAX_RECENT_GAMES = 20
    }
}
