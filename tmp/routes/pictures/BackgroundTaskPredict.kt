package com.odrigo.recognitionappkt.routes.pictures

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.odrigo.recognitionappkt.algorithms.Detect320x320
import com.odrigo.recognitionappkt.domain.Picture
import com.odrigo.recognitionappkt.drivers.BitmapManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class BackgroundTaskPredict(activity: AppCompatActivity) {

    var isProcessing = false
        private set

    private var processingIndex = 0
    private var processingRateProgress = 0
    private var processingList:List<Picture> = mutableListOf<Picture>()

    private lateinit var updateStatus: (status: Int) -> Unit
    private lateinit var updateEntity: (id: Long, counter: Int, bitmapPath: String, callBack: () -> Unit) -> Unit
    private lateinit var finish: () -> Unit

    private lateinit var my: AppCompatActivity
    private var model = Detect320x320(activity)

    init {
        my = activity
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun predictBatchCOROUTINE(
        pictures: List<Picture>,
        updateCallback: (status: Int) -> Unit,
        updateEntityCallback: (id: Long, counter: Int, bitmapPath: String, callBack: () -> Unit) -> Unit,
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
        if(processingIndex >= processingList.size) {
            finishPrediction()
            return
        }

        var currentItem = processingList[processingIndex]
        var bitmap = BitmapManager.getBitmapFromPath(currentItem.filePath)
            ?:  throw IllegalArgumentException("BITMAP_NOT_FOUND: $processingIndex")

        predictBitmapCOROUTINE(bitmap) {
            processedBitmap, counter, processedFile -> run {
                var processedFilePath =
                    BitmapManager.saveBitmapToStorage(my, processedBitmap, processedFile)
                    ?: throw IllegalArgumentException("FILE_PROCESSED_NOT_SAVED: $processingIndex")

                processingIndex++
                if(processingIndex != processingList.size - 1) {
                    updateStatus(processingRateProgress * processingIndex)
                }
                updateEntity(currentItem.id, counter, processedFilePath) {
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
    private fun predictBitmapCOROUTINE(bitmap: Bitmap, callback: (bitmap: Bitmap, counter: Int, fileName: String) -> Unit) {
        GlobalScope.launch {
            var result = model.iniciarProcesoGlobalPrediction(
                bitmap,
                splitWidth = 300,
                splitHeight = 300,
                overlap = 0.4f,
                miBatchSize = 4,
                miCustomConfidenceThreshold = 0.5,
                distanceThreshold = 10f
            )
            withContext(Dispatchers.Main) {
                val uuid: UUID = UUID.randomUUID()
                val uuidString: String = uuid.toString()
                val filename = "$uuidString-processed.png"
                callback(result.finalBitmap, result.counter, filename)
            }
        }
    }
}