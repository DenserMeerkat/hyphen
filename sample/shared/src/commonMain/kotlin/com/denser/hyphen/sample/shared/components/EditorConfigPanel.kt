package com.denser.hyphen.sample.shared.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denser.hyphen.sample.shared.EditorMode
import com.denser.hyphen.sample.shared.TextFieldVariant
import com.denser.hyphen.sample.shared.PlaygroundState
import com.denser.hyphen.sample.shared.VerticalScrollbarSlot
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────────────────
// Editor Config Panel
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorConfigPanel(
    state: PlaygroundState,
    modifier: Modifier = Modifier,
    verticalScrollbar: VerticalScrollbarSlot? = null,
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier) {
        PanelHeader(dot = MaterialTheme.colorScheme.tertiary, label = "Editor Config")
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                // ── Editor mode ───────────────────────────────────────────
                ConfigSection("Editor Mode") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Component",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        val modes = listOf(
                            EditorMode.BasicEditor to "HyphenBasicEditor",
                            EditorMode.TextField to "HyphenTextField",
                        )
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            modes.forEachIndexed { i, (mode, label) ->
                                SegmentedButton(
                                    selected = state.editorMode == mode,
                                    onClick = { state.editorMode = mode },
                                    shape = SegmentedButtonDefaults.itemShape(i, modes.size),
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    },
                                )
                            }
                        }

                        if (state.editorMode == EditorMode.TextField) {
                            Text(
                                text = "TextField Style",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            val variants = listOf(
                                TextFieldVariant.Filled to "Filled",
                                TextFieldVariant.Outlined to "Outlined",
                            )
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                variants.forEachIndexed { i, (variant, label) ->
                                    SegmentedButton(
                                        selected = state.textFieldVariant == variant,
                                        onClick = { state.textFieldVariant = variant },
                                        shape = SegmentedButtonDefaults.itemShape(i, variants.size),
                                        label = {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Text options ──────────────────────────────────────────
                ConfigSection("Text Options") {
                    SliderRow(
                        label = "Font size",
                        value = state.fontSize,
                        min = 10f, max = 28f,
                        format = { "${it.roundToInt()}sp" },
                    ) { state.fontSize = it }

                    SliderRow(
                        label = "H. padding",
                        value = state.horizontalPadding,
                        min = 0f, max = 24f,
                        format = { "${it.roundToInt()}dp" },
                    ) { state.horizontalPadding = it }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Indent spaces",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        StepperButtons(
                            value = state.indentSpaces,
                            onDecrement = { if (state.indentSpaces > 1) state.indentSpaces-- },
                            onIncrement = { if (state.indentSpaces < 8) state.indentSpaces++ },
                        )
                    }
                }

                // ── Behaviour toggles ─────────────────────────────────────
                ConfigSection("Behaviour") {
                    ToggleRow("RTL layout", state.isRtl) { state.isRtl = it }
                    ToggleRow("Read-only", state.isReadOnly) { state.isReadOnly = it }
                    ToggleRow("Enabled", state.isEnabled) { state.isEnabled = it }
                    ToggleRow("Show suggestions popup", state.showSuggestionsPopup) {
                        state.showSuggestionsPopup = it
                    }
                }

                // ── Cursor color ──────────────────────────────────────────
                ConfigSection("Cursor") {
                    ToggleRow("Custom cursor color", state.useCursorColorOverride) {
                        state.useCursorColorOverride = it
                    }
                    AnimatedVisibility(
                        visible = state.useCursorColorOverride,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        HslColorPicker(
                            color = state.cursorColor,
                            onColorChange = { state.cursorColor = it },
                            label = "Cursor color",
                        )
                    }
                }

                // ── TextField options (conditional) ───────────────────────
                AnimatedVisibility(
                    visible = state.editorMode != EditorMode.BasicEditor,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    TextFieldOptionsSection(state)
                }

                // ── Reset ─────────────────────────────────────────────────
                TextButton(
                    onClick = { state.resetEditorConfig() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Reset editor config")
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
// TextField options section
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextFieldOptionsSection(state: PlaygroundState) {
    ConfigSection("TextField Options") {
        // Label
        ToggleRow("Show label", state.showLabel) { state.showLabel = it }
        AnimatedVisibility(visible = state.showLabel) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = state.labelText,
                    onValueChange = { state.labelText = it },
                    label = { Text("Label text") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                )
                ToggleRow("Floating label", state.useFloatingLabel) { state.useFloatingLabel = it }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // Placeholder
        ToggleRow("Show placeholder", state.showPlaceholder) { state.showPlaceholder = it }
        AnimatedVisibility(visible = state.showPlaceholder) {
            OutlinedTextField(
                value = state.placeholderText,
                onValueChange = { state.placeholderText = it },
                label = { Text("Placeholder text") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // Supporting text
        ToggleRow("Show supporting text", state.showSupportingText) {
            state.showSupportingText = it
        }
        AnimatedVisibility(visible = state.showSupportingText) {
            OutlinedTextField(
                value = state.supportingText,
                onValueChange = { state.supportingText = it },
                label = { Text("Supporting text") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // Error state
        ToggleRow("Error state", state.isError) { state.isError = it }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stepper widget (+/-)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepperButtons(
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TextButton(onClick = onDecrement, modifier = Modifier.width(40.dp)) { Text("−") }
        Text(
            text = "$value",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(20.dp),
        )
        TextButton(onClick = onIncrement, modifier = Modifier.width(40.dp)) { Text("+") }
    }
}
