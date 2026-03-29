package com.example.pgntogifconverter

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.chunkymonkey.pgntogifconverter.converter.ChessBoardToBitmapConverter
import com.chunkymonkey.pgntogifconverter.data.BoardStyle
import com.chunkymonkey.pgntogifconverter.data.PieceSet
import com.chunkymonkey.pgntogifconverter.data.PreferenceSettingsStorage
import com.chunkymonkey.pgntogifconverter.preference.PreferenceService
import com.chunkymonkey.pgntogifconverter.resource.ChessPieceResourceProvider
import com.chunkymonkey.pgntogifconverter.resource.PaintResourceProvider
import com.chunkymonkey.chesscore.Board
import com.chunkymonkey.chesscore.Move
import com.chunkymonkey.chesscore.Piece
import com.chunkymonkey.chesscore.Square
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies that changing board style or piece style in settings produces
 * different paint colors / piece bitmaps, ensuring the caching fix works.
 */
@RunWith(AndroidJUnit4::class)
class StyleChangeInstrumentedTest {

    private lateinit var preferenceService: PreferenceService
    private lateinit var settingsStorage: PreferenceSettingsStorage

    private val appContext get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        preferenceService = PreferenceService(appContext)
        settingsStorage = PreferenceSettingsStorage(preferenceService)
    }

    @Test
    fun boardStyle_changingFromDefaultToBlue_changesDarkSquarePaintColor() {
        saveWithBoardStyle(BoardStyle.Default)
        val defaultColor = freshPaintProvider().darkSquarePaint.color

        saveWithBoardStyle(BoardStyle.Blue)
        val blueColor = freshPaintProvider().darkSquarePaint.color

        assertNotEquals(
            "Dark square paint color should differ between Default and Blue",
            defaultColor, blueColor
        )
    }

    @Test
    fun boardStyle_changingFromDefaultToGreen_changesLightSquarePaintColor() {
        saveWithBoardStyle(BoardStyle.Default)
        val defaultColor = freshPaintProvider().lightSquarePaint.color

        saveWithBoardStyle(BoardStyle.Green)
        val greenColor = freshPaintProvider().lightSquarePaint.color

        assertNotEquals(
            "Light square paint color should differ between Default and Green",
            defaultColor, greenColor
        )
    }

    @Test
    fun boardStyle_sameBoardStyle_producesSamePaintColor() {
        saveWithBoardStyle(BoardStyle.Purple)
        val color1 = freshPaintProvider().darkSquarePaint.color

        saveWithBoardStyle(BoardStyle.Purple)
        val color2 = freshPaintProvider().darkSquarePaint.color

        assertEquals(
            "Same board style should produce the same dark square color",
            color1, color2
        )
    }

    @Test
    fun boardStyle_coordinateTextPaintsReflectStyle() {
        saveWithBoardStyle(BoardStyle.Default)
        val defaultProvider = freshPaintProvider()
        val defaultCoordOnDark = defaultProvider.coordinateOnDarkPaint.color
        val defaultCoordOnLight = defaultProvider.coordinateOnLightPaint.color

        saveWithBoardStyle(BoardStyle.Blue)
        val blueProvider = freshPaintProvider()
        val blueCoordOnDark = blueProvider.coordinateOnDarkPaint.color
        val blueCoordOnLight = blueProvider.coordinateOnLightPaint.color

        assertNotEquals(
            "Coordinate-on-dark paint should change with board style",
            defaultCoordOnDark, blueCoordOnDark
        )
        assertNotEquals(
            "Coordinate-on-light paint should change with board style",
            defaultCoordOnLight, blueCoordOnLight
        )
    }

    @Test
    fun pieceSet_changingFromDefaultToCalifornia_changesPieceBitmapCache() {
        saveWithPieceSet(PieceSet.Default)
        val squareSize = 504 / 8
        val defaultKing = freshPieceProvider().buildPieceBitmapCache(squareSize)[Piece.WHITE_KING]
        assertNotNull("Default king bitmap should exist", defaultKing)

        saveWithPieceSet(PieceSet.California)
        val californiaKing = freshPieceProvider().buildPieceBitmapCache(squareSize)[Piece.WHITE_KING]
        assertNotNull("California king bitmap should exist", californiaKing)

        val hasDifference = bitmapsHaveDifferentPixels(defaultKing!!, californiaKing!!)
        assertEquals(
            "White king bitmap should differ between Default and California piece sets",
            true, hasDifference
        )
    }

    @Test
    fun pieceSet_changingFromDefaultToSpatial_changesPieceBitmapCache() {
        saveWithPieceSet(PieceSet.Default)
        val squareSize = 504 / 8
        val defaultBishop = freshPieceProvider().buildPieceBitmapCache(squareSize)[Piece.BLACK_BISHOP]
        assertNotNull("Default bishop bitmap should exist", defaultBishop)

        saveWithPieceSet(PieceSet.Spatial)
        val spatialBishop = freshPieceProvider().buildPieceBitmapCache(squareSize)[Piece.BLACK_BISHOP]
        assertNotNull("Spatial bishop bitmap should exist", spatialBishop)

        val hasDifference = bitmapsHaveDifferentPixels(defaultBishop!!, spatialBishop!!)
        assertEquals(
            "Black bishop bitmap should differ between Default and Spatial",
            true, hasDifference
        )
    }

    @Test
    fun fullFrame_boardStyleChangeReflectedInGeneratedBitmap() {
        saveWithBoardStyle(BoardStyle.Default)
        val defaultFrame = generateFrame()

        saveWithBoardStyle(BoardStyle.Blue)
        val blueFrame = generateFrame()

        val halfSquare = 504 / 8 / 2
        val border = 3

        var hasDifference = false
        for (row in 0..7) {
            for (col in 0..7) {
                val px = border + col * (504 / 8) + halfSquare
                val py = border + row * (504 / 8) + halfSquare
                if (defaultFrame.getPixel(px, py) != blueFrame.getPixel(px, py)) {
                    hasDifference = true
                    break
                }
            }
            if (hasDifference) break
        }

        assertEquals(
            "Generated frames should differ when board style changes",
            true, hasDifference
        )

        defaultFrame.recycle()
        blueFrame.recycle()
    }

    private fun saveWithBoardStyle(style: BoardStyle) {
        settingsStorage.saveSettings(
            settingsStorage.getSettings().copy(boardStyle = style)
        )
    }

    private fun saveWithPieceSet(pieceSet: PieceSet) {
        settingsStorage.saveSettings(
            settingsStorage.getSettings().copy(pieceSet = pieceSet)
        )
    }

    private fun freshPaintProvider() = PaintResourceProvider(appContext, settingsStorage)

    private fun freshPieceProvider() = ChessPieceResourceProvider(appContext, settingsStorage)

    private fun generateFrame(): Bitmap {
        val converter = ChessBoardToBitmapConverter(freshPaintProvider(), freshPieceProvider())
        return converter.createBitmapFromChessBoard(
            Board(), Move(Square.NONE, Square.NONE),
            shouldFlipBoard = false, showBoardCoordinates = false
        )
    }

    /** Scans a grid of sample points across two same-size bitmaps for any pixel difference. */
    private fun bitmapsHaveDifferentPixels(a: Bitmap, b: Bitmap): Boolean {
        val step = maxOf(1, a.width / 10)
        for (x in 0 until a.width step step) {
            for (y in 0 until a.height step step) {
                if (a.getPixel(x, y) != b.getPixel(x, y)) return true
            }
        }
        return false
    }
}
