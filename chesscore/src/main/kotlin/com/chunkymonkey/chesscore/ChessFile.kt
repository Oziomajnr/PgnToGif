package com.chunkymonkey.chesscore

enum class ChessFile(val notation: String) {
    FILE_A("A"),
    FILE_B("B"),
    FILE_C("C"),
    FILE_D("D"),
    FILE_E("E"),
    FILE_F("F"),
    FILE_G("G"),
    FILE_H("H"),
    NONE("");

    companion object {
        val allFiles: Array<ChessFile> = values()
    }
}
