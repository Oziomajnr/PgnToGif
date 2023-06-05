package com.chunkymonkey.pgntogifconverter.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chunkymonkey.pgntogifconverter.R
import com.chunkymonkey.pgntogifconverter.data.BoardStyle
import com.chunkymonkey.pgntogifconverter.data.PieceSet
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun SettingsBottomSheet(
    state: ModalBottomSheetState,
    settingsViewModel: SettingsViewModel,
    boardStyle: MutableState<Boolean>,
    sheetContent: @Composable () -> (Unit)
) {
    val coroutineScope = rememberCoroutineScope()
    val selectedPieceSet = remember {
        mutableStateOf(settingsViewModel.settingsUIState.value.pieceSet)
    }

    val selectedBoardStyle = remember {
        mutableStateOf(settingsViewModel.settingsUIState.value.boardStyle)
    }
    val items = setOf(
        PieceSetUiData(
            PieceSet.Default,
            R.drawable.default_piece_set_sample,
            stringResource(R.string.default_piece_type)
        ), PieceSetUiData(
            PieceSet.Pirouetti,
            R.drawable.pirouetti_piece_set_sample,
            stringResource(id = R.string.pirouetti_piece_type)
        ), PieceSetUiData(
            PieceSet.California,
            R.drawable.california_piece_set_sample,
            stringResource(id = R.string.california_piece_type)
        ), PieceSetUiData(
            PieceSet.Spatial,
            R.drawable.spatial_piece_set_sample,
            stringResource(id = R.string.spatial_piece_type)
        ), PieceSetUiData(
            PieceSet.Letter,
            R.drawable.letter_piece_set_sample,
            stringResource(id = R.string.spatial_piece_type)
        )
    )

    val boardStyleUiData = setOf(
        BoardStyleUiData(
            BoardStyle.Default,
            stringResource(id = R.string.board_style_default),
            R.color.light_square_color_default,
            R.color.dark_square_color_default,
            R.drawable.board_style_default
        ), BoardStyleUiData(
            BoardStyle.Blue,
            stringResource(id = R.string.board_style_blue),
            R.color.light_square_color_blue,
            R.color.dark_square_color_blue,
            R.drawable.board_style_blue
        ), BoardStyleUiData(
            BoardStyle.Green,
            stringResource(id = R.string.board_style_green),
            R.color.light_square_color_green,
            R.color.dark_square_color_green,
            R.drawable.board_style_green
        ), BoardStyleUiData(
            BoardStyle.IC,
            stringResource(id = R.string.board_style_ic),
            R.color.light_square_color_ic,
            R.color.dark_square_color_ic,
            R.drawable.board_style_ic
        ), BoardStyleUiData(
            BoardStyle.Purple,
            stringResource(id = R.string.board_style_purple),
            R.color.light_square_color_purple,
            R.color.dark_square_color_purple,
            R.drawable.board_style_purple
        )
    )


    ModalBottomSheetLayout(sheetShape = RoundedCornerShape(16.dp),
        sheetState = state,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (boardStyle.value) {
                        stringResource(R.string.select_board_style)
                    } else {
                        stringResource(
                            R.string.select_piece_type
                        )
                    },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .align(Alignment.CenterHorizontally),
                    fontWeight = FontWeight.Bold
                )
                if (boardStyle.value) {
                    boardStyleUiData.forEach {
                        ListItem(
                            text = it.title, image = it.drawable, modifier = Modifier.clickable {
                                selectedBoardStyle.value = it.boardStyle
                            }, it.boardStyle == selectedBoardStyle.value
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {

                    items.forEach { pieceSet ->
                        ListItem(
                            text = pieceSet.title,
                            image = pieceSet.resourceId,
                            modifier = Modifier.clickable {
                                selectedPieceSet.value = pieceSet.pieceSet
                            },
                            pieceSet.pieceSet == selectedPieceSet.value
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        onClick = {
                            if (boardStyle.value) {
                                settingsViewModel.onNewBoardStyleSelected(selectedBoardStyle.value)
                            } else {
                                settingsViewModel.onNewPieceSetSelected(selectedPieceSet.value)
                            }

                            coroutineScope.launch { state.hide() }
                        }, modifier = Modifier
                            .padding(vertical = 16.dp)
                            .align(Alignment.End)
                    ) {
                        Text(text = stringResource(R.string.save))
                    }
                }
            }
        }) {
        BoxWithConstraints {
            sheetContent()
        }
    }
}

@Composable
fun ListItem(
    text: String, image: Int, modifier: Modifier, selected: Boolean
) {
    val backgroundColor = if (selected) Color.LightGray else Color.Transparent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text, modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        )
        androidx.compose.foundation.Image(
            painter = painterResource(id = image),
            contentDescription = "",
            modifier = Modifier.size(40.dp)
        )
    }
}
