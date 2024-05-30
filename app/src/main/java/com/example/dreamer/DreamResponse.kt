package com.example.dreamer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dream_response")
data class DreamResponse(
    @PrimaryKey val dreamContent: String,
    val emotion: String,
    val prompt: String,
    val timestamp: String,
    val sorttime:String
)
