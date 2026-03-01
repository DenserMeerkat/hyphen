package com.denser.hyphen.model

data class MarkupStyleRange(
    val style: MarkupStyle,
    val start: Int,
    val end: Int
) {
    init {
        require(start <= end) { "Invalid range: start ($start) > end ($end)" }
        require(start >= 0) { "Invalid range: start cannot be negative" }
    }
}