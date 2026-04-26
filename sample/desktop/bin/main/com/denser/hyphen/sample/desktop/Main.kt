package com.denser.hyphen.sample.desktop

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.ui.window.rememberWindowState
import com.denser.hyphen.sample.shared.HyphenSampleApp

fun main() = application {
    val state = rememberWindowState(
        width = 1280.dp,
        height = 800.dp,
        position = WindowPosition.Aligned(Alignment.Center)
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "Hyphen Editor",
        state = state
    ) {
        HyphenSampleApp(
            verticalScrollbar = { scrollState, modifier ->
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(scrollState),
                    modifier = modifier,
                    style = LocalScrollbarStyle.current
                )
            }
        )
    }
}