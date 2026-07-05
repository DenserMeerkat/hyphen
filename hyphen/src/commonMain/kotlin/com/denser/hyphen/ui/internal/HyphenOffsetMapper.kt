package com.denser.hyphen.ui.internal

import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.StyleSets
import com.denser.hyphen.state.HyphenTextState

internal object HyphenOffsetMapper {
    fun toVisual(originalOffset: Int, state: HyphenTextState): Int {
        val hasHeadingAnchor = state.spans.any { it.start == 0 && it.style in StyleSets.allHeadings }
        var visualOffset = originalOffset + (if (hasHeadingAnchor) 1 else 0)

        val checkboxes = state.spans
            .filter { it.style is MarkupStyle.CheckboxUnchecked || it.style is MarkupStyle.CheckboxChecked }
            .sortedBy { it.start }

        for (cb in checkboxes) {
            if (cb.start < originalOffset) {
                val charsBeforePrefix = (originalOffset - cb.start).coerceAtMost(6)
                if (charsBeforePrefix >= 6) {
                    visualOffset -= 4
                } else if (charsBeforePrefix >= 3) {
                    visualOffset -= (charsBeforePrefix - 1)
                } else {
                    visualOffset -= charsBeforePrefix
                }
            }
        }

        val blockquotes = state.spans
            .filter { it.style is MarkupStyle.Blockquote }
            .sortedBy { it.start }

        for (bq in blockquotes) {
            if (bq.start < originalOffset) {
                val sourceText = state.textFieldState.text.toString()
                val prefixLen = if (bq.start + 1 < sourceText.length && 
                    (sourceText[bq.start + 1] == ' ' || sourceText[bq.start + 1] == '\u00A0')) 2 else 1
                
                val charsBeforePrefix = (originalOffset - bq.start).coerceAtMost(prefixLen)
                visualOffset -= (charsBeforePrefix - 1).coerceAtLeast(0)
            }
        }

        return visualOffset
    }
}