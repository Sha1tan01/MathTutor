package ru.mathtutor.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,          // USER | ASSISTANT | SYSTEM
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val topicId: String,
    val sectionId: String,
    val status: String,        // NOT_STARTED | IN_PROGRESS | DONE
    val practiceCorrect: Int = 0,
    val practiceTotal: Int = 0,
    val lastOpenedAt: Long = System.currentTimeMillis()
)
