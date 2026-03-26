package com.chunkymonkey.pgntogifconverter.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chunkymonkey.pgntogifconverter.R
import com.chunkymonkey.pgntogifconverter.data.BoardStyle
import com.chunkymonkey.pgntogifconverter.data.PieceSet
import com.chunkymonkey.pgntogifconverter.lichess.LichessBoardThemeColors
import com.chunkymonkey.pgntogifconverter.lichess.LichessPieceDownloader
import com.chunkymonkey.pgntogifconverter.lichess.LichessThemeCatalog
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
    val context = LocalContext.current
    val pieceDownloader = remember { LichessPieceDownloader(context) }

    val currentPiece = settingsViewModel.settingsUIState.value.pieceSet
    val currentBoard = settingsViewModel.settingsUIState.value.boardStyle
    var selectedPieceSet by remember(currentPiece) { mutableStateOf(currentPiece) }
    var selectedBoardStyle by remember(currentBoard) { mutableStateOf(currentBoard) }

    var busyPieceId by remember { mutableStateOf<String?>(null) }
    var busyBoardId by remember { mutableStateOf<String?>(null) }
    var downloadError by remember { mutableStateOf<String?>(null) }

    var catalogSeq by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        LichessThemeCatalog.syncIfStale()
        catalogSeq++
    }
    val lichessPieceList = remember(catalogSeq) { LichessThemeCatalog.pieceFamilies() }
    val lichessBoardList = remember(catalogSeq) { LichessThemeCatalog.boardThemes() }

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
            stringResource(id = R.string.letter_piece_type)
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
        ), BoardStyleUiData(
            BoardStyle.Maple,
            stringResource(id = R.string.board_style_maple),
            R.color.light_square_color_maple,
            R.color.dark_square_color_maple,
            R.drawable.board_style_maple
        ), BoardStyleUiData(
            BoardStyle.Wood,
            stringResource(id = R.string.board_style_wood),
            R.color.light_square_color_wood,
            R.color.dark_square_color_wood,
            R.drawable.board_style_wood
        ), BoardStyleUiData(
            BoardStyle.Canvas,
            stringResource(id = R.string.board_style_canvas),
            R.color.light_square_color_canvas,
            R.color.dark_square_color_canvas,
            R.drawable.board_style_canvas
        ), BoardStyleUiData(
            BoardStyle.Metal,
            stringResource(id = R.string.board_style_metal),
            R.color.light_square_color_metal,
            R.color.dark_square_color_metal,
            R.drawable.board_style_metal
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
                                selectedBoardStyle = it.boardStyle
                            }, it.boardStyle == selectedBoardStyle
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    Text(
                        text = stringResource(R.string.lichess_section),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    lichessBoardList.forEach { theme ->
                        val installed =
                            LichessBoardThemeColors.isBoardThemeInstalled(context, theme.id)
                        val selected =
                            selectedBoardStyle == BoardStyle.Lichess(theme.id)
                        LichessBoardThemeRow(
                            theme = theme,
                            selected = selected,
                            installed = installed,
                            busy = busyBoardId == theme.id,
                            downloadsIdle = busyPieceId == null && busyBoardId == null,
                            onSelect = {
                                selectedBoardStyle = BoardStyle.Lichess(theme.id)
                            },
                            onDownload = {
                                downloadError = null
                                coroutineScope.launch {
                                    busyBoardId = theme.id
                                    val result =
                                        settingsViewModel.installLichessBoardTheme(theme.id)
                                    busyBoardId = null
                                    result.onFailure { e -> downloadError = e.message }
                                }
                            },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    items.forEach { pieceSet ->
                        ListItem(
                            text = pieceSet.title,
                            image = pieceSet.resourceId,
                            modifier = Modifier.clickable {
                                selectedPieceSet = pieceSet.pieceSet
                            },
                            pieceSet.pieceSet == selectedPieceSet
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    Text(
                        text = stringResource(R.string.lichess_section),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    lichessPieceList.forEach { family ->
                        val installed = pieceDownloader.isPieceFamilyInstalled(family.id)
                        val selected =
                            selectedPieceSet == PieceSet.Lichess(family.id)
                        LichessPieceFamilyRow(
                            family = family,
                            selected = selected,
                            installed = installed,
                            busy = busyPieceId == family.id,
                            downloadsIdle = busyPieceId == null && busyBoardId == null,
                            onSelect = {
                                if (installed) {
                                    selectedPieceSet = PieceSet.Lichess(family.id)
                                }
                            },
                            onDownload = {
                                downloadError = null
                                coroutineScope.launch {
                                    busyPieceId = family.id
                                    val result =
                                        settingsViewModel.downloadLichessPieceFamily(family.id)
                                    busyPieceId = null
                                    result.onFailure { e -> downloadError = e.message }
                                }
                            },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                downloadError?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = err,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption,
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        onClick = {
                            if (boardStyle.value) {
                                settingsViewModel.onNewBoardStyleSelected(selectedBoardStyle)
                            } else {
                                settingsViewModel.onNewPieceSetSelected(selectedPieceSet)
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
private fun LichessPieceFamilyRow(
    family: LichessThemeCatalog.PieceFamily,
    selected: Boolean,
    installed: Boolean,
    busy: Boolean,
    downloadsIdle: Boolean,
    onSelect: () -> Unit,
    onDownload: () -> Unit,
) {
    val nameBg = if (selected) Color.LightGray else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(nameBg)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = family.displayName,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = installed) { onSelect() },
        )
        if (!installed) {
            Button(
                onClick = onDownload,
                enabled = downloadsIdle,
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.lichess_download))
                }
            }
        } else {
            Text(
                text = stringResource(R.string.lichess_downloaded),
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun LichessBoardThemeRow(
    theme: LichessThemeCatalog.BoardTheme,
    selected: Boolean,
    installed: Boolean,
    busy: Boolean,
    downloadsIdle: Boolean,
    onSelect: () -> Unit,
    onDownload: () -> Unit,
) {
    val rowBg = if (selected) Color.LightGray else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onSelect() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(theme.lightArgb)),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(theme.darkArgb)),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = theme.displayName,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.weight(1f),
            )
        }
        if (!installed) {
            Button(
                onClick = onDownload,
                enabled = downloadsIdle,
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.lichess_download))
                }
            }
        } else {
            Text(
                text = stringResource(R.string.lichess_downloaded),
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.padding(start = 8.dp),
            )
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
