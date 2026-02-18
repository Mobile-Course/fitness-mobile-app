package com.fitness.app

import android.app.Application
import com.fitness.app.data.api.RetrofitClient
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso

class FitnessApp : Application() {
    companion object {
        @Volatile private var appInstance: FitnessApp? = null
        val instance: FitnessApp
            get() = appInstance!!

        fun appContext(): android.content.Context? = appInstance?.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        appInstance = this

        // Initialize Picasso with the same OkHttpClient used by Retrofit,
        // which includes auth headers, cookies, and SSL bypass for dev.
        val picasso = Picasso.Builder(this)
            .downloader(OkHttp3Downloader(RetrofitClient.okHttpClient))
            .loggingEnabled(true)
            .build()
        Picasso.setSingletonInstance(picasso)
    }
}
