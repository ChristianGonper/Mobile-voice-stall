package com.voicestall.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.voicestall.mobile.data.local.entity.DictionaryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DictionaryDao {

    @Query("SELECT * FROM dictionary_entries ORDER BY id ASC")
    fun getAll(): Flow<List<DictionaryEntryEntity>>

    @Query("SELECT * FROM dictionary_entries WHERE enabled = 1 ORDER BY id ASC")
    suspend fun getEnabled(): List<DictionaryEntryEntity>

    @Insert
    suspend fun insert(entry: DictionaryEntryEntity): Long

    @Update
    suspend fun update(entry: DictionaryEntryEntity)

    @Delete
    suspend fun delete(entry: DictionaryEntryEntity)
}
