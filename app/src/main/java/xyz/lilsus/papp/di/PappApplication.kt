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
            // Reduce startup latency for the cameras the application uses.
            .setAvailableCamerasLimiter(CameraSelector.DEFAULT_BACK_CAMERA)
            .build()
    }
}