package com.voicestall.mobile.data.repository

import com.voicestall.mobile.data.local.dao.DictionaryDao
import com.voicestall.mobile.data.local.entity.DictionaryEntryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DictionaryRepository @Inject constructor(
    private val dictionaryDao: DictionaryDao
) {

    val entries: Flow<List<DictionaryEntryEntity>> = dictionaryDao.getAll()

    suspend fun addEntry(pattern: String, replacement: String): Long {
        return dictionaryDao.insert(
            DictionaryEntryEntity(pattern = pattern, replacement = replacement)
        )
    }

    suspend fun updateEntry(entry: DictionaryEntryEntity) {
        dictionaryDao.update(entry)
    }

    suspend fun deleteEntry(entry: DictionaryEntryEntity) {
        dictionaryDao.delete(entry)
    }
}
