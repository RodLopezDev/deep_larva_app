package com.odrigo.recognitionappkt.drivers

import android.graphics.Matrix
import android.view.ScaleGestureDetector
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ImageScaler(app: AppCompatActivity, image: ImageView) {
    lateinit var img: ImageView


    private var scaleFactor = 1.0f
    private var matrix = Matrix()

    init {
        img = image

        var scaleGestureDetector = ScaleGestureDetector(app, ScaleListener())

        img.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            true
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // Actualizar la escala
            scaleFactor *= detector.scaleFactor

            // Limitar la escala mínima y máxima si es necesario
            scaleFactor = scaleFactor.coerceIn(0.1f, 5.0f)

            // Aplicar la escala y la matriz a la imagen
            matrix.setScale(scaleFactor, scaleFactor)
            img.imageMatrix = matrix

            return true
        }
    }
}