package com.denser.hyphen.sample.shared.data

import androidx.room3.RoomDatabase

expect fun getDatabaseBuilder(): RoomDatabase.Builder<HyphenDatabase>

expect fun initDatabase(context: Any?)
