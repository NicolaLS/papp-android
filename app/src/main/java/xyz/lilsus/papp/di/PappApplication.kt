package xyz.lilsus.papp.di

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraXConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class PappApplication : Application(), CameraXConfig.Provider {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    val appDependencies by lazy {
        AppDependencies(applicationContext, applicationScope)
    }

    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            // Ignore other cameras, which can reduce startup latency for the back camera.
            .setAvailableCamerasLimiter(CameraSelector.DEFAULT_BACK_CAMERA)
            .build()
    }
}