package com.fitness.app.data.api

import com.fitness.app.data.model.AskCoachRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming

interface CoachApiService {
    @Streaming
    @Headers("Accept: text/event-stream")
    @POST("api/coach/ask-stream")
    suspend fun askStream(@Body request: AskCoachRequest): Response<ResponseBody>
}
