package com.denser.hyphen.blocks.ui.blocks

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denser.hyphen.blocks.model.OrderedListBlock
import com.denser.hyphen.blocks.state.HyphenBlockState
import com.denser.hyphen.blocks.state.rememberBlockFocusHandler
import com.denser.hyphen.blocks.state.rememberBlockInlineState
import com.denser.hyphen.blocks.ui.BlockInputTransformation
import com.denser.hyphen.blocks.ui.handleBlockKeyEvent
import com.denser.hyphen.inline.ui.HyphenInlineEditor

@Composable
fun OrderedListBlock(
    block: OrderedListBlock,
    index: Int,
    blockState: HyphenBlockState,
    modifier: Modifier = Modifier,
) {
    var textLayoutResult by remember { mutableStateOf<(() -> TextLayoutResult?)?>(null) }
    val focusHandler =
        rememberBlockFocusHandler(block.id, block.state, blockState, textLayoutResult)
    val inlineState = rememberBlockInlineState(
        blockId = block.id,
        fieldState = block.state,
        blockState = blockState,
        initialSpans = block.spans,
    )
    val blockTransformation = remember(block.id, blockState) {
        BlockInputTransformation(block.id, blockState)
    }
    val number = blockState.orderedListNumber(index)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "$number.",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            ),
            modifier = Modifier
                .widthIn(min = 16.dp)
                .wrapContentWidth(Alignment.End)
                .padding(top = 3.dp),
        )

        HyphenInlineEditor(
            fieldState = block.state,
            inlineState = inlineState,
            inputTransformation = blockTransformation,
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
            ),
            onTextLayout = { textLayoutResult = it },
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
                .focusRequester(blockState.getFocusRequester(block.id))
                .onFocusChanged(focusHandler::onFocusChanged)
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