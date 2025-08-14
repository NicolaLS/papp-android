package xyz.lilsus.papp.di

import android.content.Context
import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.view.CameraController.IMAGE_ANALYSIS
import androidx.camera.view.LifecycleCameraController

// Instantiates a CameraX `CameraController` and configures it to be optimized for qr-code detection.
// Optimizations:
// - Set optimal `ResolutionSelector` for image analysis.
// - Disable "Pinch to Zoom"
// - Disable IMAGE_CAPTURE so only Preview and ImageAnalysis is attached

class CameraControllerFactory(val context: Context) {
    fun createController(): LifecycleCameraController {
        var cameraController = LifecycleCameraController(context)
        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(
                    AspectRatio.RATIO_16_9,
                    AspectRatioStrategy.FALLBACK_RULE_AUTO
                )
            )
            .setResolutionStrategy(
                ResolutionStrategy(
                    Size(1920, 1080),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER
                )
            )
            .build()

        // Disable IMAGE_CAPTURE (default)
        cameraController.setEnabledUseCases(IMAGE_ANALYSIS)
        // Disable "Pinch to Zoom" because we implement "Pull down to Zoom" manually
        cameraController.isPinchToZoomEnabled = false
        cameraController.setImageAnalysisResolutionSelector(resolutionSelector)

        return cameraController
    }
}