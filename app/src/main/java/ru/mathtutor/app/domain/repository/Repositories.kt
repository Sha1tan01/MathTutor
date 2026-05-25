package ru.mathtutor.app.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.mathtutor.app.domain.model.*

interface ContentRepository {
    fun getSections(): Flow<List<Section>>
    fun getTopics(sectionId: String): Flow<List<Topic>>
    suspend fun getTopic(topicId: String): Topic?
}

interface ProgressRepository {
    fun getAllProgress(): Flow<List<UserProgress>>
    fun getProgress(topicId: String): Flow<UserProgress?>
    suspend fun saveProgress(progress: UserProgress)
    suspend fun getOverallProgress(): OverallProgress
    suspend fun setLastTopic(topicId: String, topicTitle: String, sectionTitle: String)
}

interface ChatRepository {
    fun getMessages(): Flow<List<ChatMessage>>
    suspend fun insertMessage(message: ChatMessage): Long
    suspend fun clearHistory()
}

interface AiRepository {
    suspend fun sendMessage(
        messages: List<ChatMessage>,
        context: ChatContext
    ): Result<String>
}
