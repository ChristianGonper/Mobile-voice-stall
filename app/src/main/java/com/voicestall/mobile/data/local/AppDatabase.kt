package com.voicestall.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.voicestall.mobile.data.local.dao.DictionaryDao
import com.voicestall.mobile.data.local.dao.HistoryDao
import com.voicestall.mobile.data.local.entity.DictionaryEntryEntity
import com.voicestall.mobile.data.local.entity.TranscriptionEntity

@Database(
    entities = [TranscriptionEntity::class, DictionaryEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun dictionaryDao(): DictionaryDao
}
