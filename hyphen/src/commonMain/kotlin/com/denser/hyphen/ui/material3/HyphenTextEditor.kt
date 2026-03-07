package com.denser.hyphen.ui.material3

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
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
import com.denser.hyphen.ui.HyphenStyleConfig

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
 * HyphenTextEditor(
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
 * @param enabled Controls the enabled state of the text field. When `false`, the field is
 *   neither editable nor focusable, and it will appear visually disabled to accessibility
 *   services.
 * @param readOnly Controls the editable state of the text field. When `true`, the field cannot
 *   be modified but can be focused and its text can be copied.
 * @param textStyle Typographic style applied to the input text. Defaults to [LocalTextStyle].
 *   The text color is resolved from [colors] when not explicitly specified in the style.
 * @param labelPosition Controls where the label is displayed relative to the field. See
 *   [TextFieldLabelPosition].
 * @param label Optional label displayed inside or above the field. Uses
 *   [Typography.bodySmall] when minimized and [Typography.bodyLarge] when expanded.
 * @param placeholder Optional placeholder displayed when the input text is empty. Uses
 *   [Typography.bodyLarge].
 * @param leadingIcon Optional icon composable displayed at the start of the field container.
 * @param trailingIcon Optional icon composable displayed at the end of the field container.
 * @param prefix Optional composable displayed before the input text inside the field.
 * @param suffix Optional composable displayed after the input text inside the field.
 * @param supportingText Optional helper or error text displayed below the field container.
 * @param isError Indicates whether the field's current value is in an error state. When
 *   `true`, the label, bottom indicator, and trailing icon are tinted with the error color,
 *   and an error is announced to accessibility services.
 * @param keyboardOptions Software keyboard options such as keyboard type and IME action.
 *   Defaults to [KeyboardOptions.Default].
 * @param lineLimits Whether the field should be single-line (horizontal scroll) or multi-line
 *   (vertical grow/scroll). Defaults to [TextFieldLineLimits.Default].
 * @param scrollState Scroll state managing vertical or horizontal scroll of the field content.
 * @param shape The shape of the field's filled container. Defaults to
 *   [TextFieldDefaults.shape].
 * @param colors [TextFieldColors] used to resolve foreground and background colors across
 *   enabled, focused, and error states. See [TextFieldDefaults.colors].
 * @param contentPadding Padding between the inner text field and the surrounding decoration
 *   elements. Defaults to [TextFieldDefaults.contentPaddingWithLabel] when a label is present
 *   in attached position, or [TextFieldDefaults.contentPaddingWithoutLabel] otherwise.
 * @param interactionSource Optional hoisted [MutableInteractionSource] for observing
 *   interactions. If `null`, an internal source is created and used.
 * @param styleConfig Visual configuration for each [com.denser.hyphen.model.MarkupStyle] — colors, weights, and
 *   decorations used when rendering formatted text.
 * @param onTextLayout Callback invoked whenever the text layout is recalculated. Provides a
 *   deferred [TextLayoutResult] useful for cursor drawing or hit testing.
 * @param clipboardLabel Label attached to the clipboard entry when text is copied.
 * @param onTextChange Optional callback invoked whenever the plain, undecorated text changes.
 * @param onMarkdownChange Optional callback invoked whenever the text OR formatting changes,
 * providing the fully serialized Markdown string. Ideal for syncing with ViewModels.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HyphenTextEditor(
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
    contentPadding: PaddingValues = if (label == null || labelPosition is TextFieldLabelPosition.Above) {
        TextFieldDefaults.contentPaddingWithoutLabel()
    } else {
        TextFieldDefaults.contentPaddingWithLabel()
    },
    interactionSource: MutableInteractionSource? = null,

    styleConfig: HyphenStyleConfig = HyphenStyleConfig(),
    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
    clipboardLabel: String = "Markdown Text",
    onTextChange: ((String) -> Unit)? = null,
    onMarkdownChange: ((String) -> Unit)? = null,
) {
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by actualInteractionSource.collectIsFocusedAsState()

    val textColor = textStyle.color.takeOrElse {
        when {
            !enabled -> colors.disabledTextColor
            isError -> colors.errorTextColor
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
            keyboardOptions = keyboardOptions,
            lineLimits = lineLimits,
            scrollState = scrollState,
            interactionSource = actualInteractionSource,
            cursorBrush = SolidColor(cursorColor),
            onTextLayout = onTextLayout,
            clipboardLabel = clipboardLabel,
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
                placeholder = placeholder,
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
                }
            )
        )
    }
}