package ru.mathtutor.app.presentation.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.mathtutor.app.domain.model.*
import ru.mathtutor.app.domain.usecase.*
import javax.inject.Inject

data class TopicUiState(
    val isLoading: Boolean = true,
    val topic: Topic? = null,
    val progress: UserProgress? = null,
    val selectedTabIndex: Int = 0,
    val answeredPractice: Map<String, Int> = emptyMap(), // practiceId -> selectedIndex
    val error: String? = null
)

@HiltViewModel
class TopicViewModel @Inject constructor(
    private val getTopicUseCase: GetTopicUseCase,
    private val getTopicProgressUseCase: GetTopicProgressUseCase,
    private val saveProgressUseCase: SaveProgressUseCase,
    private val setLastTopicUseCase: SetLastTopicUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicUiState())
    val uiState: StateFlow<TopicUiState> = _uiState.asStateFlow()

    fun loadTopic(topicId: String, topicTitle: String, sectionTitle: String) {
        viewModelScope.launch {
            try {
                val topic = getTopicUseCase(topicId)

                if (topic == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Тема не найдена: $topicId"
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(isLoading = false, topic = topic)

                // Record last opened
                setLastTopicUseCase(topicId, topicTitle, sectionTitle)

                // Mark as at least in-progress
                val current = _uiState.value.progress
                if (current == null || current.status == TopicStatus.NOT_STARTED) {
                    val progress = UserProgress(
                        topicId = topicId,
                        sectionId = topic.sectionId,
                        status = TopicStatus.IN_PROGRESS,
                        practiceCorrect = 0,
                        practiceTotal = topic.practiceItems.size,
                        lastOpenedAt = System.currentTimeMillis()
                    )
                    saveProgressUseCase(progress)
                }

                // Observe progress
                getTopicProgressUseCase(topicId).collect { prog ->
                    _uiState.value = _uiState.value.copy(progress = prog)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTabIndex = index)
    }

    fun answerPractice(practiceId: String, selectedIndex: Int, topicId: String, sectionId: String) {
        val alreadyAnswered = _uiState.value.answeredPractice.containsKey(practiceId)
        if (alreadyAnswered) return

        val newAnswered = _uiState.value.answeredPractice + (practiceId to selectedIndex)
        _uiState.value = _uiState.value.copy(answeredPractice = newAnswered)

        val topic = _uiState.value.topic ?: return
        val correct = newAnswered.entries.count { (id, idx) ->
            topic.practiceItems.find { it.id == id }?.correctIndex == idx
        }
        val total = topic.practiceItems.size
        val allDone = newAnswered.size == total

        viewModelScope.launch {
            val progress = UserProgress(
                topicId = topicId,
                sectionId = sectionId,
                status = if (allDone) TopicStatus.DONE else TopicStatus.IN_PROGRESS,
                practiceCorrect = correct,
                practiceTotal = total,
                lastOpenedAt = System.currentTimeMillis()
            )
            saveProgressUseCase(progress)
        }
    }
}
