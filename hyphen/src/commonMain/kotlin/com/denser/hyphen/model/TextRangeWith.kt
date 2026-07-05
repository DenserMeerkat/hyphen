package com.denser.hyphen.model

data class TextRangeWith<T>(
    val start: Int,
    val end: Int,
    val value: T
) {
    init {
        require(start <= end) { "Invalid range: start ($start) > end ($end)" }
        require(start >= 0) { "Invalid range: start cannot be negative" }
    }
}
