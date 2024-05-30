package com.example.dreamer

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DreamResponse::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dreamResponseDao(): DreamResponseDao
}
