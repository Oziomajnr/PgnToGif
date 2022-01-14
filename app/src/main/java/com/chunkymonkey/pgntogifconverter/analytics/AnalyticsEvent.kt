package com.chunkymonkey.pgntogifconverter.analytics

sealed interface AnalyticsEvent {
    //Home screen events
    data class CreateGifClicked(val currentPgnText: String) : AnalyticsEvent
    object SettingsClicked : AnalyticsEvent
    object ImportPgnClicked : AnalyticsEvent
    data class ExportPgnClicked(val currentPgnText: String) : AnalyticsEvent
    object GifImageClicked : AnalyticsEvent
    object GifImageAnimationPaused : AnalyticsEvent
    object GifImageAnimationStarted : AnalyticsEvent
    object HandlingSystemIntent : AnalyticsEvent
    object HandlingSystemClipData : AnalyticsEvent
    object ProcessingPgnFile : AnalyticsEvent

    //Settings Screen event
    object SettingsBoardStyleClicked : AnalyticsEvent
    object SettingsShowPlayerNameClicked : AnalyticsEvent
    object SettingsShowPlayerRatingClicked : AnalyticsEvent
    object MoveDelaySliderClicked : AnalyticsEvent
}


const val currentPgnTextParam = "Current_Pgn_text"
const val currentSettings = "Current_Settings"

const val showPlayerNameParam = "Show_Player_Name_Param"
const val showPlayerRatingParam = "Show_Player_Rating_Param"
const val showBoardCoordinatesParam = "Show_Board_Coordinates"
const val moveDelayParam = "Move_Delay"