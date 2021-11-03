package com.chunkymonkey.chesslibrarywrapper.piece


sealed interface ChessPiece {

    /**
     * White pawn piece.
     */
    object WhitePawn : ChessPiece

    /**
     * White knight piece.
     */
    object WhiteKnight: ChessPiece

    /**
     * White bishop piece.
     */
    object WhiteBishop: ChessPiece

    /**
     * White rook piece.
     */
    object WhiteRook: ChessPiece

    /**
     * White queen piece.
     */
    object WhiteQueen: ChessPiece

    /**
     * White king piece.
     */
    object WhiteKing: ChessPiece

    /**
     * Black pawn piece.
     */
    object BlackPawn: ChessPiece

    /**
     * Black knight piece.
     */
    object black_knight: ChessPiece

    /**
     * Black bishop piece.
     */
    object BlackBishop: ChessPiece

    /**
     * Black rook piece.
     */
    object BlackRook: ChessPiece

    /**
     * Black queen piece.
     */
    object BlackQueen: ChessPiece

    /**
     * Black king piece.
     */
    object BlackKing: ChessPiece

    /**
     * None piece.
     */
    object None: ChessPiece
}