package com.denser.hyphen.sample.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.denser.hyphen.sample.shared.PlaygroundState
import com.denser.hyphen.sample.shared.VerticalScrollbarSlot
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────────────────
// Style Config Panel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StyleConfigPanel(
    state: PlaygroundState,
    modifier: Modifier = Modifier,
    verticalScrollbar: VerticalScrollbarSlot? = null,
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier) {
        PanelHeader(dot = MaterialTheme.colorScheme.secondary, label = "Style Config")
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                // ── Inline Styles ─────────────────────────────────────────
                ConfigSection("Inline Styles") {
                    ToggleRow("Bold color override", state.useBoldColorOverride) {
                        state.useBoldColorOverride = it
                    }
                    if (state.useBoldColorOverride) {
                        HslColorPicker(state.boldColor, { state.boldColor = it }, "Bold color")
                    }

                    HslColorPicker(
                        color = state.highlightColor,
                        onColorChange = { state.highlightColor = it },
                        label = "Highlight color",
                    )
                    SliderRow(
                        label = "Highlight alpha",
                        value = state.highlightAlpha,
                        min = 0f, max = 1f,
                        format = { "${(it * 100).roundToInt()}%" },
                        onValueChange = { state.highlightAlpha = it },
                    )

                    SliderRow(
                        label = "Code bg alpha",
                        value = state.inlineCodeBgAlpha,
                        min = 0f, max = 0.5f,
                        format = { "${(it * 100).roundToInt()}%" },
                        onValueChange = { state.inlineCodeBgAlpha = it },
                    )

                    HslColorPicker(
                        color = state.linkColor,
                        onColorChange = { state.linkColor = it },
                        label = "Link color",
                    )
                    ToggleRow("Link underline", state.linkUnderline) { state.linkUnderline = it }
                }

                // ── Headings ──────────────────────────────────────────────
                ConfigSection("Headings") {
                    ToggleRow("Bold headings", state.headingBold) { state.headingBold = it }
                    Spacer(Modifier.height(2.dp))
                    SliderRow("H1 size", state.h1Size, 14f, 36f, format = { "${it.roundToInt()}sp" }) {
                        state.h1Size = it
                    }
                    SliderRow("H2 size", state.h2Size, 14f, 32f, format = { "${it.roundToInt()}sp" }) {
                        state.h2Size = it
                    }
                    SliderRow("H3 size", state.h3Size, 14f, 28f, format = { "${it.roundToInt()}sp" }) {
                        state.h3Size = it
                    }
                    SliderRow("H4 size", state.h4Size, 14f, 24f, format = { "${it.roundToInt()}sp" }) {
                        state.h4Size = it
                    }
                    SliderRow("H5 size", state.h5Size, 14f, 22f, format = { "${it.roundToInt()}sp" }) {
                        state.h5Size = it
                    }
                    SliderRow("H6 size", state.h6Size, 14f, 20f, format = { "${it.roundToInt()}sp" }) {
                        state.h6Size = it
                    }
                }

                // ── Blockquote ────────────────────────────────────────────
                ConfigSection("Blockquote") {
                    ToggleRow("Text color override", state.useBlockquoteTextColorOverride) {
                        state.useBlockquoteTextColorOverride = it
                    }
                    if (state.useBlockquoteTextColorOverride) {
                        HslColorPicker(
                            color = state.blockquoteTextColor,
                            onColorChange = { state.blockquoteTextColor = it },
                            label = "Text color",
                        )
                    }
                    HslColorPicker(
                        color = state.blockquoteBgColor,
                        onColorChange = { state.blockquoteBgColor = it },
                        label = "Background color",
                    )
                    SliderRow(
                        label = "Bg alpha",
                        value = state.blockquoteBgAlpha, min = 0f, max = 1f,
                        format = { "${(it * 100).roundToInt()}%" },
                    ) { state.blockquoteBgAlpha = it }

                    HslColorPicker(
                        color = state.blockquoteBorderColor,
                        onColorChange = { state.blockquoteBorderColor = it },
                        label = "Border color",
                    )
                    SliderRow(
                        label = "Border alpha",
                        value = state.blockquoteBorderAlpha, min = 0f, max = 1f,
                        format = { "${(it * 100).roundToInt()}%" },
                    ) { state.blockquoteBorderAlpha = it }

                    SliderRow(
                        label = "Border width",
                        value = state.blockquoteBorderWidth, min = 1f, max = 12f,
                        format = { "${it.roundToInt()}dp" },
                    ) { state.blockquoteBorderWidth = it }

                    SliderRow(
                        label = "Corner radius",
                        value = state.blockquoteCornerRadius, min = 0f, max = 16f,
                        format = { "${it.roundToInt()}dp" },
                    ) { state.blockquoteCornerRadius = it }
                }

                // ── Mentions ──────────────────────────────────────────────
                ConfigSection("Mentions") {
                    HslColorPicker(
                        color = state.userMentionColor,
                        onColorChange = { state.userMentionColor = it },
                        label = "@user color",
                    )
                    HslColorPicker(
                        color = state.tagMentionColor,
                        onColorChange = { state.tagMentionColor = it },
                        label = "#tag color",
                    )
                    HslColorPicker(
                        color = state.varMentionColor,
                        onColorChange = { state.varMentionColor = it },
                        label = "{var} color",
                    )
                }

                // ── Checkboxes ────────────────────────────────────────────
                ConfigSection("Checkboxes") {
                    ToggleRow("Strike checked items", state.checkedStrikethrough) {
                        state.checkedStrikethrough = it
                    }
                    ToggleRow("Gray out checked items", state.checkedGrayOut) {
                        state.checkedGrayOut = it
                    }
                }

                // ── Reset ─────────────────────────────────────────────────
                TextButton(
                    onClick = { state.resetStyleConfig() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Reset styles to defaults")
                }

                Spacer(Modifier.height(8.dp))
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

// ─────────────────────────────────────────────────────────────────────────────
// Shared section / row primitives
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ConfigSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    InspectorGroup(title = title) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}

@Composable
fun ToggleRow(
    label: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = value,
            onCheckedChange = onValueChange,
            modifier = Modifier.scale(0.75f),
        )
    }
}

@Composable
fun SliderRow(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    format: (Float) -> String = { "${it.roundToInt()}" },
    onValueChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = format(value),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max,
            modifier = Modifier.fillMaxWidth().height(28.dp),
        )
    }
}
