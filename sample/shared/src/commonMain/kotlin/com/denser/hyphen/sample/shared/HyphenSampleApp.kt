package com.denser.hyphen.sample.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.denser.hyphen.model.TriggerConfig
import com.denser.hyphen.state.rememberHyphenTextState
import com.denser.hyphen.sample.shared.data.HyphenDatabase
import com.denser.hyphen.sample.shared.data.HyphenDraft
import com.denser.hyphen.sample.shared.data.getDatabaseBuilder
import com.denser.hyphen.sample.shared.data.getRoomDatabase
import com.denser.hyphen.sample.shared.data.initDatabase
import kotlinx.coroutines.launch


typealias VerticalScrollbarSlot = @Composable (scrollState: ScrollState, modifier: androidx.compose.ui.Modifier) -> Unit

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HyphenSampleApp(
    verticalScrollbar: VerticalScrollbarSlot? = null,
    context: Any? = null,
) {
    val isAndroid = context != null

    val triggerConfigs = remember {
        listOf(
            TriggerConfig(trigger = "@", scheme = "user"),
            TriggerConfig(trigger = "#", scheme = "tag"),
            TriggerConfig(trigger = "{", scheme = "var", endTrigger = "}", addSpaceOnCompletion = false),
            TriggerConfig(trigger = "{{", scheme = "var", endTrigger = "}}", addSpaceOnCompletion = false),
        )
    }

    val hyphenState = rememberHyphenTextState(initialText = "", triggerConfigs = triggerConfigs)
    val playgroundState = remember { PlaygroundState() }
    val snackbarHostState = remember { SnackbarHostState() }

    var isDarkTheme by remember { mutableStateOf(false) }
    var database by remember { mutableStateOf<HyphenDatabase?>(null) }
    val scope = rememberCoroutineScope()

    // ── Database setup & draft restore ────────────────────────────────────
    LaunchedEffect(Unit) {
        if (database == null) {
            try {
                context?.let { initDatabase(it) }
                val db = getRoomDatabase(getDatabaseBuilder())
                database = db
                val draft = db.hyphenDao().getDraft()
                if (draft != null) {
                    hyphenState.setMarkdown(draft.text)
                } else {
                    hyphenState.setMarkdown(DEMO_TEXT)
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("DB error: ${e.message}")
            }
        }
    }

    // ── Draft callbacks ───────────────────────────────────────────────────
    val onSave: () -> Unit = remember(database) {
        {
            scope.launch {
                val db = database
                if (db != null) {
                    db.hyphenDao().saveDraft(HyphenDraft(text = hyphenState.toMarkdown()))
                    snackbarHostState.showSnackbar("Draft saved")
                } else {
                    snackbarHostState.showSnackbar("Database not ready")
                }
            }
            Unit
        }
    }

    val onReload: () -> Unit = remember(database) {
        {
            scope.launch {
                val db = database
                if (db != null) {
                    val draft = db.hyphenDao().getDraft()
                    if (draft != null) {
                        hyphenState.setMarkdown(draft.text)
                        snackbarHostState.showSnackbar("Draft reloaded")
                    } else {
                        snackbarHostState.showSnackbar("No draft found")
                    }
                } else {
                    snackbarHostState.showSnackbar("Database not ready")
                }
            }
            Unit
        }
    }

    val onReset: () -> Unit = remember(database) {
        {
            scope.launch {
                database?.hyphenDao()?.clearDraft()
                hyphenState.setMarkdown(DEMO_TEXT)
                snackbarHostState.showSnackbar("Reset to demo text")
            }
            Unit
        }
    }

    // ── Theme + scaffold ──────────────────────────────────────────────────
    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme(),
    ) {
        PlaygroundScaffold(
            playgroundState = playgroundState,
            hyphenState = hyphenState,
            isDarkTheme = isDarkTheme,
            onToggleTheme = { isDarkTheme = !isDarkTheme },
            onSave = onSave,
            onReload = onReload,
            onReset = onReset,
            snackbarHostState = snackbarHostState,
            verticalScrollbar = verticalScrollbar,
            isAndroid = isAndroid,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo content
// ─────────────────────────────────────────────────────────────────────────────

private val DEMO_TEXT = """
    # Heading 1
    ## Heading 2
    ### Heading 3
    **H1-H6 supported**
    
    This is a paragraph with **Bold**, *Italic*, __Underline__, ~~Strikethrough~~, ==Highlight==, `Inline Code`, and [Links](https://github.com/densermeerkat/hyphen).

    > This is a blockquote. Nested text can also be **bold** inside quotes.

    - Bullet point 1
    - Bullet point 2

    1. Ordered list item 1
    2. Ordered list item 2
    
    - [ ] Checklist task 1
    - [x] Checklist task 2

    [@Alice](user:Alice) [#tag](tag:tag) [{variable}](var:variable)
""".trimIndent()