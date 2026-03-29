package com.denser.hyphen.ui.inline

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
internal actual fun LinkPointerSurface(
    span: MarkupStyleRange,
    onOpenUrl: () -> Unit,
    onShowMenu: (pressOffset: Offset) -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier.androidLongPressShared(
            key = span,
            onLongPress = onShowMenu,
        ),
        content = content,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.androidLongPressShared(
    key: Any?,
    longPressTimeoutMs: Long = 500L,
    touchSlopPx: Float = 18f,
    onLongPress: (offset: Offset) -> Unit,
): Modifier = this then AndroidLongPressElement(key, longPressTimeoutMs, touchSlopPx, onLongPress)

@OptIn(ExperimentalComposeUiApi::class)
private data class AndroidLongPressElement(
    val key: Any?,
    val longPressTimeoutMs: Long,
    val touchSlopPx: Float,
    val onLongPress: (Offset) -> Unit,
) : ModifierNodeElement<AndroidLongPressNode>() {
    override fun create() = AndroidLongPressNode(longPressTimeoutMs, touchSlopPx, onLongPress)
    override fun update(node: AndroidLongPressNode) {
        node.longPressTimeoutMs = longPressTimeoutMs
        node.touchSlopPx = touchSlopPx
        node.onLongPress = onLongPress
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "androidLongPressShared"
        properties["key"] = key
        properties["longPressTimeoutMs"] = longPressTimeoutMs
        properties["touchSlopPx"] = touchSlopPx
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private class AndroidLongPressNode(
    var longPressTimeoutMs: Long,
    var touchSlopPx: Float,
    var onLongPress: (Offset) -> Unit,
) : Modifier.Node(), PointerInputModifierNode {

    override fun sharePointerInputWithSiblings(): Boolean = true

    private val scope = CoroutineScope(Dispatchers.Main)
    private var longPressJob: Job? = null
    private var pressPosition = Offset.Zero
    private var lastEvent: PointerEvent? = null

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
                lastEvent = pointerEvent

                longPressJob?.cancel()
                longPressJob = scope.launch {
                    delay(longPressTimeoutMs)
                    lastEvent?.changes?.forEach { it.consume() }
                    onLongPress(pressPosition)
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

            PointerEventType.Release,
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