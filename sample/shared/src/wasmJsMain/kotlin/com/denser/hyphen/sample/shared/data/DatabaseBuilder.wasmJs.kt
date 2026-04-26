package com.denser.hyphen.sample.shared.data

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.w3c.dom.Worker

actual fun initDatabase(context: Any?) { }

actual fun getDatabaseBuilder(): RoomDatabase.Builder<HyphenDatabase> {
    return Room.databaseBuilder<HyphenDatabase>(
        name = "hyphen.db",
        factory = { HyphenDatabaseConstructor.initialize() }
    ).setDriver(WebWorkerSQLiteDriver(worker = Worker("sqlite-worker.js")))
        .setQueryCoroutineContext(Dispatchers.Default)
}