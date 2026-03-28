package com.denser.hyphen.blocks.ui.blocks

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denser.hyphen.blocks.model.TextBlock
import com.denser.hyphen.blocks.state.HyphenBlockState
import com.denser.hyphen.blocks.state.rememberBlockFocusHandler
import com.denser.hyphen.blocks.state.rememberBlockInlineState
import com.denser.hyphen.blocks.ui.BlockInputTransformation
import com.denser.hyphen.blocks.ui.handleBlockKeyEvent
import com.denser.hyphen.inline.ui.HyphenInlineEditor

@Composable
fun TextBlock(
    block: TextBlock,
    blockState: HyphenBlockState,
    modifier: Modifier = Modifier,
) {
    var textLayoutResult by remember { mutableStateOf<(() -> TextLayoutResult?)?>(null) }
    val focusHandler = rememberBlockFocusHandler(block.id, block.state, blockState, textLayoutResult)
    val inlineState = rememberBlockInlineState(
        blockId = block.id,
        fieldState = block.state,
        blockState = blockState,
        initialSpans = block.spans,
    )
    val blockTransformation = remember(block.id, blockState) {
        BlockInputTransformation(block.id, blockState)
    }

    val baseStyle = when (block.type) {
        TextBlock.TextType.H1 -> TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)
        TextBlock.TextType.H2 -> TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
        TextBlock.TextType.H3 -> TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
        TextBlock.TextType.H4 -> TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
        TextBlock.TextType.H5 -> TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold)
        TextBlock.TextType.H6 -> TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
        TextBlock.TextType.Paragraph -> TextStyle(fontSize = 16.sp)
    }

    val textStyle = baseStyle.merge(
        TextStyle(
            color = if (block.type == TextBlock.TextType.H6) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface
        )
    )

    key(block.type) {
        HyphenInlineEditor(
            fieldState = block.state,
            inlineState = inlineState,
            inputTransformation = blockTransformation,
            textStyle = textStyle,
            onTextLayout = { textLayoutResult = it },
            modifier = modifier
                .fillMaxWidth()
                .focusRequester(blockState.getFocusRequester(block.id))
                .onFocusChanged(focusHandler::onFocusChanged)
                .padding(vertical = 4.dp, horizontal = 16.dp)
                .onPreviewKeyEvent { event ->
                    handleBlockKeyEvent(
                        event = event,
                        blockId = block.id,
                        fieldState = block.state,
                        blockState = blockState,
                        focusHandler = focusHandler,
                        layout = textLayoutResult,
                    )
                },
        )
    }
}