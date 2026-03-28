package com.denser.hyphen.blocks.ui.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denser.hyphen.blocks.model.BlockquoteBlock
import com.denser.hyphen.blocks.state.HyphenBlockState
import com.denser.hyphen.blocks.state.rememberBlockFocusHandler
import com.denser.hyphen.blocks.state.rememberBlockInlineState
import com.denser.hyphen.blocks.ui.BlockInputTransformation
import com.denser.hyphen.blocks.ui.handleBlockKeyEvent
import com.denser.hyphen.inline.ui.HyphenInlineEditor

@Composable
fun BlockquoteBlock(
    block: BlockquoteBlock,
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
    val borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    val bgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp, horizontal = 16.dp)
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
            .background(bgColor),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(borderColor),
        )

        HyphenInlineEditor(
            fieldState = block.state,
            inlineState = inlineState,
            inputTransformation = blockTransformation,
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            onTextLayout = { textLayoutResult = it },
            modifier = Modifier
                .weight(1f)
                .focusRequester(blockState.getFocusRequester(block.id))
                .onFocusChanged(focusHandler::onFocusChanged)
                .padding(horizontal = 12.dp, vertical = 8.dp)
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