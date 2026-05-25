package ru.mathtutor.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.mathtutor.app.domain.model.*
import ru.mathtutor.app.domain.repository.*
import javax.inject.Inject

// ── Content ───────────────────────────────────────────────────────────────────

class GetSectionsUseCase @Inject constructor(
    private val repo: ContentRepository
) {
    operator fun invoke(): Flow<List<Section>> = repo.getSections()
}

class GetTopicsUseCase @Inject constructor(
    private val repo: ContentRepository
) {
    operator fun invoke(sectionId: String): Flow<List<Topic>> = repo.getTopics(sectionId)
}

class GetTopicUseCase @Inject constructor(
    private val repo: ContentRepository
) {
    suspend operator fun invoke(topicId: String): Topic? = repo.getTopic(topicId)
}

// ── Progress ──────────────────────────────────────────────────────────────────

class GetOverallProgressUseCase @Inject constructor(
    private val repo: ProgressRepository
) {
    suspend operator fun invoke(): OverallProgress = repo.getOverallProgress()
}

class SaveProgressUseCase @Inject constructor(
    private val repo: ProgressRepository
) {
    suspend operator fun invoke(progress: UserProgress) = repo.saveProgress(progress)
}

class GetTopicProgressUseCase @Inject constructor(
    private val repo: ProgressRepository
) {
    operator fun invoke(topicId: String): Flow<UserProgress?> = repo.getProgress(topicId)
}

class SetLastTopicUseCase @Inject constructor(
    private val repo: ProgressRepository
) {
    suspend operator fun invoke(topicId: String, topicTitle: String, sectionTitle: String) =
        repo.setLastTopic(topicId, topicTitle, sectionTitle)
}

// ── Chat ──────────────────────────────────────────────────────────────────────

class GetChatMessagesUseCase @Inject constructor(
    private val repo: ChatRepository
) {
    operator fun invoke(): Flow<List<ChatMessage>> = repo.getMessages()
}

class SendChatMessageUseCase @Inject constructor(
    private val chatRepo: ChatRepository,
    private val aiRepo: AiRepository
) {
    suspend operator fun invoke(
        userText: String,
        history: List<ChatMessage>,
        context: ChatContext
    ): Result<ChatMessage> {
        // 1. Persist user message
        val userMsg = ChatMessage(role = MessageRole.USER, content = userText)
        val userMsgId = chatRepo.insertMessage(userMsg)

        // 2. Call AI
        val allMessages = history + userMsg.copy(id = userMsgId)
        val result = aiRepo.sendMessage(allMessages, context)

        return result.fold(
            onSuccess = { text ->
                val assistantMsg = ChatMessage(role = MessageRole.ASSISTANT, content = text)
                val assistantMsgId = chatRepo.insertMessage(assistantMsg)
                Result.success(assistantMsg.copy(id = assistantMsgId))
            },
            onFailure = { Result.failure(it) }
        )
    }
}

class ClearChatHistoryUseCase @Inject constructor(
    private val repo: ChatRepository
) {
    suspend operator fun invoke() = repo.clearHistory()
}
