package com.denser.hyphen.blocks.ui.blocks

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denser.hyphen.blocks.model.CheckboxBlock
import com.denser.hyphen.blocks.state.HyphenBlockState
import com.denser.hyphen.blocks.state.rememberBlockFocusHandler
import com.denser.hyphen.blocks.state.rememberBlockInlineState
import com.denser.hyphen.blocks.ui.BlockInputTransformation
import com.denser.hyphen.blocks.ui.handleBlockKeyEvent
import com.denser.hyphen.inline.ui.HyphenInlineEditor

@Composable
fun CheckboxBlock(
    block: CheckboxBlock,
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(
            checked = block.isChecked,
            onCheckedChange = { blockState.toggleCheckbox(block.id) },
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
                .scale(0.75f)
                .focusProperties { canFocus = false },
        )
        Spacer(Modifier.width(8.dp))
        HyphenInlineEditor(
            fieldState = block.state,
            inlineState = inlineState,
            inputTransformation = blockTransformation,
            textStyle = TextStyle(
                fontSize = 16.sp,
                textDecoration = if (block.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                color = if (block.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            ),
            onTextLayout = { textLayoutResult = it },
            modifier = Modifier
                .weight(1f)
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