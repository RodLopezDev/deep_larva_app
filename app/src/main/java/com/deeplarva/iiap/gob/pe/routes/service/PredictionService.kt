package com.deeplarva.iiap.gob.pe.routes.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.deeplarva.iiap.gob.pe.R
import com.deeplarva.iiap.gob.pe.domain.constants.AppConstants
import com.deeplarva.iiap.gob.pe.domain.entity.Picture
import com.deeplarva.iiap.gob.pe.infraestructure.internal.driver.AppDatabase
import com.deeplarva.iiap.gob.pe.infraestructure.internal.driver.DbBuilder
import com.deeplarva.iiap.gob.pe.infraestructure.services.BoxDetectionServices
import com.deeplarva.iiap.gob.pe.infraestructure.services.PicturesServices
import com.deeplarva.iiap.gob.pe.modules.prediction.BackgroundTaskPredict
import com.deeplarva.iiap.gob.pe.routes.activity.main.PicturesActivity
import com.deeplarva.iiap.gob.pe.routes.service.broadcast.PredictionBroadcastSender

class PredictionService: Service() {
    private val TAG = "DEEP_LARVA::PredictionService"

    var pictureId: Long? = null

    private val binder = PredictionServiceBinder()
    private val sender = PredictionBroadcastSender(this)

    private var backgroundTask = BackgroundTaskPredict(this)

    private lateinit var db: AppDatabase
    private lateinit var pictureService: PicturesServices
    private lateinit var boxDetectionServices: BoxDetectionServices

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Created")
        createNotificationChannel()

        db = DbBuilder.getInstance(this)

        pictureService = PicturesServices(db)
        boxDetectionServices = BoxDetectionServices(db)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service Destroyed")
        pictureId = null
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    @SuppressLint("ForegroundServiceType")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pictureId = intent!!.getLongExtra("pictureId", 0) ?: 0
        if(pictureId.toInt() == 0){
            this.onDestroy()
            return START_STICKY
        }
        if(this.pictureId != null){
            this.onDestroy()
            return START_STICKY
        }

        Log.d(TAG, "Service Started")
        this.pictureId = pictureId
        val notificationIntent = Intent(this, PicturesActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, AppConstants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("DeepLarva")
            .setContentText("Processing pictures")
            .setSmallIcon(R.drawable.deep_larva_icon)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(AppConstants.NOTIFICATION_ID, notification)
        } else {
            startForeground(AppConstants.NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        }

        eventPredict(pictureId)

        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun eventPredict(pictureId: Long) {
        if(backgroundTask.isProcessing){
            return
        }
        pictureService.findOne(pictureId) { picture ->
            if (picture == null){
                eventFinishPrediction(pictureId)
                return@findOne
            }
            sender.notify(pictureId, 0)
            backgroundTask.predictBatchCOROUTINE(
                pictureId,
                listOf(picture),
                ::eventUpdatePredictionProgress,
                ::eventEntityPredictionProgress,
                ::eventFinishPrediction
            )
        }
    }

    private fun eventUpdatePredictionProgress(pictureId: Long, status: Int) {
        if(status == 100) return
        sender.notify(pictureId, status)
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
                        timestamp = it.timestamp,
                        syncWithCloud = false,
                        uuid = it.uuid
                    )
                ) {
                    callback()
                }
            }
        }
    }

    private fun eventFinishPrediction(pictureId: Long) {
        sender.notify(pictureId, 100)
        this@PredictionService.onDestroy()
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AppConstants.NOTIFICATION_CHANNEL_ID,
                "My Background Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    inner class PredictionServiceBinder : Binder() {
        fun getService(): PredictionService = this@PredictionService
    }
}