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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import com.denser.hyphen.state.BlockStyleManager
import com.denser.hyphen.state.HyphenTextState

@Composable
fun HyphenTextEditor(
    state: HyphenTextState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = TextStyle(fontSize = 16.sp),
    styleConfig: HyphenStyleConfig = HyphenStyleConfig(),
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        autoCorrect = false,
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
    onValueChange: ((String) -> Unit)? = null,
) {
    val customClipboard = rememberMarkdownClipboard(state, clipboardLabel)

    CompositionLocalProvider(LocalClipboard provides customClipboard) {
        BasicTextField(
            state = state.textFieldState,
            modifier = modifier.onPreviewKeyEvent { event ->
                when {
                    event.key == Key.Enter && event.type == KeyEventType.KeyDown -> {
                        state.textFieldState.edit {
                            val handled = BlockStyleManager.handleSmartEnter(state, this)
                            if (handled) {
                                state.processInput(this)
                            }
                        }
                        true
                    }
                    else -> false
                }
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
                applyMarkdownStyles(state, styleConfig, this)
            },
            inputTransformation = {
                processMarkdownInput(state, onValueChange, this)
            }
        )
    }
}