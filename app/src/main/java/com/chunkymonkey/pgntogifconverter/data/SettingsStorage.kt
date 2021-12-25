package com.chunkymonkey.pgntogifconverter.data

interface SettingsStorage {
    fun saveSettings(settingsData: SettingsData)
    fun getSettings(): SettingsData
}