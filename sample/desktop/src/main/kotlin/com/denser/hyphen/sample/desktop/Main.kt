package com.denser.hyphen.sample.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.denser.hyphen.sample.shared.HyphenToolbar
import com.denser.hyphen.state.rememberHyphenTextState
import com.denser.hyphen.ui.HyphenTextEditor

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Hyphen Editor - Desktop"
    ) {
        MaterialTheme {
            val editorState = rememberHyphenTextState(
                initialText = """
                        This is a paragraph demonstrating formatting:
                        **Bold**, *Italic*, __Underline__, ~~Strikethrough~~, ==Highlight==, and `Inline Code`.

                        > This is a blockquote demonstrating how nested text 
                        > can also be **bold** inside quotes.

                        - Bullet point 1
                        - Bullet point 2
                        
                        1. Ordered list 1
                        2. Ordered list 2
                    """.trimIndent()
            )
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    Box(
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        HyphenToolbar(state = editorState)
                    }
                }
            ) { innerPadding ->
                HyphenTextEditor(
                    state = editorState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                )
            }
        }
    }
}