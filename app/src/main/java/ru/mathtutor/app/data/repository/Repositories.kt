package ru.mathtutor.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.mathtutor.app.BuildConfig
import ru.mathtutor.app.data.assets.ContentLoader
import ru.mathtutor.app.data.local.dao.ChatMessageDao
import ru.mathtutor.app.data.local.dao.UserProgressDao
import ru.mathtutor.app.data.local.entity.ChatMessageEntity
import ru.mathtutor.app.data.local.entity.UserProgressEntity
import ru.mathtutor.app.data.remote.api.OpenAiApi
import ru.mathtutor.app.data.remote.dto.ChatRequest
import ru.mathtutor.app.data.remote.dto.MessageDto
import ru.mathtutor.app.domain.model.*
import ru.mathtutor.app.domain.repository.*
import javax.inject.Inject
import javax.inject.Singleton

// ── Content ───────────────────────────────────────────────────────────────────

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val loader: ContentLoader
) : ContentRepository {

    override fun getSections(): Flow<List<Section>> =
        kotlinx.coroutines.flow.flow { emit(loader.getSections()) }

    override fun getTopics(sectionId: String): Flow<List<Topic>> =
        kotlinx.coroutines.flow.flow { emit(loader.getTopics(sectionId)) }

    override suspend fun getTopic(topicId: String): Topic? =
        loader.getTopic(topicId)
}

// ── Progress ──────────────────────────────────────────────────────────────────

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val dao: UserProgressDao,
    private val dataStore: DataStore<Preferences>,
    private val contentLoader: ContentLoader
) : ProgressRepository {

    companion object {
        private val KEY_LAST_TOPIC_ID    = stringPreferencesKey("last_topic_id")
        private val KEY_LAST_TOPIC_TITLE = stringPreferencesKey("last_topic_title")
        private val KEY_LAST_SEC_TITLE   = stringPreferencesKey("last_section_title")
    }

    override fun getAllProgress(): Flow<List<UserProgress>> =
        dao.getAllProgress().map { list -> list.map { it.toDomain() } }

    override fun getProgress(topicId: String): Flow<UserProgress?> =
        dao.getProgress(topicId).map { it?.toDomain() }

    override suspend fun saveProgress(progress: UserProgress) =
        dao.upsert(progress.toEntity())

    override suspend fun getOverallProgress(): OverallProgress {
        val prefs = dataStore.data.first()
        val allTopicCount = contentLoader.getSections().sumOf { it.topicCount }
        val doneCount = dao.countDone()
        return OverallProgress(
            completedTopics = doneCount,
            totalTopics = allTopicCount,
            lastTopicId    = prefs[KEY_LAST_TOPIC_ID],
            lastTopicTitle = prefs[KEY_LAST_TOPIC_TITLE],
            lastSectionTitle = prefs[KEY_LAST_SEC_TITLE]
        )
    }

    override suspend fun setLastTopic(topicId: String, topicTitle: String, sectionTitle: String) {
        dataStore.edit { prefs ->
            prefs[KEY_LAST_TOPIC_ID]    = topicId
            prefs[KEY_LAST_TOPIC_TITLE] = topicTitle
            prefs[KEY_LAST_SEC_TITLE]   = sectionTitle
        }
    }

    private fun UserProgressEntity.toDomain() = UserProgress(
        topicId, sectionId, TopicStatus.valueOf(status),
        practiceCorrect, practiceTotal, lastOpenedAt
    )

    private fun UserProgress.toEntity() = UserProgressEntity(
        topicId, sectionId, status.name,
        practiceCorrect, practiceTotal, lastOpenedAt
    )
}

// ── Chat ──────────────────────────────────────────────────────────────────────

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val dao: ChatMessageDao
) : ChatRepository {

    override fun getMessages(): Flow<List<ChatMessage>> =
        dao.getAllMessages().map { list -> list.map { it.toDomain() } }

    override suspend fun insertMessage(message: ChatMessage): Long =
        dao.insert(message.toEntity())

    override suspend fun clearHistory() = dao.deleteAll()

    private fun ChatMessageEntity.toDomain() = ChatMessage(
        id, MessageRole.valueOf(role), content, timestamp
    )

    private fun ChatMessage.toEntity() = ChatMessageEntity(
        id, role.name, content, timestamp
    )
}

// ── AI ────────────────────────────────────────────────────────────────────────

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val api: OpenAiApi
) : AiRepository {

    override suspend fun sendMessage(
        messages: List<ChatMessage>,
        context: ChatContext
    ): Result<String> = try {
        if (BuildConfig.AI_API_KEY.isBlank()) {
             Result.failure(Exception(
                "API-ключ не задан. Добавьте AI_API_KEY в local.properties"
            ))
        } else {

        val dtos = mutableListOf<MessageDto>()

        // System prompt with topic context
        dtos.add(MessageDto("system", context.buildSystemPrompt()))

        // Conversation history (last 20 messages to stay within token limit)
        dtos.addAll(
            messages.takeLast(20)
                .filter { it.role != MessageRole.SYSTEM }
                .map { MessageDto(it.role.name.lowercase(), it.content) }
        )

        val response = api.chatCompletions(
            authorization = "Bearer ${BuildConfig.AI_API_KEY}",
            request = ChatRequest(messages = dtos)
        )

        if (response.isSuccessful) {
            val text = response.body()?.choices?.firstOrNull()?.message?.content
                ?: "Не удалось получить ответ"
            Result.success(text)
        } else {
            Result.failure(Exception("API error ${response.code()}: ${response.message()}"))
        }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
