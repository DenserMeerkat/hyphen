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
    onValueChange: ((String) -> Unit)? = null,
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
            onValueChange = onValueChange,

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