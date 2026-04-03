package com.voicestall.mobile.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val API_KEY = stringPreferencesKey("groq_api_key")
        private val MODEL = stringPreferencesKey("selected_model")
        private val LANGUAGE = stringPreferencesKey("language")
        private val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")

        const val DEFAULT_MODEL = "whisper-large-v3-turbo"

        val AVAILABLE_MODELS = listOf(
            "whisper-large-v3-turbo",
            "whisper-large-v3",
            "distil-whisper-large-v3-en"
        )
    }

    val apiKey: Flow<String> = context.dataStore.data.map { it[API_KEY] ?: "" }
    val model: Flow<String> = context.dataStore.data.map { it[MODEL] ?: DEFAULT_MODEL }
    val language: Flow<String> = context.dataStore.data.map { it[LANGUAGE] ?: "" }
    val overlayEnabled: Flow<Boolean> = context.dataStore.data.map { it[OVERLAY_ENABLED] ?: false }

    suspend fun getApiKey(): String = context.dataStore.data.first()[API_KEY] ?: ""
    suspend fun getModel(): String = context.dataStore.data.first()[MODEL] ?: DEFAULT_MODEL
    suspend fun getLanguage(): String = context.dataStore.data.first()[LANGUAGE] ?: ""

    suspend fun setApiKey(key: String) {
        context.dataStore.edit { it[API_KEY] = key }
    }

    suspend fun setModel(model: String) {
        context.dataStore.edit { it[MODEL] = model }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[LANGUAGE] = language }
    }

    suspend fun setOverlayEnabled(enabled: Boolean) {
        context.dataStore.edit { it[OVERLAY_ENABLED] = enabled }
    }
}
