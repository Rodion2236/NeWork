package ru.netology.nework

import android.app.Application
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.io.InputStream
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class NeWorkApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        setupGlideOkHttpClient()

        val mapKitKey = BuildConfig.YANDEX_MAPKIT_KEY
        if (mapKitKey.isNotBlank()) {
            MapKitFactory.setApiKey(mapKitKey)
            MapKitFactory.initialize(this)
        }
    }

    private fun setupGlideOkHttpClient() {
        val glideOkHttpClient = OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .protocols(listOf(Protocol.HTTP_1_1))
            .build()

        val glide = Glide.get(this)
        glide.registry.prepend(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(glideOkHttpClient)
        )
    }
}