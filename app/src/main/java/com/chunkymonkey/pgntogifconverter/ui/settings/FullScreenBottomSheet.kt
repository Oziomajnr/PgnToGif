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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import com.chunkymonkey.pgntogifconverter.data.PieceSet
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun FullScreenBottomSheet(
    state: ModalBottomSheetState,
    settingsViewModel: SettingsViewModel,
    sheetContent: @Composable () -> (Unit)
) {
    val coroutineScope = rememberCoroutineScope()
    val selectedPieceSet = remember {
        mutableStateOf(settingsViewModel.settingsUIState.value.pieceSet)
    }
    val items = mapOf(
        PieceSet.Default to R.drawable.default_piece_set_sample,
        PieceSet.Pirouetti to R.drawable.pirouetti_piece_set_sample,
        PieceSet.California to R.drawable.california_piece_set_sample,
        PieceSet.Spatial to R.drawable.spatial_piece_set_sample,
        PieceSet.Letter to R.drawable.letter_piece_set_sample
    )
    ModalBottomSheetLayout(
        sheetShape = RoundedCornerShape(16.dp),
        sheetState = state,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_piece_type),
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .align(Alignment.CenterHorizontally),
                    fontWeight = FontWeight.Bold
                )
                items.forEach { pieceSet ->
                    ListItem(
                        text = pieceSet.key.name,
                        image = pieceSet.value,
                        modifier = Modifier.clickable {
                            selectedPieceSet.value = pieceSet.key
                        },
                        pieceSet.key == selectedPieceSet.value
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        onClick = {
                            settingsViewModel.onNewPieceSetSelected(selectedPieceSet.value)
                            coroutineScope.launch { state.hide() }
                        }, modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.End)
                    ) {
                        Text(text = "Save")
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
