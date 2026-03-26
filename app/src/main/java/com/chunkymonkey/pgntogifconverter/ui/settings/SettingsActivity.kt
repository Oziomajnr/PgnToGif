package com.chunkymonkey.pgntogifconverter.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.storage.base.rememberBooleanSettingState
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import com.chunkymonkey.pgntogifconverter.R
import com.chunkymonkey.pgntogifconverter.data.HighlightStyle
import com.chunkymonkey.pgntogifconverter.ui.compose.AppScaffold
import com.chunkymonkey.pgntogifconverter.ui.ui.theme.ImageToGifConverterTheme
import kotlinx.coroutines.launch
import java.math.RoundingMode
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsScreen(onBackPressed: () -> Unit = {}) {
    val settingsViewModel = remember { SettingsViewModel() }
    Parent(settingsViewModel, onBackPressed)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Parent(settingsViewModel: SettingsViewModel, onBackPressed: () -> Unit = {}) {
    ImageToGifConverterTheme {
        Surface(color = MaterialTheme.colors.background) {
            AppScaffold(
                onBack = { onBackPressed() },
                title = {
                    Text(
                        text = stringResource(id = R.string.settings)
                    )
                },
                content = {
                    val scope = rememberCoroutineScope()
                    val state = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
                    val isBoardStyle = remember { mutableStateOf(false) }

                    @Composable
                    fun MainContent() {
                        Column(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            ShowPlayerNameSettings(settingsViewModel)
                            ShowPlayerRatingSettings(settingsViewModel)
                            ShowBoardCoordinateSettings(settingsViewModel)
                            MoveDelaySetting(settingsViewModel)
                            LastMoveDelay(settingsViewModel)
                            FlipBoardSetting(settingsViewModel)
                            BoardStyleSettings(settingsViewModel) {
                                isBoardStyle.value = true
                                scope.launch { state.show() }
                            }
                            PieceSetSettings(settingsViewModel) {
                                isBoardStyle.value = false
                                scope.launch { state.show() }
                            }
                            HighlightColorSettings(settingsViewModel)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            GifQualitySetting(settingsViewModel)
                            GifLoopCountSetting(settingsViewModel)
                            BoardResolutionSetting(settingsViewModel)
                            ShowGameResultSetting(settingsViewModel)
                        }
                    }
                    SettingsBottomSheet(state, settingsViewModel, isBoardStyle) { MainContent() }
                }
            )
        }
    }
}


@Composable
fun ShowPlayerNameSettings(settingsViewModel: SettingsViewModel) {
    val state = rememberBooleanSettingState(settingsViewModel.settingsUIState.value.showPlayerName)
    SettingsSwitch(
        icon = {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = stringResource(R.string.show_player_names)
            )
        },
        title = {
            Text(
                modifier = Modifier.padding(start = Dp(0f)),
                text = stringResource(R.string.show_player_names)
            )
        },
        subtitle = {
            Text(
                modifier = Modifier.padding(start = Dp(0f)),
                text = stringResource(R.string.show_player_name_if_available)
            )
        },
        state = state,
        onCheckedChange = {
            settingsViewModel.onShowPlayerNameSettingsChange(it)
        }
    )
}

@Composable
fun ShowPlayerRatingSettings(settingsViewModel: SettingsViewModel) {
    val state =
        rememberBooleanSettingState(settingsViewModel.settingsUIState.value.showPlayerRating)

    SettingsSwitch(
        title = {
            Text(
                modifier = Modifier.padding(start = Dp(0f)),
                text = stringResource(R.string.show_player_rating)
            )
        },
        subtitle = {
            Text(
                modifier = Modifier.padding(start = Dp(0f)),
                text = stringResource(R.string.show_rating_if_available)
            )
        },
        state = state,
        onCheckedChange = {
            settingsViewModel.onShowPlayerRatingSettingsChange(it)
        }
    )
}

@Composable
fun ShowBoardCoordinateSettings(settingsViewModel: SettingsViewModel) {
    val state =
        rememberBooleanSettingState(settingsViewModel.settingsUIState.value.showBoardCoordinates)

    SettingsSwitch(
        title = {
            Text(
                modifier = Modifier.padding(start = Dp(0f)),
                text = stringResource(R.string.show_board_coordinates)
            )
        },
        state = state,
        onCheckedChange = {
            settingsViewModel.onShowBoardCoordinateSettingsChange(it)
        }
    )
}

@Composable
fun BoardStyleSettings(
    settingsViewModel: SettingsViewModel,
    onBoardSettingsVisibleChanges: (Boolean) -> (Unit)
) {

    SettingsMenuLink(
        title = {
            Text(text = stringResource(R.string.board_style))
        },
        subtitle = {
            Text(
                text = stringResource(R.string.select_colour_and_material_of_your_board)
            )
        },
        onClick = {
            settingsViewModel.settingsBoardStyleClicked()
            onBoardSettingsVisibleChanges(true)
        },
    )
}

@Composable
fun PieceSetSettings(
    settingsViewModel: SettingsViewModel,
    onPieceSettingsVisibleChanges: (Boolean) -> (Unit)
) {

    SettingsMenuLink(
        title = {
            Text(text = stringResource(R.string.piece_style))
        },
        subtitle = {
            Text(
                text = stringResource(R.string.select_style_of_your_piece)
            )
        },
        onClick = {
            settingsViewModel.settingsPieceSetClicked()
            onPieceSettingsVisibleChanges(true)
        },
        modifier = Modifier.padding(bottom = 20.dp)
    )
}

@Composable
fun FlipBoardSetting(settingsViewModel: SettingsViewModel) {
    val state =
        rememberBooleanSettingState(settingsViewModel.settingsUIState.value.flipBoard)

    SettingsSwitch(
        title = {
            Text(
                modifier = Modifier.padding(start = Dp(0f)),
                text = stringResource(R.string.flip_board_title)
            )
        },
        subtitle = {
            Text(
                modifier = Modifier.padding(start = Dp(0f)),
                text = stringResource(R.string.flip_board_description)
            )
        },
        state = state,
        onCheckedChange = {
            settingsViewModel.onFlipBoardSettingsChange(it)
        }
    )
}

@Composable
fun MoveDelaySetting(settingsViewModel: SettingsViewModel) {

    Column(modifier = Modifier.padding(start = 64.dp, top = 8.dp)) {
        Text(
            text = stringResource(R.string.move_delay_in_seconds),
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = stringResource(
                R.string.seconds, settingsViewModel.settingsUIState.value.moveDelay.toBigDecimal()
                    .setScale(1, RoundingMode.HALF_EVEN).toString()
            ),
            fontWeight = FontWeight.Bold
        )

        Slider(
            valueRange = 0.2F..3F,
            steps = 13,
            onValueChange = {
                settingsViewModel.onMoveDelaySettingsChange(it)
            },
            value = settingsViewModel.settingsUIState.value.moveDelay
        )
    }

}


@Composable
fun LastMoveDelay(settingsViewModel: SettingsViewModel) {

    Column(modifier = Modifier.padding(start = 64.dp, top = 8.dp)) {
        Text(
            text = stringResource(R.string.delay_after_last_move_in_seconds),
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = stringResource(
                R.string.seconds,
                settingsViewModel.settingsUIState.value.lastMoveDelay.toBigDecimal()
                    .setScale(1, RoundingMode.HALF_EVEN).toString()
            ),
            fontWeight = FontWeight.Bold
        )

        Slider(
            valueRange = 1F..10F,
            steps = 10,
            onValueChange = {
                settingsViewModel.onLastMoveDelaySettingsChanged(it)
            },
            value = settingsViewModel.settingsUIState.value.lastMoveDelay
        )
    }

}

@Composable
fun GifQualitySetting(settingsViewModel: SettingsViewModel) {
    Column(modifier = Modifier.padding(start = 64.dp, top = 8.dp)) {
        Text(
            text = stringResource(R.string.gif_quality),
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = stringResource(R.string.gif_quality_description),
            style = MaterialTheme.typography.caption,
            color = Color.Gray
        )
        Text(
            text = "${settingsViewModel.settingsUIState.value.gifQuality}",
            fontWeight = FontWeight.Bold
        )
        Slider(
            valueRange = 1f..20f,
            steps = 18,
            onValueChange = {
                settingsViewModel.onGifQualityChanged(it.roundToInt())
            },
            value = settingsViewModel.settingsUIState.value.gifQuality.toFloat()
        )
    }
}

@Composable
fun GifLoopCountSetting(settingsViewModel: SettingsViewModel) {
    val loopCount = settingsViewModel.settingsUIState.value.gifLoopCount
    Column(modifier = Modifier.padding(start = 64.dp, top = 8.dp)) {
        Text(
            text = stringResource(R.string.gif_loop_count),
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = if (loopCount == 0) stringResource(R.string.gif_loop_infinite)
            else stringResource(R.string.gif_loop_times, loopCount),
            fontWeight = FontWeight.Bold
        )
        Slider(
            valueRange = 0f..10f,
            steps = 9,
            onValueChange = {
                settingsViewModel.onGifLoopCountChanged(it.roundToInt())
            },
            value = loopCount.toFloat()
        )
    }
}

@Composable
fun BoardResolutionSetting(settingsViewModel: SettingsViewModel) {
    val resolution = settingsViewModel.settingsUIState.value.boardResolution
    Column(modifier = Modifier.padding(start = 64.dp, top = 8.dp)) {
        Text(
            text = stringResource(R.string.board_resolution),
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = stringResource(R.string.board_resolution_value, resolution),
            fontWeight = FontWeight.Bold
        )
        Slider(
            valueRange = 256f..1024f,
            steps = 6,
            onValueChange = {
                val rounded = (it.roundToInt() / 8) * 8
                settingsViewModel.onBoardResolutionChanged(rounded)
            },
            value = resolution.toFloat()
        )
    }
}

@Composable
fun ShowGameResultSetting(settingsViewModel: SettingsViewModel) {
    val state =
        rememberBooleanSettingState(settingsViewModel.settingsUIState.value.showGameResult)

    SettingsSwitch(
        title = {
            Text(
                modifier = Modifier.padding(start = Dp(0f)),
                text = stringResource(R.string.show_game_result)
            )
        },
        subtitle = {
            Text(
                modifier = Modifier.padding(start = Dp(0f)),
                text = stringResource(R.string.show_game_result_description)
            )
        },
        state = state,
        onCheckedChange = {
            settingsViewModel.onShowGameResultChanged(it)
        }
    )
}

@Composable
fun HighlightColorSettings(settingsViewModel: SettingsViewModel) {
    val currentStyle = settingsViewModel.settingsUIState.value.highlightStyle
    val showPickerDialog = remember { mutableStateOf(false) }

    val presets = listOf(
        HighlightStyle.Green to Color(0xFF9BC700),
        HighlightStyle.Yellow to Color(0xFFFFEB3B),
        HighlightStyle.Blue to Color(0xFF42A5F5),
        HighlightStyle.Red to Color(0xFFEF5350),
        HighlightStyle.Orange to Color(0xFFFF9800),
    )

    Column(modifier = Modifier.padding(start = 64.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)) {
        Text(
            text = stringResource(R.string.highlight_color),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presets.forEach { (style, color) ->
                val isSelected = currentStyle == style
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isSelected) Modifier.border(3.dp, MaterialTheme.colors.onSurface, CircleShape)
                            else Modifier.border(1.dp, Color.Gray, CircleShape)
                        )
                        .clickable {
                            settingsViewModel.onHighlightStyleSelected(style)
                        }
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (currentStyle is HighlightStyle.Custom)
                            Color(currentStyle.argb).copy(alpha = 1f)
                        else Color.White
                    )
                    .then(
                        if (currentStyle is HighlightStyle.Custom)
                            Modifier.border(3.dp, MaterialTheme.colors.onSurface, CircleShape)
                        else Modifier.border(1.dp, Color.Gray, CircleShape)
                    )
                    .clickable { showPickerDialog.value = true },
                contentAlignment = Alignment.Center
            ) {
                Text("?", fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        }
    }

    if (showPickerDialog.value) {
        ColorPickerDialog(
            initialColor = if (currentStyle is HighlightStyle.Custom) currentStyle.argb
            else android.graphics.Color.rgb(0x9B, 0xC7, 0x00),
            onColorSelected = { argb ->
                settingsViewModel.onHighlightStyleSelected(HighlightStyle.Custom(argb))
                showPickerDialog.value = false
            },
            onDismiss = { showPickerDialog.value = false }
        )
    }
}

@Composable
private fun ColorPickerDialog(
    initialColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialHsv = FloatArray(3)
    android.graphics.Color.colorToHSV(initialColor, initialHsv)

    val hue = remember { mutableFloatStateOf(initialHsv[0]) }
    val saturation = remember { mutableFloatStateOf(initialHsv[1]) }
    val brightness = remember { mutableFloatStateOf(initialHsv[2]) }

    val currentColor = android.graphics.Color.HSVToColor(
        floatArrayOf(hue.value, saturation.value, brightness.value)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pick_custom_color)) },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(currentColor), RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Hue", fontWeight = FontWeight.Bold)
                Slider(
                    value = hue.value,
                    onValueChange = { hue.value = it },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(currentColor),
                        activeTrackColor = Color(currentColor)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Saturation", fontWeight = FontWeight.Bold)
                Slider(
                    value = saturation.value,
                    onValueChange = { saturation.value = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(currentColor),
                        activeTrackColor = Color(currentColor)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Brightness", fontWeight = FontWeight.Bold)
                Slider(
                    value = brightness.value,
                    onValueChange = { brightness.value = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(currentColor),
                        activeTrackColor = Color(currentColor)
                    )
                )
            }
        },
        confirmButton = {
            Button(onClick = { onColorSelected(currentColor) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
