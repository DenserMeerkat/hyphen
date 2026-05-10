package com.denser.hyphen.ui.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.IntSize
import com.denser.hyphen.model.MarkupStyleRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
actual fun HyphenPointerSurface(
    span: MarkupStyleRange,
    onHoverChanged: (Boolean) -> Unit,
    onClick: (isCtrlPressed: Boolean, isRightClick: Boolean, offset: Offset) -> Boolean,
    pointerIcon: PointerIcon?,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier.hyphenPointerInput(
            key = span,
            onClick = onClick
        ),
        content = content,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.hyphenPointerInput(
    key: Any?,
    onClick: (isCtrl: Boolean, isRight: Boolean, offset: Offset) -> Boolean,
): Modifier = this then HyphenPointerElement(key, onClick)

@OptIn(ExperimentalComposeUiApi::class)
private data class HyphenPointerElement(
    val key: Any?,
    val onClick: (Boolean, Boolean, Offset) -> Boolean,
) : ModifierNodeElement<HyphenPointerNode>() {
    override fun create() = HyphenPointerNode(onClick)
    override fun update(node: HyphenPointerNode) {
        node.onClick = onClick
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "hyphenPointerInput"
        properties["key"] = key
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private class HyphenPointerNode(
    var onClick: (Boolean, Boolean, Offset) -> Boolean,
) : Modifier.Node(), PointerInputModifierNode {

    override fun sharePointerInputWithSiblings(): Boolean = true

    private val scope = CoroutineScope(Dispatchers.Main)
    private var longPressJob: Job? = null
    private var pressPosition = Offset.Zero
    private var isLongPress = false
    
    private val longPressTimeoutMs = 500L
    private val touchSlopPx = 18f

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize,
    ) {
        if (pass != PointerEventPass.Main) return

        when (pointerEvent.type) {
            PointerEventType.Press -> {
                val change = pointerEvent.changes.firstOrNull() ?: return
                pressPosition = change.position
                isLongPress = false

                longPressJob?.cancel()
                longPressJob = scope.launch {
                    delay(longPressTimeoutMs)
                    isLongPress = true
                    val handled = onClick(false, true, pressPosition)
                    if (handled) {
                        pointerEvent.changes.forEach { it.consume() }
                    }
                    longPressJob = null
                }
            }

            PointerEventType.Move -> {
                val change = pointerEvent.changes.firstOrNull() ?: return
                if ((change.position - pressPosition).getDistance() > touchSlopPx) {
                    longPressJob?.cancel()
                    longPressJob = null
                }
            }

            PointerEventType.Release -> {
                longPressJob?.cancel()
                longPressJob = null
                
                if (!isLongPress) {
                    val change = pointerEvent.changes.firstOrNull() ?: return
                    val handled = onClick(false, false, change.position)
                    if (handled) {
                        pointerEvent.changes.forEach { it.consume() }
                    }
                }
            }

            PointerEventType.Exit -> {
                longPressJob?.cancel()
                longPressJob = null
            }

            else -> Unit
        }
    }

    override fun onCancelPointerInput() {
        longPressJob?.cancel()
        longPressJob = null
    }

    override fun onDetach() {
        longPressJob?.cancel()
    }
}
