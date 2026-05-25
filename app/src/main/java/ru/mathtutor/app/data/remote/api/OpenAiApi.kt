package ru.mathtutor.app.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import ru.mathtutor.app.data.remote.dto.ChatRequest
import ru.mathtutor.app.data.remote.dto.ChatResponse

interface OpenAiApi {
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}
