package com.denser.hyphen.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import com.denser.hyphen.state.HyphenTextState

/**
 * A markdown-aware text editor that provides rich inline formatting, block-level styles,
 * hardware keyboard shortcuts, and clipboard serialization — all built on top of
 * [BasicTextField].
 *
 * Markdown syntax typed by the user (e.g. `**bold**`, `_italic_`, `- list item`) is
 * automatically detected and stripped from the visible text. The corresponding visual styles
 * are applied via an [androidx.compose.foundation.text.input.OutputTransformation] so the underlying [HyphenTextState] always holds
 * clean, undecorated text.
 *
 * Focus state and selection are tracked internally so that toolbar buttons invoked after the
 * field loses focus (e.g. a floating format bar) still operate on the correct range.
 *
 * **Hardware keyboard shortcuts**
 *
 * | Shortcut | Action |
 * |---|---|
 * | Ctrl/Cmd + B | Toggle bold |
 * | Ctrl/Cmd + I | Toggle italic |
 * | Ctrl/Cmd + U | Toggle underline |
 * | Ctrl/Cmd + Space | Clear all styles on selection |
 * | Ctrl/Cmd + Shift + S / X | Toggle strikethrough |
 * | Ctrl/Cmd + Shift + H | Toggle highlight |
 * | Ctrl/Cmd + [1–6] | Toggle Heading 1–6 |
 * | Ctrl/Cmd + Enter | Toggle checkbox checked / unchecked |
 * | Ctrl/Cmd + Z | Undo |
 * | Ctrl/Cmd + Y / Shift + Z | Redo |
 *
 * **Clipboard**
 *
 * Cut, copy operations serialize the selected range to Markdown via [HyphenTextState.toMarkdown],
 * so pasting into another Markdown-aware editor preserves formatting.
 *
 * @param state The [HyphenTextState] that holds text content, spans, selection, and undo/redo
 * history. Use [com.denser.hyphen.state.rememberHyphenTextState] to create and remember an instance.
 * @param modifier Optional [Modifier] applied to the underlying [BasicTextField].
 * @param enabled Controls the enabled state of the text field. When `false`, the field is
 * neither editable nor focusable, and its input cannot be selected.
 * @param readOnly Controls the editable state of the text field. When `true`, the field cannot
 * be modified but can be focused and its text can be copied.
 * @param textStyle Typographic style applied to the visible text. Defaults to 16 sp body text.
 * Color and font properties are merged with the active theme where applicable.
 * @param styleConfig Visual configuration for each [com.denser.hyphen.model.MarkupStyle] — colors, weights, and
 * decorations used by the output transformation when rendering formatted text.
 * @param keyboardOptions Software keyboard options such as capitalization, autocorrect, and
 * [ImeAction]. Defaults to sentence capitalization with autocorrect disabled.
 * @param lineLimits Whether the field should be single-line (horizontal scroll) or multi-line
 * (vertical grow/scroll). Defaults to [TextFieldLineLimits.Default].
 * @param scrollState Scroll state managing vertical or horizontal scroll of the field content.
 * @param interactionSource Optional hoisted [MutableInteractionSource] for observing focus,
 * hover, and press interactions. If `null`, an internal source is used.
 * @param cursorBrush [Brush] used to paint the cursor. Pass [SolidColor] with
 * [Color.Unspecified] to hide the cursor entirely.
 * @param decorator Optional [TextFieldDecorator] that wraps the inner text field with
 * decorations such as labels, icons, or borders (e.g. a Material3 decorator).
 * @param onTextLayout Callback invoked whenever the text layout is recalculated. Provides a
 * deferred [TextLayoutResult] that can be used for cursor drawing or hit testing.
 * @param clipboardLabel Label attached to the clipboard entry when text is copied. Used by
 * some platforms to describe the clipboard contents.
 * @param onTextChange Optional callback invoked whenever the plain, undecorated text changes.
 * @param onMarkdownChange Optional callback invoked whenever the text OR formatting changes,
 * providing the fully serialized Markdown string. Ideal for syncing with ViewModels.
 */
@Composable
fun HyphenBasicTextEditor(
    state: HyphenTextState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = TextStyle(fontSize = 16.sp),
    styleConfig: HyphenStyleConfig = HyphenStyleConfig(),
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Default,
    ),
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    scrollState: ScrollState = rememberScrollState(),
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(Color.Black),
    decorator: TextFieldDecorator? = null,
    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
    clipboardLabel: String = "Markdown Text",
    onTextChange: ((String) -> Unit)? = null,
    onMarkdownChange: ((String) -> Unit)? = null,
) {
    val customClipboard = rememberMarkdownClipboard(state, clipboardLabel)

    LaunchedEffect(state.selection) {
        state.updateSelection(state.selection)
    }

    LaunchedEffect(state.text, state.spans.toList()) {
        onTextChange?.invoke(state.text)
        onMarkdownChange?.invoke(state.toMarkdown())
    }

    CompositionLocalProvider(LocalClipboard provides customClipboard) {
        BasicTextField(
            state = state.textFieldState,
            modifier = modifier
                .onPreviewKeyEvent { event -> handleHardwareKeyEvent(event, state) }
                .onFocusChanged { focusState ->
                    state.isFocused = focusState.isFocused
                },
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            keyboardOptions = keyboardOptions,
            lineLimits = lineLimits,
            scrollState = scrollState,
            interactionSource = interactionSource,
            cursorBrush = cursorBrush,
            decorator = decorator,
            onTextLayout = onTextLayout,
            outputTransformation = {
                applyMarkdownStyles(state, styleConfig, textStyle, this)
            },
            inputTransformation = {
                processMarkdownInput(state, this)
            }
        )
    }
}