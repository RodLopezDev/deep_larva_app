package com.rodrigo.deeplarva.ml
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.CountDownTimer
import android.widget.ImageView
import android.widget.TextView

import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.ml.DetectSortedAugmentedXtraHistFloat32Yolov8n
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.time.Duration
import java.time.Instant

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

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
        bitmap: Bitmap,
        splitWidth: Int,
        splitHeight: Int,
        overlap: Float,
        miCustomConfidenceThreshold: Float,
        miCustomIoUThresholdNMS: Float
    ): FinalResult {
        // Dividir la imagen en partes usando la función splitImages
        val splits_results = splitImages(bitmap, splitWidth, splitHeight, overlap)

        val splitImagesDict = splits_results.splitImagesDict
        val imageSplitsKeys = splits_results.imageSplitsKeys
        val imageSplits = splits_results.imageSplits

        println("splitImagesDict: $splitImagesDict")
        println("imageSplitsKeys: $imageSplitsKeys")
        println("imageSplits: $imageSplits")

        // Llamada a la función para realizar la predicción en las imágenes divididas
        val initialTime = Instant.now()

        val modelPath = "detect_sorted_augmented_xtra_hist_float32_yolov8n.tflite" // TODO: CHANGED
        val labelPath = "labels_2.txt"

        val detector2 = Detector(activity, modelPath, labelPath)
        detector2.setup()

        val predictedAnnotations = realizarProcesoPrediccionUltralyticsYolov8TFLite1DeImagenesSpliteadasV1(
            detector2,
            imageSplits,
            imageSplitsKeys,
            miCustomConfidenceThreshold
        )

        val finalTime = Instant.now()

        // Calcular el tiempo de diferencia
        val timeDifferenceOutput = calculateTimeDifference(initialTime, finalTime)
        println("timeDifferenceOutput: $timeDifferenceOutput")

        // Convertir PredictedAnnotation a una lista de BoundingBox
        val boundingBoxes = predictedAnnotations.bboxs.mapIndexed { index, box ->
            BoundingBox(
                x1 = box[1],
                y1 = box[0],
                x2 = box[3],
                y2 = box[2],
                cx = (box[1] + box[3]) / 2,
                cy = (box[0] + box[2]) / 2,
                w = box[3] - box[1],
                h = box[2] - box[0],
                cnf = predictedAnnotations.scores[index],
                cls = predictedAnnotations.categoryIds[index],
                clsName = "" // Suponiendo que no tienes el nombre de la clase aquí
            )
        }

        // Aplicar NMS
        val selectedBoxes = applyNMS(boundingBoxes, miCustomIoUThresholdNMS)

        // Convertir selectedBoxes a GroupedAnnotation
        val groupedAnnotations = selectedBoxes.map { box ->
            GroupedAnnotation(
                scores = mutableListOf(box.cnf),
                centroids = mutableListOf(listOf(box.cy, box.cx)),
                bboxs = mutableListOf(listOf(box.y1, box.x1, box.y2, box.x2)),
                categoryIds = mutableListOf(box.cls)
            )
        }

        // Llamada a la función para plotear las anotaciones predichas
        return plotPredictedODAnnotationsDataForAndroid(groupedAnnotations, bitmap, labels)
    }

    //Mas lento
    fun nmsKotlin(bboxes: List<BoundingBox>, threshold: Float): MutableList<BoundingBox> {
        if (bboxes.isEmpty()) return mutableListOf()

        // Ordenar las bounding boxes por puntajes en orden descendente
        val sortedBboxes = bboxes.sortedByDescending { it.cnf }
        val bboxAreas = sortedBboxes.map { (it.x2 - it.x1 + 1) * (it.y2 - it.y1 + 1) }

        val filtered = mutableListOf<BoundingBox>()
        val sortedIdx = sortedBboxes.indices.toMutableList()

        while (sortedIdx.isNotEmpty()) {
            val rbboxIdx = sortedIdx[0]
            filtered.add(sortedBboxes[rbboxIdx])

            val overlap = sortedIdx.drop(1).map { idx ->
                val overlapXMin = max(sortedBboxes[rbboxIdx].x1, sortedBboxes[idx].x1)
                val overlapYMin = max(sortedBboxes[rbboxIdx].y1, sortedBboxes[idx].y1)
                val overlapXMax = min(sortedBboxes[rbboxIdx].x2, sortedBboxes[idx].x2)
                val overlapYMax = min(sortedBboxes[rbboxIdx].y2, sortedBboxes[idx].y2)
                val overlapWidth = max(0f, overlapXMax - overlapXMin + 1)
                val overlapHeight = max(0f, overlapYMax - overlapYMin + 1)
                val overlapArea = overlapWidth * overlapHeight
                val iou = overlapArea / (bboxAreas[rbboxIdx] + bboxAreas[idx] - overlapArea)
                idx to iou
            }

            val deleteIdx = overlap.filter { it.second > threshold }.map { it.first }.toMutableList()
            deleteIdx.add(0, rbboxIdx)

            sortedIdx.removeAll(deleteIdx)
        }

        return filtered
    }

    //Mas rapido
    private fun applyNMS(boxes: List<BoundingBox>, iouThreshold: Float): MutableList<BoundingBox> {
        val sortedBoxes = boxes.sortedByDescending { it.cnf }.toMutableList()
        val selectedBoxes = mutableListOf<BoundingBox>()

        while (sortedBoxes.isNotEmpty()) {
            val first = sortedBoxes.first()
            selectedBoxes.add(first)
            sortedBoxes.remove(first)

            val iterator = sortedBoxes.iterator()
            while (iterator.hasNext()) {
                val nextBox = iterator.next()
                val iou = calculateIoU(first, nextBox)
                if (iou > iouThreshold) {
                    iterator.remove()
                }
            }
        }

        return selectedBoxes
    }

    private fun calculateIoU(box1: BoundingBox, box2: BoundingBox): Float {
        val x1 = maxOf(box1.x1, box2.x1)
        val y1 = maxOf(box1.y1, box2.y1)
        val x2 = minOf(box1.x2, box2.x2)
        val y2 = minOf(box1.y2, box2.y2)
        val intersectionArea = maxOf(0F, x2 - x1) * maxOf(0F, y2 - y1)
        val box1Area = box1.w * box1.h

        val box1Area_v2 = (box1.x2 - box1.x1) * (box1.y2 - box1.y1)
        val box2Area_v2 = (box2.x2 - box2.x1) * (box2.y2 - box2.y1)

        val union = box1Area_v2 + box2Area_v2 - intersectionArea

        return intersectionArea / union
    }


    fun realizarProcesoPrediccionUltralyticsYolov8TFLite1DeImagenesSpliteadasV1(
        custom_detector: Detector,
        imageSplits: Array<Bitmap?>,
        imageSplitsKeys: Array<String?>,
        customConfidenceThreshold: Float
    ): PredictedAnnotation {

        val custom_scores = mutableListOf<Float>()
        val custom_centroids = mutableListOf<List<Float>>()
        val custom_bboxs = mutableListOf<List<Float>>()
        val custom_categoryIds = mutableListOf<Int>()
        val custom_keys = mutableListOf<String>()

        for ((batchIndex, batchImgElement) in imageSplits.withIndex()) {
            val batchSplitsKey = imageSplitsKeys.getOrNull(batchIndex)
            println("Batch Index: $batchIndex")
            println("Batch Splits Key: $batchSplitsKey")
            println("Batch Img Element: $batchImgElement")
            println("-------------")

            var mutableBatchImgElement: Bitmap? = null
            var mutableBatchImgElement2: Bitmap? = null

            var split_imgWidth: Int? = null
            var split_imgHeight: Int? = null

            batchImgElement?.let {
                mutableBatchImgElement = it
                split_imgWidth = it.width
                split_imgHeight = it.height

                mutableBatchImgElement2 = it

                mutableBatchImgElement = Bitmap.createScaledBitmap(mutableBatchImgElement!!, split_imgWidth!!, split_imgHeight!!, false)
            } ?: println("Bitmap Element es nulo")

            val BoxesapplyNMS2 = custom_detector.detect(mutableBatchImgElement2!!, customConfidenceThreshold)

            if (BoxesapplyNMS2 != null) {
                BoxesapplyNMS2.forEach { box ->
                    val left = box.x1 * split_imgWidth!! // x_min // x1
                    val top = box.y1 * split_imgHeight!! // y_min // y1
                    val right = box.x2 * split_imgWidth!! // x_max // x2
                    val bottom = box.y2 * split_imgHeight!! // y_max // y2

                    val value_box = listOf(top, left, bottom, right)
                    val value_score = box.cnf.toFloat()
                    val rawValue = box.cls.toInt()
                    val value_category_id = if (rawValue > 0) rawValue - 1 else rawValue
                    val value_key = batchSplitsKey.toString()

                    custom_scores.add(value_score)
                    custom_centroids.add(listOf(1.1f, 1.1f)) // Placeholder for centroids
                    custom_bboxs.add(value_box)
                    custom_categoryIds.add(value_category_id)
                    custom_keys.add(value_key)
                }
            }
        }

        val predictedAnnotation = PredictedAnnotation(custom_scores, custom_centroids, custom_bboxs, custom_categoryIds, custom_keys)
        return obtenerDatosEscaladoPrediccionODV1(predictedAnnotation)
    }
    fun plotPredictedODAnnotationsDataForAndroid(
        filteredAnnotations: List<GroupedAnnotation>,
        bitmap: Bitmap,
        //imageView: ImageView,
        //textView: TextView,
        labels: List<String>): FinalResult {

        val name_fun = "plotPredictedODAnnotationsDataForAndroid"
        println("Dentro de la funcion: $name_fun")

        val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)

        val total_predictions = (filteredAnnotations as? Collection<*>)?.size ?: 0

        //val dsmkdmskmkmsmdsede = "dsmkdmskmkmsmdsede"
        //println("dsmkdmskmkmsmdsede: $dsmkdmskmkmsmdsede")


        // Mostrar un Toast por 4 segundos
        var toast: Toast

        if (total_predictions != 0) {
            // La variable total_predictions es diferente de cero
            println("El total de predicciones es: $total_predictions")
            // Puedes poner aquí cualquier otra acción que desees realizar

            var colors2 = getRandomColorsForAndroid(total_predictions)

            val h = mutable.height

            for ((i_idx, group) in filteredAnnotations.withIndex()) {
                for ((j_idx, sub_group) in filteredAnnotations[i_idx].bboxs.withIndex()) {
                    val my_bbox = filteredAnnotations[i_idx].bboxs[j_idx]
                    val my_score = filteredAnnotations[i_idx].scores[j_idx]
                    val my_category_id = filteredAnnotations[i_idx].categoryIds[j_idx]

                    val custom_top = my_bbox[0]
                    val custom_left = my_bbox[1]
                    val custom_bottom = my_bbox[2]
                    val custom_right = my_bbox[3]

                    paint.textSize = h / 15f

                    paint.strokeWidth = h / 85f
                    // paint.color = colors2[i_idx]
                    paint.color = colors2[my_category_id]
                    paint.style = Paint.Style.STROKE

                    canvas.drawRect(RectF(custom_left, custom_top, custom_right, custom_bottom), paint)


                    paint.style = Paint.Style.FILL
                }
            }

            val resizedBitmap = resizeBitmapByPercentage(mutable, 0.4f)

            return FinalResult(resizedBitmap, total_predictions)
        } else {
            // La variable total_predictions es igual a cero
            println("No hay predicciones disponibles.")

            return FinalResult(null, 0)
        }

    }


    fun resizeBitmapByPercentage(originalBitmap: Bitmap, percentage: Float): Bitmap {
        val width = (originalBitmap.width * percentage).toInt()
        val height = (originalBitmap.height * percentage).toInt()

        return resizeBitmap(originalBitmap, width, height)
    }

    fun resizeBitmap(originalBitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val scaleWidth = newWidth.toFloat() / originalBitmap.width
        val scaleHeight = newHeight.toFloat() / originalBitmap.height

        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        return Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, false)
    }

    // Nueva función splitImages
//    fun splitImages(image: Bitmap, splitWidth: Int, splitHeight: Int, overlap: Float): Map<String, Bitmap> {
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
    // Función para obtener el tipo y la longitud de un array
    fun getTypeAndLength(array: FloatArray): String {
        val type = array.javaClass.componentType.simpleName
        val length = array.size
        return "Type: $type, Length: $length"
    }

    fun getScaledDataOverBbox(predictionBboxData: MutableList<Float>, splitBboxData: List<Float>): MutableList<Float> {

        // aqui_toy
        predictionBboxData[0] += splitBboxData[0] // top // y
        predictionBboxData[1] += splitBboxData[1] // left // x
        predictionBboxData[2] += splitBboxData[0] // bottom // y
        predictionBboxData[3] += splitBboxData[1] // right // x


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
        val milliseconds = duration.toMillis() % 1000

        return "Tiempo transcurrido: ${String.format("%02d", hours)} hrs " +
                "${String.format("%02d", minutes)} min " +
                "${String.format("%02d", remainingSeconds)} s " +
                "${String.format("%03d", milliseconds)} ms"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateTimeDifference(initialTime: Instant, finalTime: Instant): String {
        val duration = Duration.between(initialTime, finalTime)
        return formatDuration(duration)
    }


    fun obtenerDatosEscaladoPrediccionODV1(variable_triple: PredictedAnnotation): PredictedAnnotation {
        val bboxs = mutableListOf<List<Float>>()
        val centroids = mutableListOf<List<Float>>()
        val name_fun = "obtenerDatosEscaladoPrediccionODV1"
        println("Dentro de la funcion: $name_fun")
        for (jIdx in variable_triple.bboxs.indices) {
            val value_key = variable_triple.keys[jIdx]
            val keyValues = Regex("\\d+(\\.\\d+)?").findAll(value_key).map { it.value.toFloat() }.toList()

            val scaledBbox = getScaledDataOverBbox(variable_triple.bboxs[jIdx] as MutableList<Float>, keyValues)

            val x_min = scaledBbox[1]
            val y_min = scaledBbox[0]
            val x_max = scaledBbox[3]
            val y_max = scaledBbox[2]

            val centroid_x: Float = ((x_min + x_max) / 2).toFloat()
            val centroid_y: Float = ((y_min + y_max) / 2).toFloat()

            bboxs.add(scaledBbox)
            centroids.add(listOf(centroid_y, centroid_x))  // x: centroid[1] | y: centroid[0]
        }

        return PredictedAnnotation(
            variable_triple.scores,
            centroids,
            bboxs,
            variable_triple.categoryIds,
            variable_triple.keys
        )
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

    data class GroupedAnnotation(
        val scores: MutableList<Float>,
        val centroids: MutableList<List<Float>>,
        val bboxs: MutableList<List<Float>>,
        val categoryIds: MutableList<Int>
    )

    data class BoundingBox(
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        val cx: Float,
        val cy: Float,
        val w: Float,
        val h: Float,
        val cnf: Float,
        val cls: Int,
        val clsName: String
    )
}