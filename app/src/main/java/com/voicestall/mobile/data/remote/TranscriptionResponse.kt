package com.voicestall.mobile.data.remote

import com.google.gson.annotations.SerializedName

data class TranscriptionResponse(
    @SerializedName("text") val text: String
)
