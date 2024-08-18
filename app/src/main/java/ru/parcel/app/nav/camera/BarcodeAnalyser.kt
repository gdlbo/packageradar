package ru.parcel.app.nav.camera

import android.graphics.ImageFormat
import android.os.Build
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer

class BarcodeAnalyser(
    val onBarcodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val supportedImageFormats = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        listOf(ImageFormat.YUV_420_888, ImageFormat.YUV_422_888, ImageFormat.YUV_444_888)
    } else {
        listOf(ImageFormat.YUV_420_888)
    }

    override fun analyze(image: ImageProxy) {
        try {
            if (image.format in supportedImageFormats) {
                val bytes = image.toByteArray()
                val width = image.width
                val height = image.height

                val rotationDegrees = image.imageInfo.rotationDegrees
                val rotatedBytes = when (rotationDegrees) {
                    0 -> bytes
                    90 -> rotate90(bytes, width, height)
                    180 -> rotate180(bytes, width, height)
                    else -> bytes
                }

                val source = PlanarYUVLuminanceSource(
                    rotatedBytes, width, height, 0, 0, width, height, false
                )

                val binaryBmp = BinaryBitmap(HybridBinarizer(source))

                val result = try {
                    MultiFormatReader().apply {
                        setHints(
                            mapOf(
                                DecodeHintType.POSSIBLE_FORMATS to arrayListOf(
                                    BarcodeFormat.CODE_128,
                                    BarcodeFormat.EAN_13,
                                    BarcodeFormat.EAN_8,
                                    BarcodeFormat.UPC_A,
                                    BarcodeFormat.UPC_E,
                                    BarcodeFormat.QR_CODE
                                )
                            )
                        )
                    }.decode(binaryBmp)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return
                }

                onBarcodeScanned(result.text)
            }
        } finally {
            image.close()
        }
    }

    private fun ImageProxy.toByteArray(): ByteArray {
        return planes.first().buffer.run {
            rewind()
            ByteArray(remaining()).also { get(it) }
        }
    }

    private fun rotate90(data: ByteArray, width: Int, height: Int): ByteArray {
        val rotatedData = ByteArray(data.size)
        for (i in data.indices) {
            val x = i % width
            val y = i / width
            rotatedData[x * height + height - y - 1] = data[i]
        }
        return rotatedData
    }

    private fun rotate180(data: ByteArray, width: Int, height: Int): ByteArray {
        val rotatedData = ByteArray(data.size)
        for (i in data.indices) {
            val x = i % width
            val y = i / width
            rotatedData[(height - y - 1) * width + width - x - 1] = data[i]
        }
        return rotatedData
    }
}