package com.voicestall.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.voicestall.mobile.data.local.entity.TranscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM transcriptions ORDER BY createdAt DESC")
    fun getAll(): Flow<List<TranscriptionEntity>>

    @Insert
    suspend fun insert(transcription: TranscriptionEntity): Long

    @Delete
    suspend fun delete(transcription: TranscriptionEntity)

    @Query("DELETE FROM transcriptions")
    suspend fun deleteAll()

    @Query("SELECT * FROM transcriptions ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatest(): TranscriptionEntity?
}
