package com.example.dreamer
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DreamResponseDao {
    @Query("SELECT * FROM dream_response ORDER BY timestamp DESC")
    suspend fun getAllResponses(): List<DreamResponse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg responses: DreamResponse)

    @Query("DELETE FROM dream_response")
    suspend fun deleteAll()
}
