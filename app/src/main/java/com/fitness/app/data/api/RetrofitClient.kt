package com.fitness.app.data.api

import java.util.concurrent.TimeUnit
import com.fitness.app.data.model.Author
import com.fitness.app.data.model.AuthorJsonAdapter
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.fitness.app.network.NetworkConfig
import com.fitness.app.data.api.UserProfilesApiService

object RetrofitClient {
        private const val BASE_URL = "https://node86.cs.colman.ac.il/"

        private val loggingInterceptor =
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

        val okHttpClient =
                NetworkConfig.okHttpClient.newBuilder()
                        .addInterceptor(loggingInterceptor)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build()

        private val gson =
                GsonBuilder()
                        .registerTypeAdapter(Author::class.java, AuthorJsonAdapter())
                        .create()

        private val retrofit =
                Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build()

        val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)
        val postsApiService: PostsApiService = retrofit.create(PostsApiService::class.java)
        val userProfilesApiService: UserProfilesApiService =
                retrofit.create(UserProfilesApiService::class.java)
}
