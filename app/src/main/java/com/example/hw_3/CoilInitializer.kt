package com.example.hw_3

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory

class CoilInitializer : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        // Используем стандартный ImageLoader без дополнительных настроек
        // чтобы избежать блокировки главного потока при инициализации
        return ImageLoader.Builder(this)
            .build()
    }
}

