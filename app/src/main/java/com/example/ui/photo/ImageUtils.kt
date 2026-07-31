package com.example.ui.photo

import android.content.Context
import android.graphics.*
import android.net.Uri
import com.example.data.model.PhotoFilterPreset

object ImageUtils {

    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun applyFilters(
        original: Bitmap,
        brightness: Float = 1.0f,  // 0.5 to 1.5
        contrast: Float = 1.0f,    // 0.5 to 1.5
        saturation: Float = 1.0f,  // 0.0 to 2.0
        vintage: Boolean = false,
        cyber: Boolean = false,
        grayscale: Boolean = false,
        rotationDegrees: Float = 0f
    ): Bitmap {
        var bmp = original

        // Apply rotation if needed
        if (rotationDegrees % 360 != 0f) {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        }

        val width = bmp.width
        val height = bmp.height
        val mutableBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mutableBmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cm = ColorMatrix()

        // 1. Brightness & Contrast
        val cScale = contrast
        val bOffset = (brightness - 1.0f) * 255f
        val cmContrastBrightness = ColorMatrix(
            floatArrayOf(
                cScale, 0f, 0f, 0f, bOffset,
                0f, cScale, 0f, 0f, bOffset,
                0f, 0f, cScale, 0f, bOffset,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(cmContrastBrightness)

        // 2. Saturation
        if (grayscale) {
            cm.setSaturation(0f)
        } else {
            val cmSat = ColorMatrix()
            cmSat.setSaturation(saturation)
            cm.postConcat(cmSat)
        }

        // 3. Special Color Tints
        if (vintage) {
            // Warm sepia vintage matrix
            val vintageCm = ColorMatrix(
                floatArrayOf(
                    0.9f, 0.2f, 0.1f, 0f, 20f,
                    0.2f, 0.8f, 0.1f, 0f, 10f,
                    0.1f, 0.1f, 0.6f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            cm.postConcat(vintageCm)
        } else if (cyber) {
            // Neon Cyan / Magenta cyber tint
            val cyberCm = ColorMatrix(
                floatArrayOf(
                    0.8f, 0.0f, 0.4f, 0f, 30f,
                    0.0f, 1.1f, 0.2f, 0f, 10f,
                    0.3f, 0.1f, 1.2f, 0f, 40f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            cm.postConcat(cyberCm)
        }

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bmp, 0f, 0f, paint)

        return mutableBmp
    }

    // Default sample presets
    val defaultPresets = listOf(
        PhotoFilterPreset("Normal", 1.0f, 1.0f, 1.0f, false, false, false, "Foto Asli Tanpa Filter"),
        PhotoFilterPreset("Cyber Glow", 1.05f, 1.2f, 1.3f, false, true, false, "Gaya Neon Cyan/Magenta Cyberpunk"),
        PhotoFilterPreset("Vintage Warm", 1.0f, 1.1f, 0.9f, true, false, false, "Nuansa Hangat Klasik Retro"),
        PhotoFilterPreset("Hitam Putih", 1.0f, 1.2f, 0.0f, false, false, true, "Monokrom Artistik Kontras Tinggi"),
        PhotoFilterPreset("Pop Vivid", 1.1f, 1.25f, 1.5f, false, false, false, "Warna Terang Cerah Sempurna")
    )
}
