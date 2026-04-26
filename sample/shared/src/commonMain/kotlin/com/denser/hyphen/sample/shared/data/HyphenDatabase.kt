package com.denser.hyphen.sample.shared.data

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

@Database(entities = [HyphenDraft::class], version = 1)
@ConstructedBy(HyphenDatabaseConstructor::class)
abstract class HyphenDatabase : RoomDatabase() {
    abstract fun hyphenDao(): HyphenDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object HyphenDatabaseConstructor : RoomDatabaseConstructor<HyphenDatabase> {
    override fun initialize(): HyphenDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<HyphenDatabase>
): HyphenDatabase {
    return builder
        .fallbackToDestructiveMigration(true)
        .build()
}
