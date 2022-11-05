package com.chunkymonkey.chesslibrarywrapper.board.side

sealed interface Side{
    object Black: Side
    object White: Side
}