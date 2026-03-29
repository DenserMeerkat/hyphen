package com.denser.hyphen.ui.inline

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.IntSize

@OptIn(ExperimentalComposeUiApi::class)
internal fun Modifier.sharedPointerInput(
    vararg keys: Any?,
    pass: PointerEventPass = PointerEventPass.Main,
    onEvent: (PointerEvent) -> Unit,
): Modifier = this then SharedPointerInputElement(keys.toList(), pass, onEvent)

@OptIn(ExperimentalComposeUiApi::class)
private data class SharedPointerInputElement(
    val keys: List<Any?>,
    val pass: PointerEventPass,
    val onEvent: (PointerEvent) -> Unit,
) : ModifierNodeElement<SharedPointerInputNode>() {
    override fun create() = SharedPointerInputNode(pass, onEvent)
    override fun update(node: SharedPointerInputNode) {
        node.pass = pass
        node.onEvent = onEvent
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "sharedPointerInput"
        properties["keys"] = keys
    }
}

@OptIn(ExperimentalComposeUiApi::class)
internal class SharedPointerInputNode(
    var pass: PointerEventPass,
    var onEvent: (PointerEvent) -> Unit,
) : Modifier.Node(), PointerInputModifierNode {

    override fun sharePointerInputWithSiblings(): Boolean = true

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize,
    ) {
        if (pass == this.pass) {
            onEvent(pointerEvent)
        }
    }

    override fun onCancelPointerInput() = Unit
}