package com.chunkymonkey.pgntogifconverter.data

data class SettingsData(
    val showPlayerName: Boolean,
    val showPlayerRating: Boolean,
    val showBoardCoordinates: Boolean,
    val moveDelay: Float,
    val shouldFlipBoard: Boolean,
    val lastMoveDelay: Float,
    val pieceSet: PieceSet,
    val boardStyle: BoardStyle,
    val highlightStyle: HighlightStyle = HighlightStyle.Green,
    val gifQuality: Int = 10,
    val gifLoopCount: Int = 0,
    val boardResolution: Int = 504,
    val showGameResult: Boolean = true,
    val startFromMoveIndex: Int = 0,
    val mp4AudioEnabled: Boolean = true,
    val mp4SoundMove: Boolean = true,
    val mp4SoundCapture: Boolean = true,
    val mp4SoundCheck: Boolean = true,
    val mp4SoundCastle: Boolean = true,
)
