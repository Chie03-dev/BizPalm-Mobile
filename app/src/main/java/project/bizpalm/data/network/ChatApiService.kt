package project.bizpalm.data.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ChatApiService {
    @POST("v1/chat/completions")
    fun chat(
        @Header("Authorization") auth: String,
        @Body request: ChatRequest
    ): Call<ChatResponse>
}

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
