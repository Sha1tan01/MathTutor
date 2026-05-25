package ru.mathtutor.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// OpenAI Chat Completions request/response
data class ChatRequest(
    val model: String = "deepseek-reasoner",
    val messages: List<MessageDto>,
    @SerializedName("max_tokens") val maxTokens: Int = 1024,
    val temperature: Double = 0.7
)

data class MessageDto(
    val role: String,
    val content: String
)

data class ChatResponse(
    val id: String,
    val choices: List<Choice>
)

data class Choice(
    val message: MessageDto,
    @SerializedName("finish_reason") val finishReason: String
)
