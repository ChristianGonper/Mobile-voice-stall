package com.voicestall.mobile.data.repository

import com.voicestall.mobile.data.local.dao.DictionaryDao
import com.voicestall.mobile.data.local.dao.HistoryDao
import com.voicestall.mobile.data.local.entity.TranscriptionEntity
import com.voicestall.mobile.data.preferences.SettingsDataStore
import com.voicestall.mobile.data.remote.GroqApi
import com.voicestall.mobile.util.DictionaryProcessor
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionRepository @Inject constructor(
    private val groqApi: GroqApi,
    private val historyDao: HistoryDao,
    private val dictionaryDao: DictionaryDao,
    private val settingsDataStore: SettingsDataStore
) {

    val history: Flow<List<TranscriptionEntity>> = historyDao.getAll()

    suspend fun transcribe(audioFilePath: String, durationMs: Long): Result<TranscriptionEntity> {
        return try {
            val apiKey = settingsDataStore.getApiKey()
            if (apiKey.isBlank()) {
                return Result.failure(IllegalStateException("API key not configured. Go to Settings to add your Groq API key."))
            }

            val model = settingsDataStore.getModel()
            val language = settingsDataStore.getLanguage()

            val file = File(audioFilePath)
            val requestFile = file.asRequestBody("audio/mp4".toMediaType())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val modelBody = model.toRequestBody("text/plain".toMediaType())
            val formatBody = "json".toRequestBody("text/plain".toMediaType())
            val languageBody = if (language.isNotBlank()) {
                language.toRequestBody("text/plain".toMediaType())
            } else null

            val response = groqApi.transcribe(
                authorization = "Bearer $apiKey",
                file = filePart,
                model = modelBody,
                language = languageBody,
                responseFormat = formatBody
            )

            val rawText = response.text
            val dictionaryEntries = dictionaryDao.getEnabled()
            val processedText = DictionaryProcessor.apply(rawText, dictionaryEntries)

            val entity = TranscriptionEntity(
                text = processedText,
                rawText = rawText,
                language = language.ifBlank { null },
                model = model,
                durationMs = durationMs
            )
            val id = historyDao.insert(entity)

            Result.success(entity.copy(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTranscription(transcription: TranscriptionEntity) {
        historyDao.delete(transcription)
    }

    suspend fun clearHistory() {
        historyDao.deleteAll()
    }

    suspend fun getLatest(): TranscriptionEntity? {
        return historyDao.getLatest()
    }
}
