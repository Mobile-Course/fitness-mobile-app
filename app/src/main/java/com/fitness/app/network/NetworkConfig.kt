package com.fitness.app.network

import com.fitness.app.FitnessApp
import com.fitness.app.auth.UserSession
import com.fitness.app.data.local.AppDatabase
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.URI
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

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

    private val refreshLock = Any()

    private val refreshClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .sslSocketFactory(unsafeSslContext.socketFactory, unsafeTrustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val okHttpClient: OkHttpClient =
        OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                applyAuthHeaders(requestBuilder)
                chain.proceed(requestBuilder.build())
            }
            .authenticator { _, response ->
                val path = response.request.url.encodedPath
                if (path.contains("/api/auth/login") || path.contains("/api/auth/refresh")) {
                    return@authenticator null
                }
                if (responseCount(response) >= 2) {
                    return@authenticator null
                }

                val refreshToken =
                    getRefreshCookieValue()?.takeIf { it.isNotBlank() }
                        ?: UserSession.getRefreshToken()?.takeIf { it.isNotBlank() }
                if (refreshToken.isNullOrBlank()) {
                    forceLogout()
                    return@authenticator null
                }

                synchronized(refreshLock) {
                    val originalAuth = response.request.header("Authorization")
                    val latestAuth = buildAuthorizationHeader()
                    if (!latestAuth.isNullOrBlank() && latestAuth != originalAuth) {
                        return@synchronized response.request.newBuilder()
                            .header("Authorization", latestAuth)
                            .header("Authentication", latestAuth)
                            .header(
                                "Cookie",
                                "Authentication=${latestAuth.removePrefix("Bearer ").trim()}"
                            )
                            .build()
                    }

                    val refreshed = refreshTokensBlocking(refreshToken)
                    if (!refreshed) {
                        forceLogout()
                        return@synchronized null
                    }

                    val refreshedAuth = buildAuthorizationHeader() ?: return@synchronized null
                    response.request.newBuilder()
                        .header("Authorization", refreshedAuth)
                        .header("Authentication", refreshedAuth)
                        .header(
                            "Cookie",
                            "Authentication=${refreshedAuth.removePrefix("Bearer ").trim()}"
                        )
                        .build()
                }
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

    fun getRefreshCookieValue(): String? {
        return try {
            val uri = URI(BASE_URL)
            val cookies = cookieManager.cookieStore.get(uri)
            cookies.firstOrNull { it.name.equals("Refresh", ignoreCase = true) }?.value
        } catch (e: Exception) {
            null
        }
    }

    private fun applyAuthHeaders(requestBuilder: Request.Builder) {
        val headerValue = buildAuthorizationHeader() ?: return
        requestBuilder.header("Authorization", headerValue)
        requestBuilder.header("Authentication", headerValue)
        val cookieValue = headerValue.removePrefix("Bearer ").trim()
        if (cookieValue.isNotBlank()) {
            requestBuilder.header("Cookie", "Authentication=$cookieValue")
        }
    }

    private fun buildAuthorizationHeader(): String? {
        val token = UserSession.getAccessToken()?.trim()
        if (!token.isNullOrBlank()) {
            return if (token.startsWith("Bearer ")) token else "Bearer $token"
        }
        val cookieToken = getAuthCookieValue()?.trim()
        if (!cookieToken.isNullOrBlank()) {
            return if (cookieToken.startsWith("Bearer ")) cookieToken else "Bearer $cookieToken"
        }
        return null
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var current = response.priorResponse
        while (current != null) {
            count++
            current = current.priorResponse
        }
        return count
    }

    private fun refreshTokensBlocking(refreshToken: String): Boolean {
        return try {
            val request =
                Request.Builder()
                    .url("$BASE_URL/api/auth/refresh")
                    .get()
                    .header("Cookie", "Refresh=$refreshToken")
                    .build()
            refreshClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return false

                val accessFromCookie = getAuthCookieValue()?.takeIf { it.isNotBlank() }
                val accessHeader =
                    accessFromCookie?.let {
                        if (it.startsWith("Bearer ")) it else "Bearer $it"
                    }
                if (!accessHeader.isNullOrBlank()) {
                    UserSession.setUser(accessToken = accessHeader)
                }

                val newRefresh = getRefreshCookieValue()?.takeIf { it.isNotBlank() } ?: refreshToken
                UserSession.setUser(refreshToken = newRefresh)
                persistRefreshToken(newRefresh)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun persistRefreshToken(refreshToken: String?) {
        if (refreshToken.isNullOrBlank()) return
        val context = FitnessApp.appContext() ?: return
        try {
            kotlinx.coroutines.runBlocking {
                val dao = AppDatabase.getInstance(context).userDao()
                val current = dao.getUser()
                if (current != null && current.refreshToken != refreshToken) {
                    dao.upsert(current.copy(refreshToken = refreshToken))
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun forceLogout() {
        val context = FitnessApp.appContext()
        try {
            cookieManager.cookieStore.removeAll()
        } catch (_: Exception) {
        }

        if (context != null) {
            try {
                kotlinx.coroutines.runBlocking {
                    AppDatabase.getInstance(context).userDao().clear()
                }
            } catch (_: Exception) {
            }
            UserSession.clearPersistedAccessToken(context)
        }
        UserSession.clear()
        UserSession.notifyForcedLogout()
    }
}
