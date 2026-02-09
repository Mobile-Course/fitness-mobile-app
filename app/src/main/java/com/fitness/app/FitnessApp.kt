package com.fitness.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.fitness.app.data.api.RetrofitClient

class FitnessApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this).okHttpClient(RetrofitClient.okHttpClient).build()
    }
}
