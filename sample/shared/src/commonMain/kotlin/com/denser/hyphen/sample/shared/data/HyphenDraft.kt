package com.denser.hyphen.sample.shared.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "hyphen_draft")
data class HyphenDraft(
    @PrimaryKey val id: Int = 0,
    val text: String
)
