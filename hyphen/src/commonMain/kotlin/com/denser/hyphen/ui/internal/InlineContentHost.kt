package com.denser.hyphen.ui.internal

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.ui.link.HyphenLinkConfig
import com.denser.hyphen.ui.checkbox.InlineCheckbox
import com.denser.hyphen.ui.link.InlineLink
import com.denser.hyphen.ui.mention.HyphenMentionConfig
import com.denser.hyphen.ui.mention.InlineMention
import com.denser.hyphen.model.TriggerState
import kotlin.math.roundToInt

@Composable
internal fun InlineContentHost(
    state: HyphenTextState,
    textLayoutResult: () -> TextLayoutResult?,
    scrollState: ScrollState,
    linkConfig: HyphenLinkConfig,
    mentionConfig: HyphenMentionConfig,
    triggerPopup: @Composable (TriggerState) -> Unit,
    showDefaultSuggestionsPopup: Boolean,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    @OptIn(ExperimentalMaterial3Api::class)
    SubcomposeLayout(modifier = modifier.clipToBounds()) { constraints ->
        val standardConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val contentMeasurables = subcompose("content", content)
        val contentPlaceable = contentMeasurables.first().measure(constraints)

        val overlaySpans = state.spans.filter {
            it.style is MarkupStyle.CheckboxChecked ||
                    it.style is MarkupStyle.CheckboxUnchecked ||
                    it.style is MarkupStyle.Link ||
                    it.style is MarkupStyle.Mention
        }

        val layoutResult = textLayoutResult()

        val inlinePlaceables = overlaySpans.flatMap { span ->
            val key = "${span.start}_${span.style.hashCode()}"
            subcompose(key) {
                when (val style = span.style) {
                    is MarkupStyle.CheckboxUnchecked, is MarkupStyle.CheckboxChecked -> {
                        InlineCheckbox(style, span.start, state, textStyle)
                    }
                    is MarkupStyle.Link -> {
                        InlineLink(
                            span = span,
                            state = state,
                            linkConfig = linkConfig,
                        )
                    }
                    is MarkupStyle.Mention -> {
                        InlineMention(
                            span = span,
                            state = state,
                            mentionConfig = mentionConfig,
                        )
                    }
                    else -> {}
                }
            }.map { measurable ->
                val finalConstraints = if ((span.style is MarkupStyle.Link || span.style is MarkupStyle.Mention) && layoutResult != null) {
                    val textLen = layoutResult.layoutInput.text.length
                    if (textLen == 0) {
                        Constraints.fixed(0, 0)
                    } else {
                        val transformedStart = HyphenOffsetMapper.toVisual(span.start, state)
                            .coerceIn(0, textLen - 1)
                        val transformedEnd = HyphenOffsetMapper.toVisual(span.end, state)
                            .coerceIn(0, textLen - 1)
                            .let { if (it > transformedStart) it else transformedStart }

                        val startBox = layoutResult.getBoundingBox(transformedStart)
                        val lastCharIndex = (transformedEnd - 1).coerceAtLeast(transformedStart)
                        val endBox = layoutResult.getBoundingBox(lastCharIndex)

                        val width = (endBox.right - startBox.left).coerceAtLeast(0f).roundToInt()
                        val height = (endBox.bottom - startBox.top).coerceAtLeast(0f).roundToInt()
                        Constraints.fixed(width, height)
                    }
                } else {
                    standardConstraints
                }
                Pair(span, measurable.measure(finalConstraints))
            }
        }

        val triggerState = state.activeTrigger
        val triggerPopupContent = if (showDefaultSuggestionsPopup && triggerState != null && layoutResult != null) {
            subcompose("trigger_popup") {
                val scrollY = scrollState.value
                val density = LocalDensity.current
                var maxAvailableHeight by remember { mutableStateOf(500.dp) }

                Box(Modifier.size(0.dp)) {
                    HyphenInlinePopup(
                        onDismiss = { state.updateActiveTrigger(null) },
                        focusable = false
                    ) {
                        Box(Modifier.heightIn(max = 300.dp)) {
                            triggerPopup(triggerState)
                        }
                    }
                }
            }
        } else null

        layout(contentPlaceable.width, contentPlaceable.height) {
            contentPlaceable.placeRelative(0, 0)
            
            if (layoutResult != null) {
                val scrollY = scrollState.value
                val textLen = layoutResult.layoutInput.text.length
                inlinePlaceables.forEach { (span, placeable) ->
                    if (textLen == 0) return@forEach

                    val transformedIndex = HyphenOffsetMapper.toVisual(span.start, state)
                        .coerceIn(0, textLen - 1)
                    val boundingBox = layoutResult.getBoundingBox(transformedIndex)

                    val lineTop = boundingBox.top.roundToInt()
                    val lineBottom = boundingBox.bottom.roundToInt()
                    val lineHeight = lineBottom - lineTop
                    val x = boundingBox.left.roundToInt()

                    if (span.style is MarkupStyle.Link || span.style is MarkupStyle.Mention) {
                        placeable.placeRelative(x, lineTop - scrollY)
                    } else {
                        val y = lineTop + (lineHeight - placeable.height) / 2 - scrollY
                        placeable.placeRelative(x, y)
                    }
                }

                if (triggerState != null && triggerPopupContent != null) {
                    val textLen = layoutResult.layoutInput.text.length
                    if (textLen > 0) {
                        val transformedIndex = HyphenOffsetMapper.toVisual(triggerState.startIndex, state)
                            .coerceIn(0, textLen - 1)
                        val boundingBox = layoutResult.getBoundingBox(transformedIndex)
                        
                        val x = boundingBox.left.roundToInt()
                        val y = (boundingBox.top - scrollY).roundToInt()
                        val lineHeight = (boundingBox.bottom - boundingBox.top).roundToInt()

                        triggerPopupContent.forEach { measurable ->
                            val placeable = measurable.measure(Constraints.fixed(0, lineHeight))
                            placeable.placeRelative(x, y)
                        }
                    }
                }
            }
        }
    }
}