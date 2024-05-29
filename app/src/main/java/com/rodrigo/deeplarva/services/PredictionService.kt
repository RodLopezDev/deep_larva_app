package com.rodrigo.deeplarva.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.domain.entity.SubSample
import com.rodrigo.deeplarva.infraestructure.DbBuilder
import com.rodrigo.deeplarva.infraestructure.driver.AppDatabase
import com.rodrigo.deeplarva.routes.PicturesActivity
import com.rodrigo.deeplarva.routes.services.PicturesServices
import com.rodrigo.deeplarva.routes.services.SubSampleServices
import com.rodrigo.deeplarva.ui.tasks.BackgroundTaskPredict
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PredictionService: Service() {

    private val CHANNEL_ID = "MyServiceChannel"
    private val NOTIFICATION_ID = 1
    private val TAG = "DEEP_LARVA::PredictionService"

    var isRunning = false
    private lateinit var listener: OnServiceListener
    private val binder = LocalBinder()
    private val sender = PredictionBroadcastSender(this)


    private var backgroundTask = BackgroundTaskPredict(this)


    private lateinit var db: AppDatabase
    private lateinit var pictureService: PicturesServices
    private lateinit var subSampleService: SubSampleServices

    override fun onBind(intent: Intent?): IBinder? {
        var subSampleId = intent?.getLongExtra("subSampleId", 0)
        Log.d(TAG, "onBind: ${subSampleId}")
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Created")
        createNotificationChannel()

        db = DbBuilder.getInstance(this)

        pictureService = PicturesServices(db)
        subSampleService = SubSampleServices(db)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service Started")
        var subSampleId = intent?.getLongExtra("subSampleId", 0) ?: 0
        Log.d(TAG, "onStartCommand: ${subSampleId}")
        if(subSampleId?.toInt() == 0){
            this.onDestroy()
            return START_STICKY
        }
        isRunning = true
        this.listener?.onStartService()

        val notificationIntent = Intent(this, PicturesActivity::class.java)
        notificationIntent.putExtra("subSampleId", subSampleId)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DeepLarva")
            .setContentText("Service is running... $subSampleId")
            .setSmallIcon(R.drawable.deep_larva_icon)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
//
//        sender.notify(1)
//        GlobalScope.launch {
//            delay(2000L)
//            sender.notify(30)
//            delay(2000L)
//            sender.notify(60)
//            delay(2000L)
//            sender.notify(70)
//            delay(10000L)
//            sender.notify(90)
//            withContext(Dispatchers.Main) {
//                this@PredictionService.onDestroy()
//            }
//        }

        eventPredict(subSampleId)

        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun eventPredict(subSampleId: Long) {
        if(backgroundTask.isProcessing){
            Toast.makeText(this, "Process is running yet", Toast.LENGTH_LONG).show()
            return
        }
        pictureService.findUnprocessedBySubSampleId(subSampleId) {
                pictures ->
            if (pictures.isNotEmpty()){
                Toast.makeText(this, "PROCESSING, ${pictures.size}", Toast.LENGTH_LONG).show()
                backgroundTask.predictBatchCOROUTINE(
                    subSampleId,
                    pictures,
                    ::eventUpdatePredictionProgress,
                    ::eventEntityPredictionProgress,
                    ::eventFinishPrediction
                )
            } else {
                eventFinishPrediction(subSampleId)
            }
        }
    }



    private fun eventUpdatePredictionProgress(status: Int) {
        sender.notify(status)
    }

    private fun eventEntityPredictionProgress(id: Long, counter: Int, bitmapProcessedPath: String, callback: () -> Unit) {
        pictureService.findOne(id) {
            if (it != null) {
                pictureService.update(
                    Picture(
                        id = it.id,
                        count = counter,
                        filePath = it.filePath,
                        subSampleId = it.subSampleId,
                        hasMetadata = true,
                        processedFilePath = bitmapProcessedPath,
                        time = 0,
                        thumbnailPath = it.thumbnailPath
                    )
                ) {
                    callback()
                }
            }
        }
    }

    private fun updateSubSampleInfo(subSampleId: Long, callback: () -> Unit) {
        subSampleService.findOne(subSampleId) {
            subSample -> run {
            if(subSample != null) {
                pictureService.findProcessedBySubSampleId(subSampleId) {
                        pictures -> run {
                    if(pictures.isNotEmpty()){
                        val min = pictures.minOf { it.count }
                        val max = pictures.maxOf { it.count }
                        val valuesList = pictures.map { it.count }.distinct()
                        val fashionCounts = valuesList.groupingBy { it }.eachCount()
                        val mostCommonFashion = fashionCounts.maxByOrNull { it.value }?.key

                        val updated = SubSample(id=subSample.id, isTraining = true, min = min.toFloat(), max = max.toFloat(), mean = mostCommonFashion?.toFloat() ?: 0f, average = 0f, name = subSample.name)
                        subSampleService.update(updated) {
                            callback()
                        }
                    }
                }}
            } else {
                callback()
            }
        }}
    }

    private fun eventFinishPrediction(subSampleId: Long) {
        updateSubSampleInfo(subSampleId) {
            this.onDestroy()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service Destroyed")

        isRunning = false
        this.listener?.onFinishService()

        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun addListeners (listener: OnServiceListener) {
        this.listener = listener
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "My Background Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }


    inner class LocalBinder : Binder() {
        fun getService(): PredictionService = this@PredictionService
    }
}