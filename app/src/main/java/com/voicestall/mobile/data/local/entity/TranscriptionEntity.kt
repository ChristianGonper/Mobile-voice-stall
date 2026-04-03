package com.voicestall.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transcriptions")
data class TranscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val rawText: String,
    val language: String?,
    val model: String,
    val durationMs: Long,
    val createdAt: Long = System.currentTimeMillis()
)
