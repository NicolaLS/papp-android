package xyz.lilsus.papp.common

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage

// NOTE: Barcode Scanner should be configured for QR only
class InvoiceAnalyzer(
    val barcodeScanner: BarcodeScanner,
    val invoiceParser: InvoiceParser
) :
    ImageAnalysis.Analyzer {
    companion object {
        const val TAG = "INVOICE_ANALYZER"
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        try {
            val image = imageProxy.image
                ?: throw Exception("ImageProxy wrapped something other than an Image")

            val inputImage =
                InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)

            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        Log.i(TAG, "Got non-empty barcodes")
                        Log.i(TAG, "Parsing ${barcodes[0].rawValue}")
                        invoiceParser.parse(barcodes[0].rawValue)
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