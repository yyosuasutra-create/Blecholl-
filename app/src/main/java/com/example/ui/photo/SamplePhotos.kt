package com.example.ui.photo

import android.graphics.*

object SamplePhotos {

    fun createSampleBitmap(type: Int): Bitmap {
        val width = 800
        val height = 800
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (type) {
            0 -> {
                // Cyber Neon Cityscape
                val gradient = LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    intArrayOf(Color.parseColor("#0F172A"), Color.parseColor("#3B0764"), Color.parseColor("#00E5FF")),
                    null, Shader.TileMode.CLAMP
                )
                paint.shader = gradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                // Neon buildings & circles
                paint.shader = null
                paint.color = Color.parseColor("#FF2A85")
                canvas.drawCircle(400f, 300f, 150f, paint)

                paint.color = Color.parseColor("#00E5FF")
                paint.strokeWidth = 8f
                paint.style = Paint.Style.STROKE
                canvas.drawCircle(400f, 300f, 200f, paint)

                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#1E293B")
                canvas.drawRect(100f, 450f, 250f, 800f, paint)
                canvas.drawRect(300f, 350f, 500f, 800f, paint)
                canvas.drawRect(550f, 500f, 700f, 800f, paint)
            }
            1 -> {
                // Studio Portrait Minimalist
                val gradient = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(Color.parseColor("#F1F5F9"), Color.parseColor("#CBD5E1")),
                    null, Shader.TileMode.CLAMP
                )
                paint.shader = gradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                paint.shader = null
                // Face silhouette
                paint.color = Color.parseColor("#475569")
                canvas.drawCircle(400f, 350f, 140f, paint)
                // Glasses
                paint.color = Color.parseColor("#00E5FF")
                paint.strokeWidth = 12f
                paint.style = Paint.Style.STROKE
                canvas.drawCircle(340f, 350f, 40f, paint)
                canvas.drawCircle(460f, 350f, 40f, paint)
                canvas.drawLine(380f, 350f, 420f, 350f, paint)
                // Shoulders
                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#334155")
                canvas.drawOval(150f, 520f, 650f, 900f, paint)
            }
            2 -> {
                // Sunset Nature
                val gradient = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(Color.parseColor("#F59E0B"), Color.parseColor("#EF4444"), Color.parseColor("#7C3AED")),
                    null, Shader.TileMode.CLAMP
                )
                paint.shader = gradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                paint.shader = null
                // Sun
                paint.color = Color.parseColor("#FEF08A")
                canvas.drawCircle(400f, 400f, 120f, paint)
                // Mountains
                paint.color = Color.parseColor("#1E1B4B")
                val path = Path().apply {
                    moveTo(0f, 800f)
                    lineTo(250f, 480f)
                    lineTo(500f, 650f)
                    lineTo(800f, 400f)
                    lineTo(800f, 800f)
                    close()
                }
                canvas.drawPath(path, paint)
            }
            else -> {
                // Abstract AI Geometry
                val gradient = RadialGradient(
                    400f, 400f, 400f,
                    intArrayOf(Color.parseColor("#A855F7"), Color.parseColor("#0F172A")),
                    null, Shader.TileMode.CLAMP
                )
                paint.shader = gradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                paint.shader = null
                paint.color = Color.parseColor("#00E5FF")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 6f
                for (i in 1..8) {
                    canvas.drawCircle(400f, 400f, i * 45f, paint)
                }
            }
        }
        return bitmap
    }
}
