package com.denser.hyphen.ui.material3

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.ui.HyphenBasicTextEditor
import com.denser.hyphen.ui.link.HyphenLinkConfig
import com.denser.hyphen.ui.style.HyphenStyleConfig
import com.denser.hyphen.ui.mention.HyphenMentionConfig
import com.denser.hyphen.model.TriggerState

/**
 * [Material Design filled text field](https://m3.material.io/components/text-fields/overview)
 * with built-in Markdown formatting support.
 *
 * This is a Material3-decorated wrapper around [HyphenBasicTextEditor]. It inherits all
 * markdown editing behaviour — inline styles, block prefixes, hardware shortcuts, and
 * Markdown-serializing clipboard — while adopting the standard Material3 filled text field
 * appearance including labels, icons, supporting text, and error state.
 *
 * If you do not need Material3 decoration, use [HyphenBasicTextEditor] directly.
 *
 * A minimal example:
 * ```kotlin
 * val state = rememberHyphenTextState()
 *
 * HyphenTextField(
 *     state = state,
 *     label = { Text("Notes") },
 *     placeholder = { Text("Start typing…") },
 * )
 * ```
 *
 * To read the formatted output as Markdown:
 * ```kotlin
 * val markdown = state.toMarkdown()
 * ```
 *
 * @param state The [HyphenTextState] that holds text content, spans, selection, and undo/redo
 *   history. Use [com.denser.hyphen.state.rememberHyphenTextState] to create and remember an instance.
 * @param modifier Optional [Modifier] applied to the text field container.
 * @param enabled Controls the enabled state of the text field.
 * @param readOnly When `true`, the field cannot be modified but can be focused and copied.
 * @param textStyle Typographic style applied to the input text. Defaults to [LocalTextStyle].
 *   Color is resolved from [colors] unless set explicitly.
 * @param labelPosition Controls where the label is displayed. See [TextFieldLabelPosition].
 * @param label Optional label composable.
 * @param placeholder Optional placeholder composable shown when the field is empty.
 * @param leadingIcon Optional icon composable at the start of the field.
 * @param trailingIcon Optional icon composable at the end of the field.
 * @param prefix Optional composable before the input text.
 * @param suffix Optional composable after the input text.
 * @param supportingText Optional helper or error text below the field.
 * @param isError Whether the field's current value is in an error state.
 * @param keyboardOptions Software keyboard options. Defaults to [KeyboardOptions.Default].
 * @param lineLimits Single-line or multi-line behaviour. Defaults to [TextFieldLineLimits.Default].
 * @param scrollState Scroll state for the field content.
 * @param shape Shape of the filled container. Defaults to [TextFieldDefaults.shape].
 * @param colors [TextFieldColors] used for foreground and background colors across states.
 * @param contentPadding Padding between the inner field and decoration elements.
 * @param interactionSource Optional hoisted [MutableInteractionSource].
 * @param styleConfig Visual configuration for each [com.denser.hyphen.model.MarkupStyle].
 * @param mentionConfig Interaction configuration for mention spans — provides click handlers,
 *   personalized dropdown menus, and hover card content.
 * @param triggerPopup Composable content shown in a popup when a trigger (like @ or #)
 *   is active. Provides the current [TriggerState] for rendering autocomplete suggestions.
 * @param onTextLayout Callback invoked on text layout recalculation.
 * @param clipboardLabel Label attached to clipboard entries on copy/cut.
 * @param horizontalPadding Internal horizontal padding required to draw blockquote background and border without text overlap. Defaults to 8.dp.
 * @param layoutDirection Optional [LayoutDirection] override. When `null`, automatically inherits [LocalLayoutDirection.current] from the surrounding Compose locale (RTL / LTR).
 * @param onTextChange Callback invoked whenever the plain text changes.
 * @param onMarkdownChange Callback invoked whenever text or formatting changes, providing the
 *   serialized Markdown string.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HyphenTextField(
    state: HyphenTextState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(),
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    scrollState: ScrollState = rememberScrollState(),
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    horizontalPadding: Dp = 8.dp,
    contentPadding: PaddingValues = if (label == null || labelPosition is TextFieldLabelPosition.Above) {
        TextFieldDefaults.contentPaddingWithoutLabel(
            start = (16.dp - horizontalPadding).coerceAtLeast(0.dp),
            end = (16.dp - horizontalPadding).coerceAtLeast(0.dp)
        )
    } else {
        TextFieldDefaults.contentPaddingWithLabel(
            start = (16.dp - horizontalPadding).coerceAtLeast(0.dp),
            end = (16.dp - horizontalPadding).coerceAtLeast(0.dp)
        )
    },
    interactionSource: MutableInteractionSource? = null,
    styleConfig: HyphenStyleConfig = HyphenStyleConfig(),
    linkConfig: HyphenLinkConfig = HyphenLinkConfig(),
    mentionConfig: HyphenMentionConfig = HyphenMentionConfig(),
    triggerPopup: @Composable (TriggerState) -> Unit = {},
    showDefaultSuggestionsPopup: Boolean = true,
    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
    clipboardLabel: String = "Markdown Text",
    layoutDirection: LayoutDirection? = null,
    onTextChange: ((String) -> Unit)? = null,
    onMarkdownChange: ((String) -> Unit)? = null,
) {
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by actualInteractionSource.collectIsFocusedAsState()

    val textColor = textStyle.color.takeOrElse {
        when {
            !enabled -> colors.disabledTextColor
            isError  -> colors.errorTextColor
            isFocused -> colors.focusedTextColor
            else -> colors.unfocusedTextColor
        }
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))
    val cursorColor = if (isError) colors.errorCursorColor else colors.cursorColor

    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        HyphenBasicTextEditor(
            state = state,
            modifier = modifier
                .semantics { if (isError) error("Invalid input") }
                .defaultMinSize(
                    minWidth = TextFieldDefaults.MinWidth,
                    minHeight = TextFieldDefaults.MinHeight,
                ),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = mergedTextStyle,
            styleConfig = styleConfig,
            linkConfig = linkConfig,
            mentionConfig = mentionConfig,
            triggerPopup = triggerPopup,
            showDefaultSuggestionsPopup = showDefaultSuggestionsPopup,
            keyboardOptions = keyboardOptions,
            lineLimits = lineLimits,
            scrollState = scrollState,
            interactionSource = actualInteractionSource,
            cursorBrush = SolidColor(cursorColor),
            onTextLayout = onTextLayout,
            clipboardLabel = clipboardLabel,
            horizontalPadding = horizontalPadding,
            layoutDirection = layoutDirection,
            onTextChange = onTextChange,
            onMarkdownChange = onMarkdownChange,
            decorator = TextFieldDefaults.decorator(
                state = state.textFieldState,
                enabled = enabled,
                lineLimits = lineLimits,
                outputTransformation = null,
                interactionSource = actualInteractionSource,
                labelPosition = labelPosition,
                label = label?.let { { it() } },
                placeholder = placeholder?.let {
                    @Composable {
                        Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                            it()
                        }
                    }
                },
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                prefix = prefix,
                suffix = suffix,
                supportingText = supportingText,
                isError = isError,
                colors = colors,
                contentPadding = contentPadding,
                container = {
                    TextFieldDefaults.Container(
                        enabled = enabled,
                        isError = isError,
                        interactionSource = actualInteractionSource,
                        colors = colors,
                        shape = shape,
                    )
                },
            ),
        )
    }
}