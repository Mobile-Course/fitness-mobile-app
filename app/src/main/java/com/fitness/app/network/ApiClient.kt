package com.fitness.app.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.JavaNetCookieJar
import java.net.CookieManager
import java.net.CookiePolicy
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object ApiClient {
    private const val BASE_URL = "https://node86.cs.colman.ac.il"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val cookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    }

    private val unsafeTrustManager: X509TrustManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    private val unsafeSslContext: SSLContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf<TrustManager>(unsafeTrustManager), SecureRandom())
    }

    private val client = OkHttpClient.Builder()
        .cookieJar(JavaNetCookieJar(cookieManager))
        // WARNING: This bypasses TLS verification. Use only for development.
        .sslSocketFactory(unsafeSslContext.socketFactory, unsafeTrustManager)
        .hostnameVerifier { hostname, _ -> hostname == "node86.cs.colman.ac.il" }
        .build()

    fun post(path: String, jsonBody: String): okhttp3.Response {
        val body = jsonBody.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(BASE_URL + path)
            .post(body)
            .build()
        return client.newCall(request).execute()
    }
}
