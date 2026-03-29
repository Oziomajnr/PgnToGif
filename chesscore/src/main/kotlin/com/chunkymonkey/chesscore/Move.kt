package com.chunkymonkey.chesscore

data class Move(
    val from: Square,
    val to: Square,
    val promotion: Piece = Piece.NONE
) {
    var san: String? = null

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(from.name.lowercase())
        sb.append(to.name.lowercase())
        if (promotion != Piece.NONE) {
            sb.append(Constants.getPieceNotation(promotion)?.lowercase() ?: "")
        }
        return sb.toString()
    }

    override fun hashCode(): Int = toString().hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Move) return false
        return from == other.from && to == other.to && promotion == other.promotion
    }
}
