package com.deeplarva.iiap.gob.pe.modules.prediction
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


import java.io.File
import java.io.FileWriter


class Detect640x640(private val activity: Context) {

    val paint = Paint()

    private var numChannel = 0
    private var numElements = 0
    private var tensorWidth = 0
    private var tensorHeight = 0

    lateinit var imageView: ImageView
    lateinit var textView: TextView

    lateinit var labels: List<String>

    val imageProcessor = ImageProcessor.Builder().add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)).build()

    init {

        paint.setColor(Color.BLUE)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5.0f

        //labels = FileUtil.loadLabels(this, "labels.txt")
        labels = listOf("objeto_interes")
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun iniciarProcesoGlobalPrediction(
        fileName: String,
        bitmap: Bitmap,
        splitWidth: Int,
        splitHeight: Int,
        overlap: Float,
        miBatchSize: Int,
        miCustomConfidenceThreshold: Float,
        miCustomIoUThreshold: Float
    ): FinalResult {

        val initialTime = Instant.now()
        val startTimeMillis = System.currentTimeMillis()

        // Dividir la imagen en partes usando la función splitImages
        val splits_results = splitImages(bitmap, splitWidth, splitHeight, overlap)

        val splitImagesDict = splits_results.splitImagesDict
        val imageSplitsKeys = splits_results.imageSplitsKeys
        val imageSplits = splits_results.imageSplits

        println("splitImagesDict: $splitImagesDict")
        println("imageSplitsKeys: $imageSplitsKeys")
        println("imageSplits: $imageSplits")

        var allResults: List<Map<String, Any>>

        val modelPath = "best_weight_ds_official_epch250_float32_v2.tflite" // TODO: CHANGED
        val labelPath = "labels_2.txt"
        val detector2 = Detector(activity, modelPath, labelPath)
        detector2.setup()

        allResults = realizarProcesoPrediccionUltralyticsYolov8TFLite1DeImagenesSpliteadasV1(
            detector2, // Agrega este parámetro
            imageSplits,
            imageSplitsKeys,
            miCustomConfidenceThreshold,
            miCustomIoUThreshold
        )
        val scaledPredictedAnnotations = getAllPredictedAnnotationsAndScaledData(allResults)

        // Aplicar NMS
        val nmsScaledPredictedAnnotations = applyNms(
            scaledPredictedAnnotations.scores,
            scaledPredictedAnnotations.centroids,
            scaledPredictedAnnotations.bboxs,
            scaledPredictedAnnotations.categoryIds,
            scaledPredictedAnnotations.keys,
            customIouThreshold = miCustomIoUThreshold // Umbral de IoU personalizado
        )

        val tiempo_tomado = calculateTimeDifference(initialTime, Instant.now())
        val totalTime = System.currentTimeMillis() - startTimeMillis

        // Obtener el conteo total con el algoritmo Golden
        val TotalPredictions = mapOf(
            miCustomIoUThreshold to nmsScaledPredictedAnnotations.scores.size
        )

        val outputFileName = "${fileName}_${miCustomConfidenceThreshold}_conf_predictions.txt" // Cambia el nombre del archivo según tu necesidad
        savePredictionsToTextFile(activity, fileName, miCustomConfidenceThreshold, TotalPredictions, tiempo_tomado, outputFileName)

        return plotPredictedODAnnotationsDataForAndroid(nmsScaledPredictedAnnotations, bitmap, totalTime)

    }
    fun realizarProcesoPrediccionUltralyticsYolov8TFLite1DeImagenesSpliteadasV1(
        custom_detector: Detector, // Agrega este parámetro
        imageSplits: Array<Bitmap?>,
        imageSplitsKeys: Array<String?>,
        customConfidenceThreshold: Float,
        customIoUThreshold: Float
    ): List<Map<String, Any>> {
        // Crear el título de la barra de progreso
        val progressBarTitle = "results_${customConfidenceThreshold}_conf_thr_${customIoUThreshold}_iou_thr"

        val allResults = mutableListOf<Map<String, Any>>()
        val totalBatches = imageSplits.size

        for ((batchIndex, batchImgElement) in imageSplits.withIndex()) {

            val batchSplitsKey = imageSplitsKeys.getOrNull(batchIndex)

            var mutableBatchImgElement: Bitmap? = null
            var mutableBatchImgElement2: Bitmap? = null

            var split_imgWidth: Int? = null
            var split_imgHeight: Int? = null

            batchImgElement?.let {
                mutableBatchImgElement = it // Declarar como variable mutable
                split_imgWidth = it.width
                split_imgHeight = it.height

                mutableBatchImgElement2 = it

                // Redimensionar el Bitmap directamente
                mutableBatchImgElement = Bitmap.createScaledBitmap(mutableBatchImgElement!!, split_imgWidth!!, split_imgHeight!!, false)

            } ?: println("Bitmap Element es nulo")

            val BoxesapplyNMS2 = custom_detector.detect(mutableBatchImgElement2!!, customConfidenceThreshold)

            var new_locations_List2 = mutableListOf<List<Float>>()
            val classes2 = mutableListOf<Float>()
            val scores2 = mutableListOf<Float>()

            if (BoxesapplyNMS2 != null) {
                BoxesapplyNMS2.forEach { box ->

                    var left = box.x1 * split_imgWidth!! // x_min // x1
                    var top = box.y1 * split_imgHeight!! // y_min // y1
                    var right = box.x2 * split_imgWidth!! // x_max // x2
                    var bottom = box.y2 * split_imgHeight!! // y_max // y2

                    val custom_box = listOf(left, top, right, bottom)
                    new_locations_List2.add(custom_box)

                    // Agregar el valor de la clase a la lista
                    classes2.add(box.cls.toFloat())
                    scores2.add(box.cnf.toFloat())
                }
            }

            if (BoxesapplyNMS2 != null && BoxesapplyNMS2.isNotEmpty()) {
                allResults.add(
                    mapOf(
                        "boxes" to new_locations_List2,
                        "classes" to classes2.toFloatArray(),
                        "scores" to scores2.toFloatArray(),
                        "image_splits_keys" to batchSplitsKey.toString()
                    )
                )
            }

            // Actualizar la barra de progreso con título
            printProgressBar(batchIndex + 1, totalBatches, progressBarTitle)

        }

        return allResults
    }
    fun plotPredictedODAnnotationsDataForAndroid(
        nmsAnnotations: PredictedAnnotation,
        bitmap: Bitmap,
        totalTime: Long
    ): FinalResult {

        val totalPredictions = nmsAnnotations.scores.size.takeIf { it > 0 } ?: 0

        if (totalPredictions != 0) {
            var mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutableBitmap)
            val colors = getRandomRedColorsForAndroid(totalPredictions)
            val paint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            nmsAnnotations.bboxs.forEachIndexed { iIdx, bbox ->
                paint.color = colors[0]
                val customLeft = bbox[0]
                val customTop = bbox[1]
                val customRight = bbox[2]
                val customBottom = bbox[3]
                canvas.drawRect(customLeft, customTop, customRight, customBottom, paint)

            }
//            mutableBitmap = resizeBitmapByPercentage(mutableBitmap, 0.4f)
            return FinalResult(mutableBitmap, totalPredictions, nmsAnnotations.bboxs, totalTime)
//            return FinalResult(bitmap, totalPredictions, boxes.map { listOf(it) })
        } else {
            return FinalResult(null, 0, listOf(), totalTime)
        }
    }
    fun savePredictionsToTextFile(
        context: Context,
        imageName: String,
        confidenceThreshold: Float,
        predictions: Map<Float, Int>,
        tiempo_tomado: String,
        outputFileName: String
    ) {
        val file = File(context.getExternalFilesDir(null), outputFileName)
        val writer = FileWriter(file, true)

        writer.append("imageName,ConfidenceThreshold,nmsThreshold,totalPredicciones,tiempoTomado\n")
        for ((nmsThreshold, totalPredicciones) in predictions) {
            writer.append("$imageName,$confidenceThreshold,$nmsThreshold,$totalPredicciones,$tiempo_tomado\n")
        }
        writer.flush()
        writer.close()

        println("Archivo de texto guardado en: ${file.absolutePath}")
    }
    fun splitImages(image: Bitmap, splitWidth: Int, splitHeight: Int, overlap: Float): SplitImagesResult {
        val imgWidth = image.width
        val imgHeight = image.height

        val xPoints = startPoints(imgWidth, splitWidth, overlap)
        val yPoints = startPoints(imgHeight, splitHeight, overlap)

        val numSplits = xPoints.size * yPoints.size
        val imageSplitsKeys = arrayOfNulls<String>(numSplits)
        val imageSplits = arrayOfNulls<Bitmap>(numSplits)
        val splitImagesDict = mutableMapOf<String, Bitmap>() // Cambiado el tipo a Bitmap


        var contSplit = 0

        for (i in yPoints) {
            for (j in xPoints) {
                val key = "${i?.toFloat()}:${j?.toFloat()},${splitHeight?.toFloat()}:${splitWidth?.toFloat()}"
                customPrint(j,"j")
                customPrint(j,"i")
                customPrint(splitWidth,"splitWidth")
                customPrint(splitHeight,"splitHeight")
                val split = Bitmap.createBitmap(image, j, i, splitWidth, splitHeight)

                splitImagesDict[key] = split
                imageSplitsKeys[contSplit] = key
                imageSplits[contSplit] = split

                contSplit++
            }
        }

        return SplitImagesResult(splitImagesDict, imageSplitsKeys, imageSplits)

    }


    fun getRandomColorsForAndroid(numColors: Int): List<Int> {
        val colors = mutableListOf<Int>()

        repeat(numColors) {
            val randomColor = Color.rgb(
                (0..255).random(),
                (0..255).random(),
                (0..255).random()
            )
            colors.add(randomColor)
        }

        return colors
    }

    fun getRandomBlueColorsForAndroid(numColors: Int): List<Int> {
        val colors = mutableListOf<Int>()

        repeat(numColors) {
            val randomBlue = Color.rgb(
                0,                   // Componente rojo (R) fijo en 0
                0,                   // Componente verde (G) fijo en 0
                (128..255).random()  // Componente azul (B) aleatorio
            )
            colors.add(randomBlue)
        }

        return colors
    }

    fun getRandomRedColorsForAndroid(numColors: Int): List<Int> {
        val colors = mutableListOf<Int>()

        repeat(numColors) {
            val randomRed = Color.rgb(
                (128..255).random(), // Componente rojo (R) aleatorio
                0,                   // Componente verde (G) fijo en 0
                0                    // Componente azul (B) fijo en 0
            )
            colors.add(randomRed)
        }

        return colors
    }

    fun getScaledDataOverBbox(predictionBboxData: MutableList<Float>, splitBboxData: List<Float>): MutableList<Float> {

        predictionBboxData[0] += splitBboxData[1] // left // x // x1
        predictionBboxData[1] += splitBboxData[0] // top // y // y1
        predictionBboxData[2] += splitBboxData[1] // left // x // x2
        predictionBboxData[3] += splitBboxData[0] // top // y // y2

        return mutableListOf(
            predictionBboxData[0],
            predictionBboxData[1],
            predictionBboxData[2],
            predictionBboxData[3]
        )
    }
    // Nueva función startPoints
    fun startPoints(size: Int, splitSize: Int, overlap: Float = 0f): List<Int> {
        val points = mutableListOf(0)
        val stride = (splitSize * (1 - overlap)).toInt()
        var counter = 1

        while (true) {
            val pt = stride * counter
            if (pt + splitSize >= size) {
                points.add(size - splitSize)
                break
            } else {
                points.add(pt)
            }
            counter++
        }
        return points
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDuration(duration: Duration): String {
        val seconds = duration.seconds
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
//        val milliseconds = duration.toMillis() % 1000

//        return "Tiempo transcurrido: ${String.format("%02d", hours)} hrs " +
//                "${String.format("%02d", minutes)} min " +
//                "${String.format("%02d", remainingSeconds)} s " +
//                "${String.format("%03d", milliseconds)} ms"

        return "${String.format("%02d", hours)}:" +
                "${String.format("%02d", minutes)}:" +
                "${String.format("%02d", remainingSeconds)}"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateTimeDifference(initialTime: Instant, finalTime: Instant): String {
        val duration = Duration.between(initialTime, finalTime)
        return formatDuration(duration)
    }

    fun getAllPredictedAnnotationsAndScaledData(getallResults: List<Map<String, Any>>): PredictedAnnotation {
        val customScores = mutableListOf<Float>()
        val updatedCentroids = mutableListOf<List<Float>>()
        val scaledBboxes = mutableListOf<List<Float>>()
        val customCategoryIds = mutableListOf<Int>()
        val customKeys = mutableListOf<String>()

        getallResults.forEach { eachResult ->
            val boxesList = eachResult["boxes"] as? List<List<Float>> ?: emptyList()
            val classesArray = eachResult["classes"] as? FloatArray ?: FloatArray(0)
            val scoresArray = eachResult["scores"] as? FloatArray ?: FloatArray(0)
            val imageSplitsKey = eachResult["image_splits_keys"] as? String ?: ""

            boxesList.forEachIndexed { idx, boxValue ->
                val score = scoresArray.getOrNull(idx) ?: 0.0f
                val categoryId = (classesArray.getOrNull(idx)?.toInt()?.minus(1) ?: 0).coerceAtLeast(0)
                val keyValues = Regex("\\d+(\\.\\d+)?").findAll(imageSplitsKey).map { it.value.toFloat() }.toList()

                val scaledBbox = getScaledDataOverBbox(boxValue.toMutableList(), keyValues)

                val (xMin, yMin, xMax, yMax) = scaledBbox

                val centroidX = (xMin + xMax) / 2
                val centroidY = (yMin + yMax) / 2

                customScores.add(score)
                customCategoryIds.add(categoryId)
                customKeys.add(imageSplitsKey)
                scaledBboxes.add(scaledBbox)
                updatedCentroids.add(listOf(centroidY, centroidX))
            }
        }

        return PredictedAnnotation(
            customScores,
            updatedCentroids,
            scaledBboxes,
            customCategoryIds,
            customKeys
        )
    }

    fun applyNms(
        scores: List<Float>,
        centroids: List<List<Float>>,
        bboxs: List<List<Float>>,
        categoryIds: List<Int>,
        keys: List<String>,
        customIouThreshold: Float
    ): PredictedAnnotation {
        // Ordenar las detecciones existentes por puntaje de confianza (de mayor a menor)
        val sortedIndices = scores.indices.sortedByDescending { scores[it] }.toMutableList()

        // Crear lista vacía para almacenar las mejores detecciones
        val selectedIndices = mutableListOf<Int>()

        // Mientras queden detecciones existentes por revisar
        while (sortedIndices.isNotEmpty()) {
            // Elegir la detección con mayor puntaje
            val currentIndex = sortedIndices.first()
            // Agregar la detección con mayor puntaje a las mejores detecciones
            selectedIndices.add(currentIndex)
            // Quitar la detección con mayor puntaje de las detecciones existentes
            sortedIndices.removeAt(0)

            // Crear lista vacía para los elementos a eliminar
            val removeIndices = mutableListOf<Int>()

            // Para cada detección restante en las detecciones existentes
            for (i in sortedIndices.indices) {
                // Calcular superposición con la detección con mayor puntaje
                val iou = calculateIou(bboxs[currentIndex], bboxs[sortedIndices[i]])
                // ¿La superposición es mayor al límite permitido?
                if (iou >= customIouThreshold) {
                    // Agregar elemento no deseado a la lista de elementos a eliminar
                    removeIndices.add(i)
                }
            }
            // Quitar los elementos no deseados de la lista de detecciones existentes
            removeIndices.sortedDescending().forEach { sortedIndices.removeAt(it) }
        }

        // Recopilar resultados finales de la lista de mejores detecciones
        val selectedScores = selectedIndices.map { scores[it] }
        val selectedCentroids = selectedIndices.map { centroids[it] }
        val selectedBboxs = selectedIndices.map { bboxs[it] }
        val selectedCategoryIds = selectedIndices.map { categoryIds[it] }
        val selectedKeys = selectedIndices.map { keys[it] }

        // Devolver resultados finales
        return PredictedAnnotation(
            selectedScores,
            selectedCentroids,
            selectedBboxs,
            selectedCategoryIds,
            selectedKeys
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

    fun customPrint(data: Any, dataName: String, saltoLineaTipo1: Boolean = false, saltoLineaTipo2: Boolean = false, displayData: Boolean = true, hasLen: Boolean = true, wannaExit: Boolean = false) {
        if (saltoLineaTipo1) {
            println("")
        }
        if (saltoLineaTipo2) {
            println("\n")
        }
        if (hasLen) {
            if (displayData) {
                println("$dataName: $data | type: ${data.javaClass} | len: ${(data as? Collection<*>)?.size ?: "N/A"}")
            } else {
                println("$dataName: | type: ${data.javaClass} | len: ${(data as? Collection<*>)?.size ?: "N/A"}")
            }
        } else {
            if (displayData) {
                println("$dataName: $data | type: ${data.javaClass}")
            } else {
                println("$dataName: | type: ${data.javaClass}")
            }
        }
        if (wannaExit) {
            System.exit(0)
        }
    }

    data class SplitImagesResult(
        val splitImagesDict: Map<String, Bitmap>,
        val imageSplitsKeys: Array<String?>,
        val imageSplits: Array<Bitmap?>
    )

    data class PredictedAnnotation(
        val scores: List<Float>,
        val centroids: List<List<Float>>,
        val bboxs: List<List<Float>>,
        val categoryIds: List<Int>,
        val keys: List<String>
    )
    fun printProgressBar(current: Int, total: Int, title: String) {
        val progressBarLength = 50
        val progress = (current.toDouble() / total.toDouble() * progressBarLength).toInt()
        val progressBar = "=".repeat(progress) + " ".repeat(progressBarLength - progress)
        println() // Salto de línea antes de imprimir la barra de progreso
        print("\r$title: [$progressBar] ${current * 100 / total}% [$current/$total]")
        println() // Salto de línea antes de imprimir la barra de progreso
        if (current == total) {
            println() // Para saltar a la siguiente línea cuando se complete la barra de progreso
        }
    }
}