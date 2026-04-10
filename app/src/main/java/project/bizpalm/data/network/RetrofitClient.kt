package project.bizpalm.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.groq.com/openai/" // Example: using Groq or any OpenAI compatible API

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val chatApiService: ChatApiService by lazy {
        retrofit.create(ChatApiService::class.java)
    }
}
