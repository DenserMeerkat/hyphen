package com.denser.hyphen.sample.shared.data

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

private lateinit var appContext: Context

actual fun initDatabase(context: Any?) {
    if (context is Context) {
        appContext = context.applicationContext
    }
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<HyphenDatabase> {
    val dbFile = appContext.getDatabasePath("hyphen.db")

    return Room.databaseBuilder<HyphenDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
        factory = { HyphenDatabaseConstructor.initialize() }
    ).setDriver(BundledSQLiteDriver())
}