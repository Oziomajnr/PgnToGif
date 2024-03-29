package com.chunkymonkey.pgntogifconverter.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.storage.base.rememberBooleanSettingState
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import com.chunkymonkey.pgntogifconverter.R
import com.chunkymonkey.pgntogifconverter.ui.compose.AppScaffold
import com.chunkymonkey.pgntogifconverter.ui.ui.theme.ImageToGifConverterTheme
import kotlinx.coroutines.launch
import java.math.RoundingMode

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Parent(SettingsViewModel()) {
                finish()
            }
        }
    }
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