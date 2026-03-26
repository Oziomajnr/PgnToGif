package com.chunkymonkey.pgntogifconverter.ui.home

import android.graphics.Bitmap
import android.graphics.drawable.Animatable
import android.widget.ImageView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.chunkymonkey.pgntogifconverter.R
import com.chunkymonkey.pgntogifconverter.data.RecentGame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    pgnText: String,
    onPgnTextChange: (String) -> Unit,
    isLoading: Boolean,
    loadingStatus: String?,
    gifFile: File?,
    gifLoadKey: Int,
    boardBitmap: Bitmap?,
    moveList: List<MoveData>,
    currentMoveIndex: Int,
    isAutoPlaying: Boolean,
    autoPlayDelayMs: Long,
    clipboardPgn: String?,
    recentGames: List<RecentGame>,
    onMoveClick: (Int) -> Unit,
    onStepForward: () -> Unit,
    onStepBackward: () -> Unit,
    onToggleAutoPlay: () -> Unit,
    onLoadPgn: () -> Unit,
    onGenerateGifClick: () -> Unit,
    onImportPgnClick: () -> Unit,
    onExportClick: () -> Unit,
    onSaveClick: () -> Unit,
    onExportMp4Click: () -> Unit,
    onShareMp4Click: () -> Unit,
    onSettingsClick: () -> Unit,
    onGifImageViewCreated: (ImageView) -> Unit,
    onClipboardLoad: () -> Unit,
    onClipboardDismiss: () -> Unit,
    onRecentGameClick: (RecentGame) -> Unit,
    onClearRecentGames: () -> Unit,
    onStartFromMove: (Int) -> Unit,
    @Suppress("UNUSED_PARAMETER") soundEnabled: Boolean,
) {
    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    LaunchedEffect(isAutoPlaying, currentMoveIndex) {
        if (isAutoPlaying && moveList.isNotEmpty() && currentMoveIndex < moveList.size - 1) {
            delay(autoPlayDelayMs)
            onStepForward()
        } else if (isAutoPlaying && currentMoveIndex >= moveList.size - 1) {
            onToggleAutoPlay()
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContent = {
            PgnInputSheet(
                pgnText = pgnText,
                onPgnTextChange = onPgnTextChange,
                onLoad = {
                    scope.launch { sheetState.hide() }
                    onLoadPgn()
                },
                onImport = {
                    scope.launch { sheetState.hide() }
                    onImportPgnClick()
                },
                recentGames = recentGames,
                onRecentGameClick = { game ->
                    scope.launch { sheetState.hide() }
                    onRecentGameClick(game)
                },
                onClearRecentGames = onClearRecentGames
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.systemBarsPadding(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                painter = painterResource(R.drawable.ic_settings),
                                contentDescription = stringResource(R.string.settings)
                            )
                        }
                    }
                )
            }
        ) { padding ->
            BoxWithConstraints(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                val isLandscape = maxWidth > maxHeight

                if (isLandscape) {
                    LandscapeLayout(
                        gifFile = gifFile,
                        gifLoadKey = gifLoadKey,
                        boardBitmap = boardBitmap,
                        isLoading = isLoading,
                        loadingStatus = loadingStatus,
                        moveList = moveList,
                        currentMoveIndex = currentMoveIndex,
                        isAutoPlaying = isAutoPlaying,
                        clipboardPgn = clipboardPgn,
                        onMoveClick = onMoveClick,
                        onStepForward = onStepForward,
                        onStepBackward = onStepBackward,
                        onToggleAutoPlay = onToggleAutoPlay,
                        onGifImageViewCreated = onGifImageViewCreated,
                        onPastePgnClick = { scope.launch { sheetState.show() } },
                        onGenerateGifClick = onGenerateGifClick,
                        onExportClick = onExportClick,
                        onSaveClick = onSaveClick,
                        onExportMp4Click = onExportMp4Click,
                        onShareMp4Click = onShareMp4Click,
                        onClipboardLoad = onClipboardLoad,
                        onClipboardDismiss = onClipboardDismiss,
                        onStartFromMove = onStartFromMove,
                    )
                } else {
                    PortraitLayout(
                        gifFile = gifFile,
                        gifLoadKey = gifLoadKey,
                        boardBitmap = boardBitmap,
                        isLoading = isLoading,
                        loadingStatus = loadingStatus,
                        moveList = moveList,
                        currentMoveIndex = currentMoveIndex,
                        isAutoPlaying = isAutoPlaying,
                        clipboardPgn = clipboardPgn,
                        onMoveClick = onMoveClick,
                        onStepForward = onStepForward,
                        onStepBackward = onStepBackward,
                        onToggleAutoPlay = onToggleAutoPlay,
                        onGifImageViewCreated = onGifImageViewCreated,
                        onPastePgnClick = { scope.launch { sheetState.show() } },
                        onGenerateGifClick = onGenerateGifClick,
                        onExportClick = onExportClick,
                        onSaveClick = onSaveClick,
                        onExportMp4Click = onExportMp4Click,
                        onShareMp4Click = onShareMp4Click,
                        onClipboardLoad = onClipboardLoad,
                        onClipboardDismiss = onClipboardDismiss,
                        onStartFromMove = onStartFromMove,
                    )
                }
            }
        }
    }
}

@Composable
private fun PortraitLayout(
    gifFile: File?,
    gifLoadKey: Int,
    boardBitmap: Bitmap?,
    isLoading: Boolean,
    loadingStatus: String?,
    moveList: List<MoveData>,
    currentMoveIndex: Int,
    isAutoPlaying: Boolean,
    clipboardPgn: String?,
    onMoveClick: (Int) -> Unit,
    onStepForward: () -> Unit,
    onStepBackward: () -> Unit,
    onToggleAutoPlay: () -> Unit,
    onGifImageViewCreated: (ImageView) -> Unit,
    onPastePgnClick: () -> Unit,
    onGenerateGifClick: () -> Unit,
    onExportClick: () -> Unit,
    onSaveClick: () -> Unit,
    onExportMp4Click: () -> Unit,
    onShareMp4Click: () -> Unit,
    onClipboardLoad: () -> Unit,
    onClipboardDismiss: () -> Unit,
    onStartFromMove: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (clipboardPgn != null) {
            ClipboardBanner(onLoad = onClipboardLoad, onDismiss = onClipboardDismiss)
        }

        BoardDisplay(
            gifFile = gifFile,
            gifLoadKey = gifLoadKey,
            boardBitmap = boardBitmap,
            isLoading = isLoading,
            loadingStatus = loadingStatus,
            onGifImageViewCreated = onGifImageViewCreated,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        if (moveList.isNotEmpty()) {
            PlaybackControls(
                onStepBackward = onStepBackward,
                onStepForward = onStepForward,
                onToggleAutoPlay = onToggleAutoPlay,
                isAutoPlaying = isAutoPlaying,
                canGoBack = currentMoveIndex >= 0,
                canGoForward = currentMoveIndex < moveList.size - 1,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            MoveListView(
                moves = moveList,
                currentMoveIndex = currentMoveIndex,
                onMoveClick = onMoveClick,
                onStartFromMove = onStartFromMove,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        ActionButtons(
            hasMoves = moveList.isNotEmpty(),
            onPastePgnClick = onPastePgnClick,
            onGenerateGifClick = onGenerateGifClick,
            onExportClick = onExportClick,
            onSaveClick = onSaveClick,
            onExportMp4Click = onExportMp4Click,
            onShareMp4Click = onShareMp4Click,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun LandscapeLayout(
    gifFile: File?,
    gifLoadKey: Int,
    boardBitmap: Bitmap?,
    isLoading: Boolean,
    loadingStatus: String?,
    moveList: List<MoveData>,
    currentMoveIndex: Int,
    isAutoPlaying: Boolean,
    clipboardPgn: String?,
    onMoveClick: (Int) -> Unit,
    onStepForward: () -> Unit,
    onStepBackward: () -> Unit,
    onToggleAutoPlay: () -> Unit,
    onGifImageViewCreated: (ImageView) -> Unit,
    onPastePgnClick: () -> Unit,
    onGenerateGifClick: () -> Unit,
    onExportClick: () -> Unit,
    onSaveClick: () -> Unit,
    onExportMp4Click: () -> Unit,
    onShareMp4Click: () -> Unit,
    onClipboardLoad: () -> Unit,
    onClipboardDismiss: () -> Unit,
    onStartFromMove: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (clipboardPgn != null) {
            ClipboardBanner(onLoad = onClipboardLoad, onDismiss = onClipboardDismiss)
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            BoardDisplay(
                gifFile = gifFile,
                gifLoadKey = gifLoadKey,
                boardBitmap = boardBitmap,
                isLoading = isLoading,
                loadingStatus = loadingStatus,
                onGifImageViewCreated = onGifImageViewCreated,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .padding(4.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (moveList.isNotEmpty()) {
                    PlaybackControls(
                        onStepBackward = onStepBackward,
                        onStepForward = onStepForward,
                        onToggleAutoPlay = onToggleAutoPlay,
                        isAutoPlaying = isAutoPlaying,
                        canGoBack = currentMoveIndex >= 0,
                        canGoForward = currentMoveIndex < moveList.size - 1,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    MoveListView(
                        moves = moveList,
                        currentMoveIndex = currentMoveIndex,
                        onMoveClick = onMoveClick,
                        onStartFromMove = onStartFromMove,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                ActionButtons(
                    hasMoves = moveList.isNotEmpty(),
                    onPastePgnClick = onPastePgnClick,
                    onGenerateGifClick = onGenerateGifClick,
                    onExportClick = onExportClick,
                    onSaveClick = onSaveClick,
                    onExportMp4Click = onExportMp4Click,
                    onShareMp4Click = onShareMp4Click,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ClipboardBanner(
    onLoad: () -> Unit,
    onDismiss: () -> Unit,
) {
    Snackbar(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        action = {
            TextButton(onClick = onLoad) {
                Text(stringResource(R.string.clipboard_load), color = Color.White)
            }
        },
        backgroundColor = MaterialTheme.colors.primary,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.clipboard_pgn_detected),
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dismiss), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun BoardDisplay(
    gifFile: File?,
    gifLoadKey: Int,
    boardBitmap: Bitmap?,
    isLoading: Boolean,
    loadingStatus: String?,
    onGifImageViewCreated: (ImageView) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                colorResource(R.color.gif_preview_background),
                RoundedCornerShape(8.dp)
            )
            .border(1.dp, Color(0xFF979797), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            gifFile != null -> {
                var toggleCount by remember { mutableIntStateOf(0) }
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            setOnClickListener {
                                val d = drawable
                                if (d is Animatable) {
                                    if (d.isRunning) d.stop() else d.start()
                                    toggleCount++
                                }
                            }
                            onGifImageViewCreated(this)
                        }
                    },
                    update = { imageView ->
                        @Suppress("UNUSED_EXPRESSION")
                        gifLoadKey
                        Glide.with(imageView).load(gifFile).into(imageView)
                        onGifImageViewCreated(imageView)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            boardBitmap != null -> {
                Image(
                    bitmap = boardBitmap.asImageBitmap(),
                    contentDescription = "Chess board position",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Text(
                    text = "GIF Preview",
                    style = MaterialTheme.typography.h5,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = Color.White,
                    )
                    if (loadingStatus != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = loadingStatus,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaybackControls(
    onStepBackward: () -> Unit,
    onStepForward: () -> Unit,
    onToggleAutoPlay: () -> Unit,
    isAutoPlaying: Boolean,
    canGoBack: Boolean,
    canGoForward: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onStepBackward, enabled = canGoBack && !isAutoPlaying) {
            Icon(
                painter = painterResource(R.drawable.ic_skip_previous),
                contentDescription = "Previous move",
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(onClick = onToggleAutoPlay) {
            Icon(
                painter = if (isAutoPlaying) painterResource(R.drawable.ic_pause)
                else painterResource(R.drawable.ic_play),
                contentDescription = stringResource(R.string.auto_play),
                modifier = Modifier.size(32.dp),
                tint = if (isAutoPlaying) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(onClick = onStepForward, enabled = canGoForward && !isAutoPlaying) {
            Icon(
                painter = painterResource(R.drawable.ic_skip_next),
                contentDescription = "Next move",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun MoveListView(
    moves: List<MoveData>,
    currentMoveIndex: Int,
    onMoveClick: (Int) -> Unit,
    onStartFromMove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colors.surface,
                RoundedCornerShape(8.dp)
            )
            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(8.dp))
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            var lastMoveNumber = -1
            moves.forEach { moveData ->
                if (moveData.moveNumber != lastMoveNumber) {
                    lastMoveNumber = moveData.moveNumber
                    Text(
                        text = "${moveData.moveNumber}.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(end = 2.dp, top = 2.dp, bottom = 2.dp),
                        color = Color.Gray,
                    )
                }

                val isSelected = moveData.index == currentMoveIndex
                val bgColor = if (isSelected)
                    MaterialTheme.colors.primary.copy(alpha = 0.2f)
                else
                    Color.Transparent
                val borderMod = if (isSelected)
                    Modifier.border(1.dp, MaterialTheme.colors.primary, RoundedCornerShape(4.dp))
                else Modifier

                var showContextMenu by remember { mutableStateOf(false) }

                Box {
                    Text(
                        text = moveData.san,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .then(borderMod)
                            .background(bgColor, RoundedCornerShape(4.dp))
                            .combinedClickable(
                                onClick = { onMoveClick(moveData.index) },
                                onLongClick = { showContextMenu = true }
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                    )

                    DropdownMenu(
                        expanded = showContextMenu,
                        onDismissRequest = { showContextMenu = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            showContextMenu = false
                            onStartFromMove(moveData.index)
                        }) {
                            Text(stringResource(R.string.start_from_here))
                        }
                    }
                }
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}

@Composable
private fun PgnInputSheet(
    pgnText: String,
    onPgnTextChange: (String) -> Unit,
    onLoad: () -> Unit,
    onImport: () -> Unit,
    recentGames: List<RecentGame>,
    onRecentGameClick: (RecentGame) -> Unit,
    onClearRecentGames: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.paste_pgn_of_game),
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        OutlinedTextField(
            value = pgnText,
            onValueChange = onPgnTextChange,
            placeholder = { Text(stringResource(R.string.paste_or_type_pgn)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onImport,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_file_download),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.import_pgn),
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            Button(
                onClick = onLoad,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary
                ),
            ) {
                Text(stringResource(R.string.load))
            }
        }

        if (recentGames.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.recent_games),
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                )
                TextButton(onClick = onClearRecentGames) {
                    Text(stringResource(R.string.clear_recent), fontSize = 12.sp)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                recentGames.forEach { game ->
                    Text(
                        text = game.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRecentGameClick(game) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        maxLines = 1,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ActionButtons(
    hasMoves: Boolean,
    onPastePgnClick: () -> Unit,
    onGenerateGifClick: () -> Unit,
    onExportClick: () -> Unit,
    onSaveClick: () -> Unit,
    onExportMp4Click: () -> Unit,
    onShareMp4Click: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showExportMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = onPastePgnClick,
            modifier = Modifier.weight(1f),
        ) {
            Text("Paste PGN")
        }
        if (hasMoves) {
            Button(
                onClick = onGenerateGifClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.secondary
                ),
            ) {
                Text(stringResource(R.string.generate_gif))
            }

            Box {
                OutlinedButton(
                    onClick = { showExportMenu = true },
                    modifier = Modifier.width(100.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_share),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.export),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                DropdownMenu(
                    expanded = showExportMenu,
                    onDismissRequest = { showExportMenu = false }
                ) {
                    DropdownMenuItem(onClick = {
                        showExportMenu = false
                        onExportClick()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_share),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.share),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    DropdownMenuItem(onClick = {
                        showExportMenu = false
                        onSaveClick()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_file_download),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.save_to_device),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    DropdownMenuItem(onClick = {
                        showExportMenu = false
                        onExportMp4Click()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_file_download),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.export_as_mp4),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    DropdownMenuItem(onClick = {
                        showExportMenu = false
                        onShareMp4Click()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_share),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.share_mp4),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
