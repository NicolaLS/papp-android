package xyz.lilsus.papp.common

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage

// `BarcodeScanner` **should be** configured with:
// `.setBarcodeFormats(Barcode.FORMAT_QR_CODE)`
// And no other formats for best performance.
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
            if (image == null) {
                Log.e(TAG, "ImageProxy wrapped something other than an Image, image is null")
                imageProxy.close()
                return
            }

            val inputImage =
                InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)

            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        Log.d(TAG, "Successfully processed ${barcodes.count()} barcodes")
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