package com.example.pgntogifconverter.lichess

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chunkymonkey.pgntogifconverter.lichess.LichessBoardThemeColors
import com.chunkymonkey.pgntogifconverter.lichess.LichessBoardThemeInstaller
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LichessBoardInstallInstrumentedTest {

    @Test
    fun installBoardTheme_green_createsMetadataFile() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val installer = LichessBoardThemeInstaller(ctx)
        installer.installBoardTheme("green").getOrThrow()
        assertTrue(LichessBoardThemeColors.isBoardThemeInstalled(ctx, "green"))
    }
}
