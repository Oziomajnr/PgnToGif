package com.chunkymonkey.pgntogifconverter.preference

import android.content.Context

class PreferenceService(val context: Context) {
    private val sharedPreference =
        context.getSharedPreferences("COM.CHUNKYMONKEY.PGNTOGIFCONVERTER", Context.MODE_PRIVATE)

    fun saveData(key: String, value: Boolean) {
        sharedPreference.edit().putBoolean(key, value).apply()
    }

    fun saveData(key: String, value: Float) {
        sharedPreference.edit().putFloat(key, value).apply()
    }

    fun saveData(key: String, value: String) {
        sharedPreference.edit().putString(key, value).commit()
    }

    fun saveData(key: String, value: Int) {
        sharedPreference.edit().putInt(key, value).apply()
    }

    fun saveData(key: String, value: Long) {
        sharedPreference.edit().putLong(key, value).apply()
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        return sharedPreference.getBoolean(key, default)
    }

    fun getFloat(key: String, default: Float): Float {
        return sharedPreference.getFloat(key, default)
    }

    fun getString(key: String, default: String): String {
        return sharedPreference.getString(key, default)!!
    }

    fun getInt(key: String, default: Int): Int {
        return sharedPreference.getInt(key, default)
    }

    fun getLong(key: String, default: Long): Long {
        return sharedPreference.getLong(key, default)
    }
}
