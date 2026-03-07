package com.denser.hyphen.sample.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denser.hyphen.sample.shared.HyphenToolbar
import com.denser.hyphen.state.rememberHyphenTextState
import com.denser.hyphen.ui.HyphenBasicTextEditor

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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
                    modifier = Modifier.Companion.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("Hyphen Editor - Android") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    bottomBar = {
                        Box(
                            modifier = Modifier.Companion
                                .background(color = MaterialTheme.colorScheme.surfaceContainer)
                                .navigationBarsPadding()
                                .imePadding(),
                        ) {
                            HyphenToolbar(state = editorState)
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier.Companion
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        HyphenBasicTextEditor(
                            state = editorState,
                            modifier = Modifier.Companion.fillMaxSize(),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}