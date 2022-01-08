package com.chunkymonkey.pgntogifconverter.analytics

sealed interface Event

object CreateGifButtonClicked: Event
object SettingsButtonClicked: Event
//object SettingsButtonClicked: Event