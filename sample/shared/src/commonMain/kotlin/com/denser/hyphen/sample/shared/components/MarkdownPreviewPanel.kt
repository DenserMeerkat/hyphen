package com.denser.hyphen.sample.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denser.hyphen.sample.shared.VerticalScrollbarSlot
import hyphen.sample.shared.generated.resources.Res
import hyphen.sample.shared.generated.resources.content_copy_24dp
import hyphen.sample.shared.generated.resources.terminal_24dp
import org.jetbrains.compose.resources.painterResource

@Composable
fun MarkdownPreviewPanel(
    markdown: String,
    modifier: Modifier = Modifier,
    verticalScrollbar: VerticalScrollbarSlot? = null,
) {
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    val lines = markdown.lines()

    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        // ── Terminal Header Row ──────────────────────────────────────────
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        painterResource(Res.drawable.terminal_24dp),
                        contentDescription = "Terminal",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Markdown Preview",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "(${markdown.length} chars, ${lines.size} lines)",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(markdown))
                    },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        painterResource(Res.drawable.content_copy_24dp),
                        contentDescription = "Copy markdown",
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── Terminal Code View ───────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize()) {
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(10.dp),
                ) {
                    lines.forEachIndexed { index, line ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                        ) {
                            DisableSelection {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                    ),
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier
                                        .width(32.dp)
                                        .padding(end = 12.dp),
                                )
                            }
                            Text(
                                text = line.ifEmpty { " " },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            verticalScrollbar?.invoke(
                scrollState,
                Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp, horizontal = 2.dp),
            )
        }
    }
}
