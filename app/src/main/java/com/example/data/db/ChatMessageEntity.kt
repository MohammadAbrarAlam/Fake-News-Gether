package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelId: String = "gemini-3.5-flash",
    val groundingSourcesJson: String = "",
    val searchQueriesJson: String = "",
    val geoPinsJson: String = "",
    val isError: Boolean = false
)
