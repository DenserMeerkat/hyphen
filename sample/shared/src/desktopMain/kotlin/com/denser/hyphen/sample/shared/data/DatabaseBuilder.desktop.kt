package com.denser.hyphen.sample.shared.data

import androidx.room3.Room
import androidx.room3.RoomDatabase
import java.io.File

import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual fun initDatabase(context: Any?) { }

actual fun getDatabaseBuilder(): RoomDatabase.Builder<HyphenDatabase> {
    val dbFile = File(System.getProperty("user.home"), "hyphen.db")
    return Room.databaseBuilder<HyphenDatabase>(
        name = dbFile.absolutePath,
        factory = { HyphenDatabaseConstructor.initialize() }
    ).setDriver(BundledSQLiteDriver())
}
