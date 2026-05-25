package ru.mathtutor.app.presentation.theory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.mathtutor.app.domain.model.Section
import ru.mathtutor.app.domain.model.Topic
import ru.mathtutor.app.domain.model.TopicStatus
import ru.mathtutor.app.domain.model.UserProgress
import ru.mathtutor.app.domain.usecase.GetSectionsUseCase
import ru.mathtutor.app.domain.usecase.GetTopicsUseCase
import javax.inject.Inject

// ── Sections screen ───────────────────────────────────────────────────────────

data class SectionsUiState(
    val isLoading: Boolean = true,
    val sections: List<Section> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SectionsViewModel @Inject constructor(
    private val getSections: GetSectionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SectionsUiState())
    val uiState: StateFlow<SectionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getSections().collect { sections ->
                _uiState.value = SectionsUiState(isLoading = false, sections = sections)
            }
        }
    }
}

// ── Topics (subsections) screen ───────────────────────────────────────────────

data class TopicsUiState(
    val isLoading: Boolean = true,
    val topics: List<Topic> = emptyList(),
    val progressMap: Map<String, UserProgress> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class TopicsViewModel @Inject constructor(
    private val getTopics: GetTopicsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicsUiState())
    val uiState: StateFlow<TopicsUiState> = _uiState.asStateFlow()

    fun loadTopics(sectionId: String) {
        viewModelScope.launch {
            getTopics(sectionId).collect { topics ->
                _uiState.value = TopicsUiState(isLoading = false, topics = topics)
            }
        }
    }
}
