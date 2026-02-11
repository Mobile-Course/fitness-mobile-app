package com.fitness.app.network

import java.net.CookieManager
import java.net.CookiePolicy
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import com.fitness.app.auth.UserSession
import java.net.URI

object NetworkConfig {
    const val BASE_URL = "https://node86.cs.colman.ac.il"

    val cookieManager: CookieManager =
        CookieManager().apply { setCookiePolicy(CookiePolicy.ACCEPT_ALL) }

    private val unsafeTrustManager: X509TrustManager =
        object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }

    private val unsafeSslContext: SSLContext =
        SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(unsafeTrustManager), SecureRandom())
        }

    val okHttpClient: OkHttpClient =
        OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                val token = UserSession.getAccessToken()?.trim()
                if (!token.isNullOrBlank()) {
                    val headerValue =
                        if (token.startsWith("Bearer ")) token else "Bearer $token"
                    requestBuilder.addHeader("Authorization", headerValue)
                    requestBuilder.addHeader("Authentication", headerValue)
                    // Some backends read JWT only from cookies.
                    val cookieValue = headerValue.removePrefix("Bearer ").trim()
                    requestBuilder.addHeader("Cookie", "Authentication=$cookieValue")
                } else {
                    val cookieToken = getAuthCookieValue()
                    if (!cookieToken.isNullOrBlank()) {
                        val headerValue =
                            if (cookieToken.startsWith("Bearer ")) cookieToken
                            else "Bearer $cookieToken"
                        requestBuilder.addHeader("Authorization", headerValue)
                        requestBuilder.addHeader("Authentication", headerValue)
                        val cookieValue = headerValue.removePrefix("Bearer ").trim()
                        requestBuilder.addHeader("Cookie", "Authentication=$cookieValue")
                    }
                }
                chain.proceed(requestBuilder.build())
            }
            // WARNING: This bypasses TLS verification. Use only for development.
            .sslSocketFactory(unsafeSslContext.socketFactory, unsafeTrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()

    fun getAuthCookieValue(): String? {
        return try {
            val uri = URI(BASE_URL)
            val cookies = cookieManager.cookieStore.get(uri)
            cookies.firstOrNull {
                it.name.equals("Authentication", ignoreCase = true) ||
                    it.name.equals("Authorization", ignoreCase = true)
            }?.value
        } catch (e: Exception) {
            null
        }
    }

    fun dumpCookies(): String {
        return try {
            val cookies = cookieManager.cookieStore.cookies
            if (cookies.isEmpty()) {
                "no_cookies"
            } else {
                cookies.joinToString(" | ") { cookie ->
                    "${cookie.name}=${cookie.value}; domain=${cookie.domain}; path=${cookie.path}"
                }
            }
        } catch (e: Exception) {
            "cookie_dump_error=${e.message}"
        }
    }
}
