package com.denser.hyphen.ui.internal

import androidx.compose.foundation.ScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.ui.link.HyphenLinkConfig
import com.denser.hyphen.ui.checkbox.InlineCheckbox
import com.denser.hyphen.ui.link.InlineLink
import kotlin.math.roundToInt

@Composable
internal fun InlineContentHost(
    state: HyphenTextState,
    textLayoutResult: () -> TextLayoutResult?,
    scrollState: ScrollState,
    linkConfig: HyphenLinkConfig,
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
                    it.style is MarkupStyle.Link
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
                    else -> {}
                }
            }.map { measurable ->
                val finalConstraints = if (span.style is MarkupStyle.Link && layoutResult != null) {
                    val transformedStart = HyphenOffsetMapper.toVisual(span.start, state)
                        .coerceIn(0, layoutResult.layoutInput.text.length)
                    val transformedEnd = HyphenOffsetMapper.toVisual(span.end, state)
                        .coerceIn(0, layoutResult.layoutInput.text.length)
                        .let { if (it > transformedStart) it else transformedStart }

                    val startBox = layoutResult.getBoundingBox(transformedStart)
                    val lastCharIndex = (transformedEnd - 1).coerceAtLeast(transformedStart)
                    val endBox = layoutResult.getBoundingBox(lastCharIndex)

                    val width = (endBox.right - startBox.left).coerceAtLeast(0f).roundToInt()
                    val height = (endBox.bottom - startBox.top).coerceAtLeast(0f).roundToInt()
                    Constraints.fixed(width, height)
                } else {
                    standardConstraints
                }
                Pair(span, measurable.measure(finalConstraints))
            }
        }

        layout(contentPlaceable.width, contentPlaceable.height) {
            contentPlaceable.placeRelative(0, 0)

            if (layoutResult != null) {
                val scrollY = scrollState.value
                inlinePlaceables.forEach { (span, placeable) ->
                    val transformedIndex = HyphenOffsetMapper.toVisual(span.start, state)
                        .coerceIn(0, layoutResult.layoutInput.text.length)
                    val boundingBox = layoutResult.getBoundingBox(transformedIndex)

                    val lineTop = boundingBox.top.roundToInt()
                    val lineBottom = boundingBox.bottom.roundToInt()
                    val lineHeight = lineBottom - lineTop
                    val x = boundingBox.left.roundToInt()

                    if (span.style is MarkupStyle.Link) {
                        placeable.placeRelative(x, lineTop - scrollY)
                    } else {
                        val y = lineTop + (lineHeight - placeable.height) / 2 - scrollY
                        placeable.placeRelative(x, y)
                    }
                }
            }
        }
    }
}