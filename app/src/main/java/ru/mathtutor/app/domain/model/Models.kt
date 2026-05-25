package ru.mathtutor.app.domain.model

// ── Sections & Topics ─────────────────────────────────────────────────────────

data class Section(
    val id: String,
    val title: String,
    val icon: String,          // Unicode math symbol
    val description: String,
    val topicCount: Int,
    val colorTag: String       // "blue" | "teal" | "purple" | "green" | "orange" | "red"
)

data class Topic(
    val id: String,
    val sectionId: String,
    val title: String,
    val orderIndex: Int,
    val theory: String,        // Markdown / LaTeX
    val examples: List<Example>,
    val practiceItems: List<PracticeItem>
)

data class Example(
    val id: String,
    val title: String,
    val steps: List<ExampleStep>
)

data class ExampleStep(
    val stepNumber: Int,
    val description: String,
    val formula: String?
)

// ── Practice ──────────────────────────────────────────────────────────────────

data class PracticeItem(
    val id: String,
    val topicId: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

// ── Progress ──────────────────────────────────────────────────────────────────

data class UserProgress(
    val topicId: String,
    val sectionId: String,
    val status: TopicStatus,
    val practiceCorrect: Int,
    val practiceTotal: Int,
    val lastOpenedAt: Long
)

enum class TopicStatus { NOT_STARTED, IN_PROGRESS, DONE }

data class OverallProgress(
    val completedTopics: Int,
    val totalTopics: Int,
    val lastTopicId: String?,
    val lastTopicTitle: String?,
    val lastSectionTitle: String?
)

// ── Chat ──────────────────────────────────────────────────────────────────────

data class ChatMessage(
    val id: Long = 0,
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageRole { USER, ASSISTANT, SYSTEM }

data class ChatContext(
    val topicId: String?,
    val topicTitle: String?,
    val sectionTitle: String?,
    val contextContent: String? = null  // текст конкретного примера или задания
) {
    val hasContext: Boolean get() = topicId != null

    fun buildSystemPrompt(): String {
        val base = if (hasContext) {
            "Ты — репетитор по высшей математике. Сейчас студент изучает тему «$topicTitle» " +
            "(раздел «$sectionTitle»). Отвечай кратко, используй LaTeX для формул (\$...\$). " +
            "Будь дружелюбным наставником, а не лектором."
        } else {
            "Ты — репетитор по высшей математике. Помогай студенту разобраться с любыми " +
            "вопросами по курсу высшей математики. Используй LaTeX для формул (\$...\$)."
        }
        return if (contextContent != null) {
            "$base\n\nСтудент столкнулся с затруднением при решении следующего задания:\n" +
            "$contextContent\n" +
            "Объясни решение пошагово, не давая сразу готовый ответ — веди студента к нему вопросами."
        } else {
            base
        }
    }
}
