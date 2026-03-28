package com.denser.hyphen.inline.ui

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import com.denser.hyphen.core.model.MarkupStyle
import com.denser.hyphen.core.model.StyleRange
import com.denser.hyphen.inline.state.HyphenInlineState

@Composable
fun HyphenInlineEditor(
    fieldState: TextFieldState,
    inlineState: HyphenInlineState,
    modifier: Modifier = Modifier,
    inputTransformation: InputTransformation? = null,
    textStyle: TextStyle = TextStyle(
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface,
    ),
    styleConfig: InlineStyleConfig = InlineStyleConfig(),
    cursorBrush: Brush = SolidColor(MaterialTheme.colorScheme.onSurface),
    onTextLayout: (Density.(() -> TextLayoutResult?) -> Unit)? = null
) {
    LaunchedEffect(fieldState.selection) {
        inlineState.updateSelection(fieldState.selection)
    }

    val chainedTransformation = remember(inputTransformation, inlineState) {
        InputTransformation {
            inputTransformation?.apply { transformInput() }

            if (asCharSequence().isNotEmpty()) {
                inlineState.processInput(this)
            }
        }
    }

    BasicTextField(
        state = fieldState,
        textStyle = textStyle,
        cursorBrush = cursorBrush,
        inputTransformation = chainedTransformation,
        onTextLayout = onTextLayout,
        outputTransformation = {
            applyInlineStyles(inlineState.spans, styleConfig, this)
        },
        modifier = modifier
            .onFocusChanged { focusState ->
                inlineState.isFocused = focusState.isFocused
            }
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                val isPrimary = event.isCtrlPressed || event.isMetaPressed
                val isShift = event.isShiftPressed
                val isAlt = event.isAltPressed
                if (!isPrimary) return@onPreviewKeyEvent false

                when {
                    !isShift && !isAlt -> when (event.key) {
                        Key.A -> { fieldState.edit { selection = TextRange(0, length) }; true }
                        Key.B -> { inlineState.toggleStyle(MarkupStyle.Bold); true }
                        Key.I -> { inlineState.toggleStyle(MarkupStyle.Italic); true }
                        Key.U -> { inlineState.toggleStyle(MarkupStyle.Underline); true }
                        Key.Spacebar -> { inlineState.clearAllStyles(); true }
                        else -> false
                    }
                    isShift && !isAlt -> when (event.key) {
                        Key.S -> { inlineState.toggleStyle(MarkupStyle.Strikethrough); true }
                        Key.X -> { inlineState.toggleStyle(MarkupStyle.Strikethrough); true }
                        Key.H -> { inlineState.toggleStyle(MarkupStyle.Highlight); true }
                        Key.E -> { inlineState.toggleStyle(MarkupStyle.InlineCode); true }
                        else -> false
                    }
                    else -> false
                }
            },
    )
}

private fun applyInlineStyles(
    spans: List<StyleRange>,
    config: InlineStyleConfig,
    buffer: TextFieldBuffer,
) {
    with(buffer) {
        spans.forEach { span ->
            val safeStart = span.start.coerceIn(0, length)
            val safeEnd = span.end.coerceIn(0, length)
            if (safeStart >= safeEnd) return@forEach

            val spanStyle = when (val style = span.style) {
                is MarkupStyle.Inline -> when (style) {
                    MarkupStyle.Bold -> config.boldStyle
                    MarkupStyle.Italic -> config.italicStyle
                    MarkupStyle.Underline -> config.underlineStyle
                    MarkupStyle.Strikethrough -> config.strikethroughStyle
                    MarkupStyle.Highlight -> config.highlightStyle
                    MarkupStyle.InlineCode -> config.inlineCodeStyle
                }
                else -> return@forEach
            }

            addStyle(spanStyle, safeStart, safeEnd)
        }
    }
}
