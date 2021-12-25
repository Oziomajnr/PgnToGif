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

    /**
     * Gets boolean value with key and returns default if key is absent
     */
    fun getBoolean(key: String, default: Boolean): Boolean {
        return sharedPreference.getBoolean(key, false)
    }

    fun getFloat(key: String, default: Float): Float {
        return sharedPreference.getFloat(key, default)
    }

    fun getString(key: String, default: String): String {
        return sharedPreference.getString(key, default)!!
    }
}