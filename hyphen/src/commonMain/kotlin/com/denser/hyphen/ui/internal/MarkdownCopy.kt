package com.denser.hyphen.ui.internal

import androidx.compose.ui.text.TextRange
import com.denser.hyphen.markdown.MarkdownSerializer
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.MarkupStyleRange
import com.denser.hyphen.model.TextRangeWith

/**
 * Returns the Markdown to put on the clipboard in place of [clipText], or `null` if the clip is
 * our own write, was not copied from [selection], or needs no change.
 */
internal fun markdownForCopiedClip(
    clipText: CharSequence?,
    lastMarkdownWrite: String?,
    text: String,
    spans: List<MarkupStyleRange>,
    selection: TextRange,
): String? {
    val clip = clipText?.toString() ?: return null
    if (clip == lastMarkdownWrite || selection.collapsed || selection.max > text.length) return null

    val start = selection.min
    val end = selection.max
    val normalizedClip = clip.filterNot { it == '​' || it == '‎' }.replace(' ', ' ')
    if (normalizedClip != copiedPlainText(text, spans, start, end)) return null

    return MarkdownSerializer.serialize(text, spans, start, end).takeIf { it != clip }
}

/**
 * The text field copies its visual text, where checkbox prefixes are two spaces and blockquote
 * prefixes are hidden (see [applyMarkdownStyles]).
 */
internal fun copiedPlainText(
    text: String,
    spans: List<MarkupStyleRange>,
    start: Int,
    end: Int,
): String {
    val prefixes = spans.mapNotNull { span ->
        when (span.style) {
            is MarkupStyle.CheckboxUnchecked, is MarkupStyle.CheckboxChecked ->
                TextRangeWith(span.start, span.start + 6, "  ")

            is MarkupStyle.Blockquote -> {
                val next = text.getOrNull(span.start + 1)
                TextRangeWith(span.start, span.start + if (next == ' ' || next == ' ') 2 else 1, "")
            }

            else -> null
        }
    }.sortedBy { it.start }

    val builder = StringBuilder(end - start)
    var index = start
    for (prefix in prefixes) {
        if (prefix.end <= index) continue
        if (prefix.start >= end) break
        if (prefix.start > index) builder.appendRange(text, index, prefix.start)
        builder.append(prefix.value)
        index = prefix.end
    }
    if (index < end) builder.appendRange(text, index, end)
    return builder.toString()
}
