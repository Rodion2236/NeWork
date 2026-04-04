package ru.netology.nework

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NeWorkApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val mapKitKey = BuildConfig.YANDEX_MAPKIT_KEY
        if (mapKitKey.isNotBlank()) {
            MapKitFactory.setApiKey(mapKitKey)
            MapKitFactory.initialize(this)
        }
    }
}