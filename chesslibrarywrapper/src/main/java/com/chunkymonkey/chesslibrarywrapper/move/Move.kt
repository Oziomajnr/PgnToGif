package com.chunkymonkey.chesslibrarywrapper.move

import com.chunkymonkey.chesslibrarywrapper.board.square.Square

interface Move {
     val from: Square
     val to: Square
 }