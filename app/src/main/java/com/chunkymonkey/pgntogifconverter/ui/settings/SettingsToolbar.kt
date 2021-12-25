package com.chunkymonkey.pgntogifconverter.ui.settings

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.chunkymonkey.pgntogifconverter.R

@Composable
fun SettingsToolbar(
    title: @Composable () -> Unit,
    onBack: (() -> Unit)? = null
) {
    TopAppBar(
        title = title,
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = { onBack.invoke() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                    )
                }
            }
        },
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = colorResource(R.color.white),
    )
}

@Preview
@Composable
fun SearchToolbarPreview() {
    MaterialTheme {
        SettingsToolbar(
            title = { Text(text = "Title") },
            onBack = {},
        )
    }
}