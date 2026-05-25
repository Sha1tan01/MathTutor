package ru.mathtutor.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.mathtutor.app.domain.model.OverallProgress
import ru.mathtutor.app.domain.model.UserProgress
import ru.mathtutor.app.domain.usecase.GetOverallProgressUseCase
import ru.mathtutor.app.domain.repository.ProgressRepository
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val progress: OverallProgress? = null,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getOverallProgress: GetOverallProgressUseCase,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val progressListState: StateFlow<List<UserProgress>> =
        progressRepository.getAllProgress()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { loadProgress() }

    fun loadProgress() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            try {
                val progress = getOverallProgress()
                _uiState.value = HomeUiState(isLoading = false, progress = progress)
            } catch (e: Exception) {
                _uiState.value = HomeUiState(isLoading = false, error = e.message)
            }
        }
    }
}
