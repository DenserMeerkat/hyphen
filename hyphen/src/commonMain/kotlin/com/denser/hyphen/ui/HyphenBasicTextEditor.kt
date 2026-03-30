package com.denser.hyphen.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.runtime.*
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
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.ui.link.HyphenLinkConfig
import com.denser.hyphen.ui.style.HyphenStyleConfig
import com.denser.hyphen.ui.link.LinkEditDialog
import com.denser.hyphen.ui.internal.InlineContentHost
import com.denser.hyphen.ui.internal.applyMarkdownStyles
import com.denser.hyphen.ui.internal.handleHardwareKeyEvent
import com.denser.hyphen.ui.internal.processMarkdownInput
import com.denser.hyphen.ui.internal.rememberMarkdownClipboard

/**
 * A markdown-aware text editor that provides rich inline formatting, block-level styles,
 * hardware keyboard shortcuts, and clipboard serialization — all built on top of
 * [BasicTextField].
 *
 * Markdown syntax typed by the user (e.g. `**bold**`, `_italic_`, `- list item`) is
 * automatically detected and stripped from the visible text. The corresponding visual styles
 * are applied via an [androidx.compose.foundation.text.input.OutputTransformation] so the
 * underlying [HyphenTextState] always holds clean, undecorated text.
 *
 * **Link interactions**
 *
 * Pass a [HyphenLinkConfig] to customise how link taps, context menus, and the edit dialog
 * behave. Omit it (or pass `HyphenLinkConfig()`) to use the built-in UI.
 *
 * @param state The [HyphenTextState] that holds text content, spans, selection, and
 *   undo/redo history. Use [com.denser.hyphen.state.rememberHyphenTextState] to create
 *   and remember an instance.
 * @param modifier Optional [Modifier] applied to the underlying [BasicTextField].
 * @param enabled Controls the enabled state of the text field.
 * @param readOnly When `true`, the field cannot be modified but can be focused and copied.
 * @param textStyle Typographic style applied to the visible text. Defaults to 16 sp.
 * @param styleConfig Visual configuration for each [com.denser.hyphen.model.MarkupStyle].
 * @param linkConfig Interaction configuration for link spans — custom dropdown menu, custom
 *   edit dialog, and/or a custom URL-open handler. Defaults to built-in UI.
 * @param keyboardOptions Software keyboard options. Defaults to sentence capitalisation with
 *   autocorrect disabled.
 * @param lineLimits Single-line or multi-line behaviour.
 * @param scrollState Scroll state for the field content.
 * @param interactionSource Optional hoisted [MutableInteractionSource].
 * @param cursorBrush [Brush] used to paint the cursor.
 * @param decorator Optional [TextFieldDecorator] for Material3 decorations.
 * @param onTextLayout Callback invoked on text layout recalculation.
 * @param clipboardLabel Label attached to clipboard entries on copy/cut.
 * @param onTextChange Callback invoked whenever the plain text changes.
 * @param onMarkdownChange Callback invoked whenever text or formatting changes, providing
 *   the serialized Markdown string.
 */
@Composable
fun HyphenBasicTextEditor(
    state: HyphenTextState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = TextStyle(fontSize = 16.sp),
    styleConfig: HyphenStyleConfig = HyphenStyleConfig(),
    linkConfig: HyphenLinkConfig = HyphenLinkConfig(),
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
        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

        val wrappedDecorator = TextFieldDecorator { innerTextField ->
            val hostContent = @Composable {
                InlineContentHost(
                    state = state,
                    textLayoutResult = { textLayoutResult },
                    scrollState = scrollState,
                    linkConfig = linkConfig,
                    textStyle = textStyle,
                    modifier = Modifier,
                ) {
                    innerTextField()
                }
            }

            if (decorator != null) {
                decorator.Decoration {
                    hostContent()
                }
            } else {
                hostContent()
            }
        }

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
            decorator = wrappedDecorator,
            onTextLayout = { getResult ->
                textLayoutResult = getResult()
                onTextLayout?.invoke(this, getResult)
            },
            outputTransformation = {
                applyMarkdownStyles(state, styleConfig, textStyle, this)
            },
            inputTransformation = {
                processMarkdownInput(state, this)
            },
        )
    }

    val activeLink = state.activeLinkForEditing
    if (activeLink != null && linkConfig.dialogContent == null) {
        val linkStyle = activeLink.style as? MarkupStyle.Link
        if (linkStyle != null) {
            val currentText = state.textFieldState.text
                .substring(activeLink.start.coerceAtMost(state.text.length),
                    activeLink.end.coerceAtMost(state.text.length))
            LinkEditDialog(
                initialText = currentText,
                initialUrl = linkStyle.url,
                onDismiss = { state.activeLinkForEditing = null },
                onConfirm = { newText, newUrl ->
                    state.updateLink(activeLink, newText, newUrl)
                    state.activeLinkForEditing = null
                },
            )
        }
    }
}