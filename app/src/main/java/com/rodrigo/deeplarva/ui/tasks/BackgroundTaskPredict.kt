package com.rodrigo.deeplarva.ui.tasks

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.deeplarva.domain.Constants

import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.ml.Detect320x320
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

class BackgroundTaskPredict(activity: Context) {

    var isProcessing = false
        private set

    private var processingIndex = 0
    private var processingRateProgress = 0
    private var processingList:List<Picture> = mutableListOf<Picture>()

    private lateinit var updateStatus: (status: Int) -> Unit
    private lateinit var updateEntity: (id: Long, counter: Int, time: Long, bitmapPath: String, callBack: () -> Unit) -> Unit
    private lateinit var finish: (id: Long) -> Unit

    private lateinit var my: Context
    private var model = Detect640x640(activity)

    init {
        my = activity
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun predictBatchCOROUTINE(
        subSampleId: Long,
        pictures: List<Picture>,
        updateCallback: (status: Int) -> Unit,
        updateEntityCallback: (id: Long, counter: Int, time: Long, bitmapPath: String, callBack: () -> Unit) -> Unit,
        finishCallback: (id: Long) -> Unit
    ) {
        isProcessing = true
        updateStatus = updateCallback
        updateEntity = updateEntityCallback
        finish = finishCallback

        processingIndex = 0
        processingRateProgress = 100 / pictures.size
        processingList = pictures

        recursivePredictionCOROUTINE(subSampleId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun recursivePredictionCOROUTINE(subSampleId: Long) {
        if(processingIndex >= processingList.size) {
            finishPrediction(subSampleId)
            return
        }

        var currentItem = processingList[processingIndex]
        var bitmap = BitmapUtils.getBitmapFromPath(currentItem.filePath)
            ?:  throw IllegalArgumentException("BITMAP_NOT_FOUND: $processingIndex")

        predictBitmapCOROUTINE(bitmap) {
                processedBitmap, counter, processedFile, time -> run {
            var processedFilePath = if(processedBitmap != null) {
                // TODO: Guardar en galeria
                val imageFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "deep-larva")
                if (!imageFolder.exists()) {
                    imageFolder.mkdirs()
                }

                val fileName = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(
                    Date()
                ) + ".jpg"
                val imageFile = File(imageFolder, fileName)
                FileOutputStream(imageFile)
                // TODO: Guardar en galeria
                BitmapUtils.saveBitmapToStorage(my, processedBitmap, processedFile)
                    ?: throw IllegalArgumentException("FILE_PROCESSED_NOT_SAVED: $processingIndex")
            } else {
                ""
            }

            processingIndex++
            if(processingIndex != processingList.size - 1) {
                updateStatus(processingRateProgress * processingIndex)
            }
            updateEntity(currentItem.id, counter, time, processedFilePath) {
                recursivePredictionCOROUTINE(subSampleId)
            }
        }}
    }

    private fun finishPrediction(subSampleId: Long) {
        isProcessing = false
        processingIndex = 0
        finish(subSampleId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun predictBitmapCOROUTINE(bitmap: Bitmap, callback: (bitmap: Bitmap?, counter: Int, fileName: String, time: Long) -> Unit) {
        val startTimeMillis = System.currentTimeMillis()
        GlobalScope.launch {
            var result = model.iniciarProcesoGlobalPrediction(
                bitmap,
                splitWidth = 640,
                splitHeight = 640,
                overlap = 0.8f,
                miBatchSize = 6,
                miCustomConfidenceThreshold = 0.28F,
                miCustomIoUThreshold = 0.80F,
                distanceThreshold = 10.0f
            )
            val endTimeMillis = System.currentTimeMillis()
            val totalTime = endTimeMillis - startTimeMillis
            withContext(Dispatchers.Main) {
                val uuid: UUID = UUID.randomUUID()
                val uuidString: String = uuid.toString()
                val filename = "$uuidString-processed.${Constants.IMAGE_EXTENSION}"
                callback(result.finalBitmap, result.counter, filename, totalTime)
            }
        }
    }
}