package com.rodrigo.deeplarva.ml

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.CountDownTimer
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.ml.Detect300x300Uint8WithMetadata
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.time.Duration
import java.time.Instant
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

@RequiresApi(Build.VERSION_CODES.O)
class Detect300x300(private val activity: AppCompatActivity) {

    val paint = Paint()


    private var labels: List<String>

    private val imageProcessor = ImageProcessor.Builder().add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)).build()

    init {

        paint.color = Color.BLUE
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
        miBatchSize: Int,
        miCustomConfidenceThreshold: Double,
        distanceThreshold: Float
    ): FinalResult {
        // Dividir la imagen en partes usando la función splitImages
        val splits_results = splitImages(bitmap, splitWidth, splitHeight, overlap)

        val splitImagesDict = splits_results.splitImagesDict
        val imageSplitsKeys = splits_results.imageSplitsKeys
        val imageSplits = splits_results.imageSplits

        println("splitImagesDict: $splitImagesDict")
        println("imageSplitsKeys: $imageSplitsKeys")
        println("imageSplits: $imageSplits")

        // Crear una instancia del modelo
        val miModelo = Detect300x300Uint8WithMetadata.newInstance(activity)

        // Llamada a la función para realizar la predicción en las imágenes divididas
        val initialTime = Instant.now()
        val allResults = realizarProcesoPrediccionSSDMobileNetV1QuantizedTFLite1DeImagenesSpliteadasV1(
            miModelo,
            imageSplits,
            imageSplitsKeys,
            miBatchSize,
            miCustomConfidenceThreshold
        )
        val finalTime = Instant.now()

        // Calcular el tiempo de diferencia
        val timeDifferenceOutput = calculateTimeDifference(initialTime, finalTime)
        println("timeDifferenceOutput: $timeDifferenceOutput")

        println("allResults: $allResults")

        // Obtener los resultados de la predicción
        val variable_triple = getAllPredictedAnnotations(allResults)

        // Imprimir los resultados actuales
        customPrint(variable_triple.scores, "[CURRENT] scores", false, false, true, true)
        customPrint(variable_triple.centroids, "[CURRENT] centroids", false, false, true, true)
        customPrint(variable_triple.bboxs, "[CURRENT] bboxs", false, false, true, true)
        customPrint(variable_triple.categoryIds, "[CURRENT] categoryIds", false, false, true, true)
        customPrint(variable_triple.keys, "[CURRENT] keys", false, false, true, true)

        // Escalar los resultados de la predicción
        val variable_triple_actualizado = obtenerDatosEscaladoPrediccionODV1(variable_triple)

        println("\n")

        // Imprimir los resultados actualizados
        customPrint(variable_triple_actualizado.scores, "[ACTUALIZADO] scores", false, false, true, true)
        customPrint(variable_triple_actualizado.centroids, "[ACTUALIZADO] centroids", false, false, true, true)
        customPrint(variable_triple_actualizado.bboxs, "[ACTUALIZADO] bboxs", false, false, true, true)
        customPrint(variable_triple_actualizado.categoryIds, "[ACTUALIZADO] categoryIds", false, false, true, true)
        customPrint(variable_triple_actualizado.keys, "[ACTUALIZADO] keys", false, false, true, true)

        // Llamada a la función para agrupar las anotaciones
        val groupedAnnotations = groupODAnnotationsDataV1(variable_triple_actualizado, distanceThreshold)

        customPrint((groupedAnnotations as? Collection<*>)?.size ?: 0, "[len_groupedAnnotations] ", true, true, false, false)
        customPrint(groupedAnnotations, "[GET_LEN] groupedAnnotations", false, false, true, true, false)

        // Llamada a la función para filtrar las anotaciones agrupadas
        val filteredAnnotations = filterGroupedAnnotationsDataV2(groupedAnnotations)


        println("[len_filteredAnnotations] : ${(filteredAnnotations as? Collection<*>)?.size ?: 0}")

        customPrint((filteredAnnotations as? Collection<*>)?.size ?: 0, "[len_filteredAnnotations] ", true, true, false, false)
        customPrint(filteredAnnotations, "[GET_LEN] filteredAnnotations", false, false, true, true, false)

        // Llamada a la función para plotear las anotaciones predichas
        return plotPredictedODAnnotationsDataForAndroid(filteredAnnotations, bitmap, labels)
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
//                val key = "$i:${i + splitHeight},${j}:${j + splitWidth}"
//                val key = "$i:${j},${splitHeight}:${splitWidth}"
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

    fun realizarProcesoPrediccionSSDMobileNetV1QuantizedTFLite1DeImagenesSpliteadasV1(
        model: Detect300x300Uint8WithMetadata,
        imageSplits: Array<Bitmap?>,
        imageSplitsKeys: Array<String?>,
        batchSize: Int,
        customConfidenceThreshold: Double
    ): List<Map<String, Any>> {

        val allResults = mutableListOf<Map<String, Any>>()

        for (startIdx in 0 until imageSplits.size step batchSize) {
            val endIdx = min(startIdx + batchSize, imageSplits.size)
            val batchSplits = imageSplits.copyOfRange(startIdx, endIdx)
            val batchSplitsKeys = imageSplitsKeys.copyOfRange(startIdx, endIdx)

            for ((batchIndex, batchImgElement) in batchSplits.withIndex()) {

                val batchSplitsKey = batchSplitsKeys.getOrNull(batchIndex)
                println("Batch Index: $batchIndex")
                println("Batch Splits Key: $batchSplitsKey")
                println("Batch Img Element: $batchImgElement")
                println("-------------")

                var mutableBatchImgElement: Bitmap? = null
                var split_imgWidth: Int? = null
                var split_imgHeight: Int? = null

                batchImgElement?.let {
                    mutableBatchImgElement = it // Declarar como variable mutable
                    split_imgWidth = it.width
                    split_imgHeight = it.height

//                    println("Bitmap Element Width: $split_imgWidth")
//                    println("Bitmap Element Height: $split_imgHeight")

                    // Redimensionar el Bitmap directamente
                    mutableBatchImgElement = Bitmap.createScaledBitmap(mutableBatchImgElement!!, split_imgWidth!!, split_imgHeight!!, false)

                    // Aquí puedes realizar cualquier otra lógica específica de tu aplicación
                    // También puedes llamar a "model.process(batchImgElement)" si es necesario
                } ?: println("Bitmap Element es nulo")

                var image = TensorImage.fromBitmap(mutableBatchImgElement)

                image = imageProcessor.process(image)

                val outputs2 = model.process(image)


                var locations = outputs2.locationAsTensorBuffer.floatArray
                var classes = outputs2.categoryAsTensorBuffer.floatArray
                var scores = outputs2.scoreAsTensorBuffer.floatArray

                var new_locations_List = mutableListOf<List<Float>>()

                scores.forEachIndexed { i_idx, _ ->
                    var x = i_idx
                    x *= 4
                    val x_min = locations[x + 1] * (split_imgWidth!!).toFloat()
                    val y_min = locations[x] * (split_imgHeight!!).toFloat()
                    val x_max = locations[x + 3] * (split_imgWidth!!).toFloat()
                    val y_max = locations[x + 2] * (split_imgHeight!!).toFloat()

                    var top = locations[x] * (split_imgHeight!!).toFloat()
                    var left = locations[x + 1] * (split_imgWidth!!).toFloat()
                    var bottom = locations[x + 2] * (split_imgHeight!!).toFloat()
                    var right = locations[x + 3] * (split_imgWidth!!).toFloat()

                    customPrint(top, "top", hasLen = false)
                    customPrint(left, "left", hasLen = false)
                    customPrint(bottom, "bottom", hasLen = false)
                    customPrint(right, "right", hasLen = false)

                    val subList = listOf(top, left, bottom, right)
                    new_locations_List.add(subList as List<Float>)
                }

                // Filtrar detecciones basadas en la confianza
                val indicesAEliminar = scores.indices.filter { scores[it] < customConfidenceThreshold }

                // Crear nuevas listas que contengan solo los elementos que deseas mantener
                scores = scores.filterIndexed { index, _ -> !indicesAEliminar.contains(index) }.toFloatArray()
//                new_locations_Array = new_locations_Array.filterIndexed { index, _ -> !indicesAEliminar.contains(index) }.toFloatArray()
                new_locations_List = new_locations_List.filterIndexed { index, _ -> !indicesAEliminar.contains(index) }.toMutableList()
                classes = classes.filterIndexed { index, _ -> !indicesAEliminar.contains(index) }.toFloatArray()

                // Verificar si scores tiene elementos después del filtrado
                if (scores.isNotEmpty()) {

//                    scores.forEachIndexed { i_idx, _ ->
//                        println("scores[$i_idx] después del filtrado: ${scores[i_idx]}")
//                        println("new_locations_Array[$i_idx] después del filtrado: ${new_locations_List[i_idx]}")
//                        println("classes[$i_idx] después del filtrado: ${classes[i_idx]}")
//                    }

                    allResults.add(mapOf("boxes" to new_locations_List, "classes" to classes, "scores" to scores, "image_splits_keys" to batchSplitsKey.toString()))  // Creando el diccionario directamente

                } else {
                    println("La lista 'scores' está vacía después del filtrado.")
                }


//                // Eliminar elementos de los arrays
//                val filteredLocations = locations.filterIndexed { index, _ -> !indicesAEliminar.contains(index) }
//                val filteredClasses = classes.filterIndexed { index, _ -> !indicesAEliminar.contains(index) }
//                val filteredScores = scores.filterIndexed { index, _ -> !indicesAEliminar.contains(index) }
//                val filteredNumberOfDetections = numberOfDetections.filterIndexed { index, _ -> !indicesAEliminar.contains(index) }

                // Tu lógica aquí
                // Por ejemplo, puedes usar "model.process(batchImgElement)" para procesar cada fragmento
                // Asegúrate de adaptar la lógica según las necesidades de tu aplicación
            }
        }


        return allResults
    }


    fun groupODAnnotationsDataV1(annotationsData: PredictedAnnotation, threshold: Float): List<GroupedAnnotation> {
        val (scores, centroids, bboxs, categoryIds, _) = annotationsData

        val groupedAnnotationsData = mutableListOf<GroupedAnnotation>()

        for (idx in bboxs.indices) {
            var foundGroup = false

            // Check if the point is close to any existing group
            for (group in groupedAnnotationsData) {
                for (groupCentroid in group.centroids) {
                    if (euclideanDistance(centroids[idx], groupCentroid) < threshold) {
                        group.scores.add(scores[idx])
                        group.centroids.add(centroids[idx])
                        group.bboxs.add(bboxs[idx])
                        group.categoryIds.add(categoryIds[idx])
                        foundGroup = true
                        break
                    }
                }
                if (foundGroup) break
            }

            // If the point is not close to any existing group, create a new group
            if (!foundGroup) {
                val newGroup = GroupedAnnotation(
                    scores = mutableListOf(scores[idx]),
                    centroids = mutableListOf(centroids[idx]),
                    bboxs = mutableListOf(bboxs[idx]),
                    categoryIds = mutableListOf(categoryIds[idx])
                )
                groupedAnnotationsData.add(newGroup)
            }
        }

        return groupedAnnotationsData
    }

    fun euclideanDistance(points1: List<Float>, points2: List<Float>): Float {
        return sqrt((points1[1] - points2[1]).pow(2) + (points1[0] - points2[0]).pow(2))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateTimeDifference(initialTime: Instant, finalTime: Instant): String {
        val duration = Duration.between(initialTime, finalTime)
        return formatDuration(duration)
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

    fun getAllPredictedAnnotations(getallResults: List<Map<String, Any>>): PredictedAnnotation {

        val custom_scores = mutableListOf<Float>()
        val custom_centroids = mutableListOf<List<Float>>()
        val custom_bboxs = mutableListOf<List<Float>>()
        val custom_categoryIds = mutableListOf<Int>()
        val custom_keys = mutableListOf<String>()

        for ((index, eachResult) in getallResults.withIndex()) {
            val boxesList = eachResult["boxes"] as MutableList<*>
            val classesArray = eachResult["classes"] as? FloatArray
            val scoresArray = eachResult["scores"] as? FloatArray
            val imageSplitsKeys = eachResult["image_splits_keys"] as? String

            boxesList.forEachIndexed { jIdx, box_value ->
                val value_centroid = listOf(1.1f, 1.1f)
                val value_score = scoresArray?.get(jIdx) ?: 0.0f


                val value_box = box_value as List<Float>

                val rawValue = classesArray?.get(jIdx)?.toInt() ?: 0
                val value_category_id = if (rawValue > 0) rawValue - 1 else rawValue
                val value_key = imageSplitsKeys.toString()


                custom_scores.add(value_score)
                custom_centroids.add(value_centroid)
                custom_bboxs.add(value_box)
                custom_categoryIds.add(value_category_id)
                custom_keys.add(value_key)
            }

        }

        return PredictedAnnotation(custom_scores, custom_centroids, custom_bboxs, custom_categoryIds, custom_keys)

    }


    fun obtenerDatosEscaladoPrediccionODV1(variable_triple: PredictedAnnotation): PredictedAnnotation {
        val bboxs = mutableListOf<List<Float>>()
        val centroids = mutableListOf<List<Float>>()
        // iIdx
        for (jIdx in variable_triple.bboxs.indices) {

            // val keyValues = variable_triple.keys[jIdx].split("\\d+".toRegex()).filter { it.isNotEmpty() }.map { it.toInt() }


            // Obtener el valor correspondiente para keys
            val value_key = variable_triple.keys[jIdx]
            // Aplicar la expresión regular y convertir los resultados a floats
            // val keyValues = Regex("\\d+").findAll(value_key).map { it.value.toFloat() }.toList()
            val keyValues = Regex("\\d+(\\.\\d+)?").findAll(value_key).map { it.value.toFloat() }.toList()

            // customPrint(value_key, "[ojo] value_key",saltoLineaTipo1 = false,saltoLineaTipo2 = false, displayData = true, hasLen = true, wannaExit = false)
            // customPrint(keyValues, "[ojo2] keyValues",saltoLineaTipo1 = false,saltoLineaTipo2 = false, displayData = true, hasLen = true, wannaExit = false)

            // println("\n")
            // println(" @@@@@@@@@@@@@@ SCORE: ${variable_triple.scores[jIdx]} @@@@@@@@@@@@@@ ")
            // val scaledBbox = getScaledDataOverBbox(variable_triple.bboxs[jIdx] as MutableList<Float>, keyValues)
            val scaledBbox = getScaledDataOverBbox(variable_triple.bboxs[jIdx] as MutableList<Float>, keyValues)

            // aqui_toy
            // scaledBbox[0] // top // y_min
            // scaledBbox[1] // left // x_min
            // scaledBbox[2] // bottom // y_max
            // scaledBbox[3] // right // x_max

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

        // return PredictedAnnotation(
        //     variable_triple.scores,
        //     variable_triple.centroids,
        //     bboxs,
        //     variable_triple.categoryIds,
        //     variable_triple.keys
        // )
    }

    fun getScaledDataOverBbox(predictionBboxData: MutableList<Float>, splitBboxData: List<Float>): MutableList<Float> {
        // Inicializa la variable para almacenar los contornos actualizados

//        println("")
//        customPrint(splitBboxData, "splitBboxData",saltoLineaTipo1 = false,saltoLineaTipo2 = false, displayData = true, hasLen = true, wannaExit = false)
//        customPrint(predictionBboxData, "[OLD] predictionBboxData",saltoLineaTipo1 = false,saltoLineaTipo2 = false, displayData = true, hasLen = true, wannaExit = false)


        // aqui_toy
        predictionBboxData[0] += splitBboxData[0] // top // y
        predictionBboxData[1] += splitBboxData[1] // left // x
        predictionBboxData[2] += splitBboxData[0] // bottom // y
        predictionBboxData[3] += splitBboxData[1] // right // x


//        customPrint(predictionBboxData, "[NEW] predictionBboxData",saltoLineaTipo1 = false,saltoLineaTipo2 = false, displayData = true, hasLen = true, wannaExit = false)
        // customPrint(espejoXResultado, "espejoXResultado",saltoLineaTipo1 = false,saltoLineaTipo2 = false, displayData = true, hasLen = true, wannaExit = false)
        // customPrint(espejoYResultado, "espejoYResultado",saltoLineaTipo1 = false,saltoLineaTipo2 = false, displayData = true, hasLen = true, wannaExit = true)

        // predictionBboxData[3] += splitBboxData[2] // x_min
        // predictionBboxData[0] += splitBboxData[3] // y_min
        // predictionBboxData[1] += splitBboxData[2] // x_max
        // predictionBboxData[2] += splitBboxData[3] // y_max


        // val y1 = key_values[0]
        // val y2 = key_values[1]
        // val x1 = key_values[2]
        // val x2 = key_values[3]
        // splitBboxData: [x1, x2, y1, y2]

        // x_min: 1897
        // y_min: 51.8

        // real: [1897, 52, 28, 30]

        // return mutableListOf(
        //     predictionBboxData[0],
        //     predictionBboxData[1],
        //     predictionBboxData[2],
        //     predictionBboxData[3]
        // )

        return mutableListOf(
            predictionBboxData[0],
            predictionBboxData[1],
            predictionBboxData[2],
            predictionBboxData[3]
        )

        // return mutableListOf(
        //     espejoYResultado[0],
        //     espejoYResultado[1],
        //     espejoYResultado[2],
        //     espejoYResultado[3]
        // )
    }


    fun plotPredictedODAnnotationsDataForAndroid(filteredAnnotations: List<GroupedAnnotation>, bitmap: Bitmap, labels: List<String>):FinalResult {

        val dsmkdmskmkmsmds = "dsmkdmskmkmsmds"
        println("dsmkdmskmkmsmds: $dsmkdmskmkmsmds")
        val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)

        val total_predictions = (filteredAnnotations as? Collection<*>)?.size ?: 0


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
                    // Puedes descomentar la línea siguiente si deseas dibujar también el texto
                    // canvas.drawText(labels[my_category_id] + " " + my_score.toString(), custom_left, custom_top, paint)
                }
            }

            val finalBitmap = resizeBitmapByPercentage(mutable, 0.4f)
            //imageView.setImageBitmap(resizedBitmap)

//            imageView.setImageBitmap(mutable)

            // Actualizar el texto del textView
            var finalText = "$total_predictions detected"

            // toast = Toast.makeText(applicationContext, "Se ha detectado objeto(s) exitosamente.", Toast.LENGTH_LONG)

            return FinalResult(finalBitmap, total_predictions)
        } else {
            // La variable total_predictions es igual a cero
            println("No hay predicciones disponibles.")
            // Puedes poner aquí cualquier otra acción que desees realizar cuando total_predictions sea igual a cero
            toast = Toast.makeText(activity, "No se ha detectado ningún objeto.", Toast.LENGTH_LONG)
            throw Exception("NO SE ENCONTRARON NADA")
        }

        val timer = object : CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                toast.show()
            }

            override fun onFinish() {
                toast.cancel()
            }
        }


        timer.start()

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


    private fun filterGroupedAnnotationsDataV2(annotationsData: List<GroupedAnnotation>): List<GroupedAnnotation> {
        val filteredAnnotationsData = mutableListOf<GroupedAnnotation>()

        for (iIdx in annotationsData.indices) {
            val categories = mutableMapOf<Int, MutableMap<String, Any>>()

            for ((jIdx, score) in annotationsData[iIdx].scores.withIndex()) {
                val centroid = annotationsData[iIdx].centroids[jIdx]
                val bbox = annotationsData[iIdx].bboxs[jIdx] as MutableList<Float>
                val categoryId = annotationsData[iIdx].categoryIds[jIdx]

                if (categoryId !in categories) {
                    categories[categoryId] = mutableMapOf(
                        "count" to 1,
                        "locations" to mutableListOf(jIdx)
                    )
                } else {
                    categories[categoryId]!!["count"] = (categories[categoryId]!!["count"] as Int) + 1
                    (categories[categoryId]!!["locations"] as MutableList<Int>).add(jIdx)
                }
            }

            var datosJIdxOrdenados = listOf<CustomDataJIdx>()

            for ((category, info) in categories) {
                val sumCentroidsOfCategory = floatArrayOf(0f, 0f)

                for (jIdx in info["locations"] as List<Int>) {
                    val centroid = annotationsData[iIdx].centroids[jIdx]
                    sumCentroidsOfCategory[0] += centroid[0]
                    sumCentroidsOfCategory[1] += centroid[1]
                }

                val averageCentroidOfCategory = sumCentroidsOfCategory.map { it / (info["locations"] as List<Int>).size }

                var bestScoreJIdx = 0.0f
                var locationBestScoreJIdx = 0

                for (jIdx in info["locations"] as List<Int>) {
                    val score = annotationsData[iIdx].scores[jIdx]

                    if (score > bestScoreJIdx) {
                        bestScoreJIdx = score
                        locationBestScoreJIdx = jIdx
                    }
                }

                val referenceBox = moveBoxToCentroid(annotationsData[iIdx].bboxs[locationBestScoreJIdx].toMutableList(), averageCentroidOfCategory)

                // Inicializar datos como una lista vacía mutable
                val datosJIdx = mutableListOf<CustomDataJIdx>()

                for (jIdx in info["locations"] as List<Int>) {
                    val bbox_2 = annotationsData[iIdx].bboxs[jIdx]
                    val boxIOU = calculateBoxIoU(referenceBox, bbox_2)

                    // Agregar datos a la lista
                    datosJIdx += CustomDataJIdx(jIdx, annotationsData[iIdx].scores[jIdx], boxIOU.toFloat())
                }

                datosJIdxOrdenados = ordenarDataJIdx(datosJIdx)

            }

            val newGroup = GroupedAnnotation(
                scores = mutableListOf(annotationsData[iIdx].scores[datosJIdxOrdenados.first().jIdx]),
                centroids = mutableListOf(annotationsData[iIdx].centroids[datosJIdxOrdenados.first().jIdx]),
                bboxs = mutableListOf(annotationsData[iIdx].bboxs[datosJIdxOrdenados.first().jIdx]),
                categoryIds = mutableListOf(annotationsData[iIdx].categoryIds[datosJIdxOrdenados.first().jIdx])
            )
            filteredAnnotationsData.add(newGroup)

        }

        return filteredAnnotationsData
    }

    fun ordenarDataJIdx(datos: List<CustomDataJIdx>): List<CustomDataJIdx> {
        // Ordenar los datos por score y luego por boxIOU de forma descendente
        return datos.sortedWith(compareByDescending<CustomDataJIdx> { it.score }.thenByDescending { it.boxIOU })
    }

    fun moveBoxToCentroid(box: List<Float>, centroidRef: List<Float>): List<Float> {
        val xMin = box[0]
        val yMin = box[1]
        val xMax = box[2]
        val yMax = box[3]

        val oldBoxCentroid = listOf((yMin + yMax) / 2, (xMin + xMax) / 2)
        val displacement = listOf(centroidRef[0] - oldBoxCentroid[0], centroidRef[1] - oldBoxCentroid[1])

        return listOf(xMin + displacement[1], yMin + displacement[0], xMax + displacement[1], yMax + displacement[0])
    }

    fun calculateBoxIoU(box1: List<Float>, box2: List<Float>): Float {
        val xMin1 = box1[0]
        val yMin1 = box1[1]
        val xMax1 = box1[2]
        val yMax1 = box1[3]

        val xMin2 = box2[0]
        val yMin2 = box2[1]
        val xMax2 = box2[2]
        val yMax2 = box2[3]

        val interXMin = maxOf(xMin1, xMin2)
        val interYMin = maxOf(yMin1, yMin2)
        val interXMax = minOf(xMax1, xMax2)
        val interYMax = minOf(yMax1, yMax2)

        val interArea = maxOf(0f, interXMax - interXMin + 1) * maxOf(0f, interYMax - interYMin + 1)
        val areaBox1 = (xMax1 - xMin1 + 1) * (yMax1 - yMin1 + 1)
        val areaBox2 = (xMax2 - xMin2 + 1) * (yMax2 - yMin2 + 1)

        val unionArea = areaBox1 + areaBox2 - interArea

        return if (unionArea == 0f) 0f else interArea / unionArea
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

    data class CustomDataJIdx(val jIdx: Int, val score: Float, val boxIOU: Float)
}