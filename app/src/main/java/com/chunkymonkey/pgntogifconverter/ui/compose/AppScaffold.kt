package com.chunkymonkey.pgntogifconverter.ui.compose

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chunkymonkey.pgntogifconverter.ui.settings.SettingsToolbar

@Composable
fun AppScaffold(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    title: @Composable (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit),
) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            if (title != null) {
                SettingsToolbar(
                    title = title,
                    onBack = onBack
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .scrollable(
                    state = rememberScrollState(),
                    orientation = Orientation.Vertical,
                )
                .padding(0.dp),
            content = content
        )
    }
}