package com.fitness.app.data.api

import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
        private const val BASE_URL = "https://node86.cs.colman.ac.il/"

        private val loggingInterceptor =
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

        private val cookieManager =
                java.net.CookieManager().apply { setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL) }

        private val unsafeTrustManager: X509TrustManager =
                object : X509TrustManager {
                        override fun checkClientTrusted(
                                chain: Array<X509Certificate>,
                                authType: String
                        ) = Unit
                        override fun checkServerTrusted(
                                chain: Array<X509Certificate>,
                                authType: String
                        ) = Unit
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }

        private val unsafeSslContext: SSLContext =
                SSLContext.getInstance("TLS").apply {
                        init(null, arrayOf<TrustManager>(unsafeTrustManager), SecureRandom())
                }

        val okHttpClient =
                OkHttpClient.Builder()
                        .cookieJar(okhttp3.JavaNetCookieJar(cookieManager))
                        .addInterceptor(loggingInterceptor)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .sslSocketFactory(unsafeSslContext.socketFactory, unsafeTrustManager)
                        .hostnameVerifier { _, _ -> true }
                        .build()

        private val retrofit =
                Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

        val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)
        val postsApiService: PostsApiService = retrofit.create(PostsApiService::class.java)
}
