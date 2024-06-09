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
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.domain.Constants
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.infraestructure.DbBuilder
import com.rodrigo.deeplarva.infraestructure.driver.AppDatabase
import com.rodrigo.deeplarva.routes.PicturesActivity
import com.rodrigo.deeplarva.routes.services.BoxDetectionServices
import com.rodrigo.deeplarva.routes.services.PicturesServices
import com.rodrigo.deeplarva.ui.tasks.BackgroundTaskPredict

class PredictionService: Service() {

    private val TAG = "DEEP_LARVA::PredictionService"

    var isRunning = false
    private val binder = LocalBinder()
    private val sender = PredictionBroadcastSender(this)

    private var backgroundTask = BackgroundTaskPredict(this)

    private lateinit var db: AppDatabase
    private lateinit var pictureService: PicturesServices
    private lateinit var boxDetectionServices: BoxDetectionServices

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Created")
        createNotificationChannel()

        db = DbBuilder.getInstance(this)

        pictureService = PicturesServices(db)
        boxDetectionServices = BoxDetectionServices(db)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service Started")
        isRunning = true

        val notificationIntent = Intent(this, PicturesActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("DeepLarva")
            .setContentText("Processing pictures")
            .setSmallIcon(R.drawable.deep_larva_icon)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(Constants.NOTIFICATION_ID, notification)

        eventPredict()

        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun eventPredict() {
        if(backgroundTask.isProcessing){
            return
        }
        pictureService.findUnprocessed {
                pictures ->
            if (pictures.isNotEmpty()){
                sender.notify(0)
                backgroundTask.predictBatchCOROUTINE(
                    pictures,
                    ::eventUpdatePredictionProgress,
                    ::eventEntityPredictionProgress,
                    ::eventFinishPrediction
                )
            } else {
                eventFinishPrediction()
            }
        }
    }



    private fun eventUpdatePredictionProgress(status: Int) {
        if(status == 100) return
        sender.notify(status)
    }

    private fun eventEntityPredictionProgress(id: Long, counter: Int, boxes: List<List<Float>>, time: Long, bitmapProcessedPath: String, callback: () -> Unit) {
        pictureService.findOne(id) {
            if (it == null) return@findOne
            boxDetectionServices.saveBulk(it.id, boxes) {
                pictureService.update(
                    Picture(
                        id = it.id,
                        deviceId = it.deviceId,
                        count = counter,
                        filePath = it.filePath,
                        hasMetadata = true,
                        processedFilePath = bitmapProcessedPath,
                        time = time,
                        thumbnailPath = it.thumbnailPath,
                        timestamp = it.timestamp
                    )
                ) {
                    callback()
                }
            }
        }
    }

    private fun eventFinishPrediction() {
        sender.notify(100)
        this.onDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service Destroyed")

        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
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