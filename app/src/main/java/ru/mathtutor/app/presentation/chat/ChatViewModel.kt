package ru.mathtutor.app.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.mathtutor.app.domain.model.ChatContext
import ru.mathtutor.app.domain.model.ChatMessage
import ru.mathtutor.app.domain.usecase.ClearChatHistoryUseCase
import ru.mathtutor.app.domain.usecase.GetChatMessagesUseCase
import ru.mathtutor.app.domain.usecase.SendChatMessageUseCase
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val error: String? = null,
    val context: ChatContext = ChatContext(null, null, null)
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getMessages: GetChatMessagesUseCase,
    private val sendMessage: SendChatMessageUseCase,
    private val clearHistory: ClearChatHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun init(topicId: String?, topicTitle: String?, sectionTitle: String?, contextContent: String? = null) {
        val context = ChatContext(topicId, topicTitle, sectionTitle, contextContent)
        _uiState.value = _uiState.value.copy(context = context)

        viewModelScope.launch {
            getMessages().collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    fun send(text: String) {
        if (text.isBlank()) return
        _uiState.value = _uiState.value.copy(isTyping = true, error = null)

        viewModelScope.launch {
            val result = sendMessage(
                userText = text,
                history = _uiState.value.messages,
                context = _uiState.value.context
            )
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isTyping = false)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isTyping = false,
                        error = "Ошибка: ${e.message}"
                    )
                }
            )
        }
    }

    fun clearChat() {
        viewModelScope.launch { clearHistory() }
    }
}
