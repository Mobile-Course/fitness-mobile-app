package com.fitness.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.fitness.app.data.api.RetrofitClient

class FitnessApp : Application(), ImageLoaderFactory {
    companion object {
        @Volatile private var appInstance: FitnessApp? = null
        fun appContext(): android.content.Context? = appInstance?.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        appInstance = this
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this).okHttpClient(RetrofitClient.okHttpClient).build()
    }
}
