package com.denser.hyphen.sample.shared.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query

@Dao
interface HyphenDao {
    @Query("SELECT * FROM hyphen_draft WHERE id = 0")
    suspend fun getDraft(): HyphenDraft?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: HyphenDraft)

    @Query("DELETE FROM hyphen_draft")
    suspend fun clearDraft()
}
