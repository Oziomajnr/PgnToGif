package com.chunkymonkey.pgntogifconverter.analytics

sealed class AnalyticsEvent(val title: String) {
    //Home screen events
    data class CreateGifClicked(val currentPgnText: String) :
        AnalyticsEvent("CreateGifClicked")

    object SettingsClicked : AnalyticsEvent("SettingsClicked")
    object ImportPgnClicked : AnalyticsEvent("ImportPgnClicked")
    data class ExportPgnClicked(val currentPgnText: String) : AnalyticsEvent("ExportPgnClicked")
    object GifImageClicked : AnalyticsEvent("GifImageClicked")
    object GifImageAnimationPaused : AnalyticsEvent("GifImageAnimationPaused")
    object GifImageAnimationStarted : AnalyticsEvent("GifImageAnimationStarted")
    object HandlingSystemIntent : AnalyticsEvent("HandlingSystemIntent")
    object HandlingSystemClipData : AnalyticsEvent("HandlingSystemClipData")
    object ProcessingPgnFile : AnalyticsEvent("ProcessingPgnFile")
    object LoadingPgnFileToView : AnalyticsEvent("LoadingPgnFileToView")

    //Settings Screen event
    object SettingsBoardStyleClicked : AnalyticsEvent("SettingsBoardStyleClicked")
    data class OnNewPieceSetSelected(val selectedPiece: String) :
        AnalyticsEvent("SettingsBoardStyleClicked")

    object SettingsShowPlayerNameClicked : AnalyticsEvent("SettingsShowPlayerNameClicked")
    object SettingsShowPlayerRatingClicked : AnalyticsEvent("SettingsShowPlayerRatingClicked")
    object SettingsFlipBoardClicked : AnalyticsEvent("SettingsFlipBoardClicked")
    object MoveDelaySliderClicked : AnalyticsEvent("MoveDelaySliderClicked")
    object LastMoveDelaySliderClicked : AnalyticsEvent("LastMoveDelaySliderClicked")
}


const val currentPgnTextParam = "Current_Pgn_text"
const val currentSettings = "Current_Settings"

const val showPlayerNameParam = "Show_Player_Name_Param"
const val showPlayerRatingParam = "Show_Player_Rating_Param"
const val showBoardCoordinatesParam = "Show_Board_Coordinates"
const val moveDelayParam = "Move_Delay"
const val lastMoveDelay = "Last_Move_Delay"