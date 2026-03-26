package com.chunkymonkey.pgntogifconverter.resource

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import com.chunkymonkey.pgntogifconverter.R
import com.chunkymonkey.pgntogifconverter.data.BoardStyle
import com.chunkymonkey.pgntogifconverter.data.SettingsStorage
import com.chunkymonkey.pgntogifconverter.lichess.LichessBoardThemeColors
import com.chunkymonkey.pgntogifconverter.lichess.LichessThemeCatalog

class PaintResourceProvider(
    private val context: Context, private val settingsStorage: SettingsStorage
) {
    private val boardStyleSnapshot: BoardStyle = settingsStorage.getSettings().boardStyle

    val darkSquarePaint: Paint = Paint().apply {
        color = resolveDarkSquareArgb(boardStyleSnapshot)
    }

    val lightSquarePaint: Paint = Paint().apply {
        color = resolveLightSquareArgb(boardStyleSnapshot)
    }

    val kingAttackedPaint: Paint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(context, R.color.kind_attached_colour)
        }
    }

    val highlightedSquarePaint: Paint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(context, R.color.highlighted_square_paint)
        }
    }

    val highlightFromPaint: Paint by lazy {
        Paint().apply {
            val hs = settingsStorage.getSettings().highlightStyle
            color = hs.fromColorAlpha(102) // ~40%
        }
    }

    val highlightToPaint: Paint by lazy {
        Paint().apply {
            val hs = settingsStorage.getSettings().highlightStyle
            color = hs.fromColorAlpha(153) // ~60%
        }
    }

    val boardBorderPaint: Paint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(context, R.color.board_border_color)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
    }

    /** Coordinate labels drawn on dark squares use the light square color for contrast. */
    val coordinateOnDarkPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = resolveLightSquareArgb(boardStyleSnapshot)
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textSize = 12f
    }

    /** Coordinate labels drawn on light squares use the dark square color for contrast. */
    val coordinateOnLightPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = resolveDarkSquareArgb(boardStyleSnapshot)
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textSize = 12f
    }

    val playerNameTextPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.player_name_text_color)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            textSize = 15f
        }
    }

    val boardFrameFillPaint: Paint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(context, R.color.board_border_color)
            style = Paint.Style.FILL
        }
    }

    private fun resolveLightSquareArgb(boardStyle: BoardStyle): Int {
        return when (boardStyle) {
            is BoardStyle.Lichess -> {
                LichessThemeCatalog.getBoardTheme(boardStyle.themeId)?.lightArgb
                    ?: LichessBoardThemeColors.readInstalled(context, boardStyle.themeId)?.first
                    ?: ContextCompat.getColor(context, R.color.light_square_color_default)
            }
            else -> resolveBuiltinColor(
                "light_square_color_${boardStyle.name}",
                R.color.light_square_color_default
            )
        }
    }

    private fun resolveDarkSquareArgb(boardStyle: BoardStyle): Int {
        return when (boardStyle) {
            is BoardStyle.Lichess -> {
                LichessThemeCatalog.getBoardTheme(boardStyle.themeId)?.darkArgb
                    ?: LichessBoardThemeColors.readInstalled(context, boardStyle.themeId)?.second
                    ?: ContextCompat.getColor(context, R.color.dark_square_color_default)
            }
            else -> resolveBuiltinColor(
                "dark_square_color_${boardStyle.name}",
                R.color.dark_square_color_default
            )
        }
    }

    private fun resolveBuiltinColor(resourceName: String, fallbackRes: Int): Int {
        val id = getColorResourceId(resourceName)
        return if (id != 0) ContextCompat.getColor(context, id)
        else ContextCompat.getColor(context, fallbackRes)
    }

    @SuppressLint("DiscouragedApi")
    private fun getColorResourceId(resourceName: String): Int {
        return context.resources.getIdentifier(
            resourceName, "color", context.packageName
        )
    }
}
