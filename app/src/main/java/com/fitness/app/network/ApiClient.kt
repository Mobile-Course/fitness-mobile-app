package com.fitness.app.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient

object ApiClient {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val client: OkHttpClient = NetworkConfig.okHttpClient

    fun post(path: String, jsonBody: String): okhttp3.Response {
        val body = jsonBody.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(NetworkConfig.BASE_URL + path)
            .post(body)
            .build()
        return client.newCall(request).execute()
    }
}
