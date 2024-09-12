package com.iiap.deeplarva.modules.prediction
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.widget.ImageView
import android.widget.TextView

import android.widget.Toast
import androidx.annotation.RequiresApi
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.time.Duration
import java.time.Instant

import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


import java.io.File
import java.io.FileWriter


class Detect640x640_recorte_imagen_region_interes(private val activity: Context) {

    val paint = Paint()

    lateinit var labels: List<String>

    init {

        paint.setColor(Color.BLUE)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5.0f

        //labels = FileUtil.loadLabels(this, "labels.txt")
        labels = listOf("objeto_interes")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerImagenAutoRecortado(bitmap: Bitmap): Bitmap? {
        val modelPath = "best_weight_recorte_region_interes_official_epch12_float32_v3.tflite"

        val labelPath = "labels_2.txt"

        val detector2 = Detector(activity, modelPath, labelPath)
        detector2.setup()

        val resultado = realizarProcesoPrediccionUltralyticsYolov8TFLite1DeImagenV1(
            custom_detector = detector2,
            image = bitmap,
            customConfidenceThreshold = 0.40f
        )

        return if (resultado == null) {
            bitmap // Retorna el bitmap tal cual si resultado es null
        } else {
            resultado.let { resultadoMap ->
                val boxes = resultadoMap["boxes"] as? List<List<Float>> ?: return bitmap
                val scores = resultadoMap["scores"] as? FloatArray ?: return bitmap
                val classes = resultadoMap["classes"] as? FloatArray ?: return bitmap

                val nmsResult = applyNms(
                    scores = scores,
                    bboxes = boxes,
                    classes = classes,
                    customIoUThreshold = 0.2f
                )

                val nmsBboxes = nmsResult["bboxes"] as? List<List<Float>> ?: return bitmap
                val bbox = nmsBboxes.firstOrNull() ?: return bitmap

                // x1: left: 10.00f
                // y1: top: 10.00f
                // x2: right: 60.00f
                // y2: bottom: 10.00f

                var (left, top, right, bottom) = bbox
                left += 10.00f
                top += 10.00f
                right += 10.00f
                bottom += 10.00f

                recortarBitmap(bitmap, left, top, right, bottom)
            }
        }
    }


    fun realizarProcesoPrediccionUltralyticsYolov8TFLite1DeImagenV1(
        custom_detector: Detector, // Agrega este parámetro
        image: Bitmap,
        customConfidenceThreshold: Float
    ): Map<String, Any>? {

        val mutableImage: Bitmap = image.copy(image.config, true)
        val imageWidth = mutableImage.width
        val imageHeight = mutableImage.height

        // Redimensionar el Bitmap directamente si es necesario
        val resizedImage = Bitmap.createScaledBitmap(mutableImage, imageWidth, imageHeight, false)

        val BoxesapplyNMS = custom_detector.detect(resizedImage, customConfidenceThreshold)

        val newLocationsList = mutableListOf<List<Float>>()
        val classes = mutableListOf<Float>()
        val scores = mutableListOf<Float>()

        BoxesapplyNMS?.forEach { box ->
            val left = box.x1 * imageWidth // x_min // x1
            val top = box.y1 * imageHeight // y_min // y1
            val right = box.x2 * imageWidth // x_max // x2
            val bottom = box.y2 * imageHeight // y_max // y2

            val customBox = listOf(left, top, right, bottom)
            newLocationsList.add(customBox)

            // Agregar el valor de la clase a la lista
            classes.add(box.cls.toFloat())
            scores.add(box.cnf.toFloat())
        }

        return if (BoxesapplyNMS != null && BoxesapplyNMS.isNotEmpty()) {
            mapOf(
                "boxes" to newLocationsList,
                "classes" to classes.toFloatArray(),
                "scores" to scores.toFloatArray()
            )
        } else {
            null
        }
    }

    fun recortarBitmap(bitmap: Bitmap, left: Float, top: Float, right: Float, bottom: Float): Bitmap {
        val x1 = left.toInt().coerceIn(0, bitmap.width)
        val y1 = top.toInt().coerceIn(0, bitmap.height)
        val crop_width = (right - left).toInt().coerceAtMost(bitmap.width - x1)
        val crop_height = (bottom - top).toInt().coerceAtMost(bitmap.height - y1)

        return Bitmap.createBitmap(bitmap, x1, y1, crop_width, crop_height)
    }

    fun applyNms(
        scores: FloatArray,
        bboxes: List<List<Float>>,
        classes: FloatArray,
        customIoUThreshold: Float
    ): Map<String, Any> {
        // Ordenar las detecciones por puntaje de confianza (de mayor a menor)
        val sortedIndices = scores.indices.sortedByDescending { scores[it] }.toMutableList()

        // Lista para almacenar las mejores detecciones
        val selectedIndices = mutableListOf<Int>()

        // Mientras queden detecciones por revisar
        while (sortedIndices.isNotEmpty()) {
            // Elegir la detección con mayor puntaje
            val currentIndex = sortedIndices.first()
            selectedIndices.add(currentIndex)
            sortedIndices.removeAt(0)

            // Lista para almacenar índices a eliminar
            val removeIndices = mutableListOf<Int>()

            // Iterar sobre las detecciones restantes
            for (i in sortedIndices.indices) {
                val iou = calculateIou(bboxes[currentIndex], bboxes[sortedIndices[i]])
                if (iou >= customIoUThreshold) {
                    removeIndices.add(i)
                }
            }

            // Eliminar los índices no deseados de la lista de detecciones restantes
            for (i in removeIndices.indices.reversed()) {
                sortedIndices.removeAt(removeIndices[i])
            }
        }

        // Recopilar los resultados finales
        val selectedScores = selectedIndices.map { scores[it] }
        val selectedBboxes = selectedIndices.map { bboxes[it] }
        val selectedClasses = selectedIndices.map { classes[it] }

        // Devolver los resultados finales
        return mapOf(
            "scores" to selectedScores.toFloatArray(),
            "bboxes" to selectedBboxes,
            "classes" to selectedClasses.toFloatArray()
        )
    }

    // Función para calcular IoU (Intersection over Union)
    fun calculateIou(box1: List<Float>, box2: List<Float>): Float {
        val x1 = maxOf(box1[0], box2[0])
        val y1 = maxOf(box1[1], box2[1])
        val x2 = minOf(box1[2], box2[2])
        val y2 = minOf(box1[3], box2[3])

        val intersectionArea = maxOf(0.0f, x2 - x1) * maxOf(0.0f, y2 - y1)
        val box1Area = (box1[2] - box1[0]) * (box1[3] - box1[1])
        val box2Area = (box2[2] - box2[0]) * (box2[3] - box2[1])
        val unionArea = box1Area + box2Area - intersectionArea

        return if (unionArea > 0) intersectionArea / unionArea else 0.0f
    }

}