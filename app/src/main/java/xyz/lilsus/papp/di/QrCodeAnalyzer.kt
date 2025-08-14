package xyz.lilsus.papp.di

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.atomic.AtomicBoolean

// ImageAnalysis.Analyzer qr-code detection implementation which configures and instantiates its
// own MlKit BarcodeScanner and sets up public attributes, `qrCodeFlow` and `isProcessingFlag` which
// are used/controlled by consumers of the detected qr-codes.
// This can be passed to `CameraController` `setImageAnalysisAnalyzer` it is important to also set
// `setImageAnalysisResolutionSelector` otherwise the `ImageAnalysis` use case will fall back to
// defaults that are not optimal for qr-code detection (aspect ratio 4:3, resolution 640x480).
// This Analyzer works best with:
// Aspect Ratio: 16:9
// Resolution: 1920x1080 or 1280x720
// Format: YUV_420_888 (imageProxy.format = 35)
// Docs:
// https://developers.google.com/ml-kit/vision/barcode-scanning/android
// https://developer.android.com/media/camera/camerax

class QrCodeAnalyzer : ImageAnalysis.Analyzer {
    companion object {
        const val TAG = "QR_CODE_ANALYZER"
    }

    val qrCodeFlow: MutableSharedFlow<String> = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val isProcessingFlag: AtomicBoolean = AtomicBoolean(false)

    private val scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (isProcessingFlag.get()) {
            imageProxy.close()
            return
        }

        try {
            val image = imageProxy.image
                ?: throw Exception("ImageProxy wrapped something other than an Image")

            val inputImage =
                InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        barcodes[0].rawValue?.let { qrCodeFlow.tryEmit(it) }
                    }
                }
                .addOnFailureListener { error ->
                    Log.e(
                        TAG,
                        "MlKit Barcode Scanner process input image failure (callback)",
                        error
                    )
                }
                .addOnCompleteListener { imageProxy.close() }
        } catch (e: Throwable) {
            Log.e(TAG, "Unexpected error", e)
            imageProxy.close()
        }
    }
}
