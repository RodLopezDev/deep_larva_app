package com.rodrigo.deeplarva.ui.tasks

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import com.rodrigo.deeplarva.domain.Constants
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.ml.Detect640x640
import com.rodrigo.deeplarva.utils.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BackgroundTaskPredict(private val my: Context) {

    var isProcessing = false
        private set

    private var processingIndex = 0
    private var processingRateProgress = 0
    private var processingList: List<Picture> = mutableListOf()

    private lateinit var updateStatus: (status: Int) -> Unit
    private lateinit var updateEntity: (id: Long, counter: Int, boxes: List<List<Float>>, time: Long, bitmapPath: String, callBack: () -> Unit) -> Unit
    private lateinit var finish: () -> Unit

    private var model = Detect640x640(my)


    @RequiresApi(Build.VERSION_CODES.O)
    fun predictBatchCOROUTINE(
        pictures: List<Picture>,
        updateCallback: (status: Int) -> Unit,
        updateEntityCallback: (id: Long, counter: Int, boxes: List<List<Float>>, time: Long, bitmapPath: String, callBack: () -> Unit) -> Unit,
        finishCallback: () -> Unit
    ) {
        isProcessing = true
        updateStatus = updateCallback
        updateEntity = updateEntityCallback
        finish = finishCallback

        processingIndex = 0
        processingRateProgress = 100 / pictures.size
        processingList = pictures

        recursivePredictionCOROUTINE()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun recursivePredictionCOROUTINE() {
        if (processingIndex >= processingList.size) {
            finishPrediction()
            return
        }

        val currentItem = processingList[processingIndex]
        val bitmap = BitmapUtils.getBitmapFromPath(currentItem.filePath)
            ?: throw IllegalArgumentException("BITMAP_NOT_FOUND: $processingIndex")

        // Extraer el nombre del archivo desde filePath
        val file = File(currentItem.filePath)
        val originalFileName = file.name

        //val originalFileName = currentItem.originalFileName
        println(originalFileName)
        predictBitmapCOROUTINE(my, bitmap, originalFileName) { processedBitmap, counter, boxes, processedFile, time ->
            run {
                val processedFilePath = if (processedBitmap != null) {
                    // TODO: Guardar en galeria
                    val imageFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "deep-larva")
                    if (!imageFolder.exists()) {
                        imageFolder.mkdirs()
                    }

                    val fileName = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date()) + ".jpg"
                    val imageFile = File(imageFolder, fileName)
                    FileOutputStream(imageFile).use {
                        processedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }
                    // TODO: Guardar en galeria
                    BitmapUtils.saveBitmapToStorage(my, processedBitmap, processedFile)
                        ?: throw IllegalArgumentException("FILE_PROCESSED_NOT_SAVED: $processingIndex")
                    imageFile.absolutePath
                } else {
                    ""
                }

                processingIndex++
                if (processingIndex != processingList.size - 1) {
                    updateStatus(processingRateProgress * processingIndex)
                }
                updateEntity(currentItem.id, counter, boxes, time, processedFilePath) {
                    recursivePredictionCOROUTINE()
                }
            }
        }
    }

    private fun finishPrediction() {
        isProcessing = false
        processingIndex = 0
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun predictBitmapCOROUTINE(context: Context, bitmap: Bitmap, fileName: String, callback: (bitmap: Bitmap?, counter: Int, boxes: List<List<Float>>, fileName: String, time: Long) -> Unit) {
        val startTimeMillis = System.currentTimeMillis()
        GlobalScope.launch {
            val result = model.iniciarProcesoGlobalPrediction(
                context,
                bitmap,
                splitWidth = 640,
                splitHeight = 640,
                overlap = 0.75f,
                miCustomConfidenceThreshold = 0.70F,
                miCustomIoUThresholdNMS = 0.3f,
                imageName = fileName // Pasar el nombre del archivo aquí
            )
            val endTimeMillis = System.currentTimeMillis()
            val totalTime = endTimeMillis - startTimeMillis
            withContext(Dispatchers.Main) {
                val uuid: UUID = UUID.randomUUID()
                val uuidString: String = uuid.toString()
                val filename = "$uuidString-processed.${Constants.IMAGE_EXTENSION}"
                callback(result.finalBitmap, result.counter, result.boxes, filename, totalTime)
            }
        }
    }
}
