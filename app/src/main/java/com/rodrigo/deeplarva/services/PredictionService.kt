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
import androidx.core.app.NotificationCompat
import com.rodrigo.deeplarva.R
import com.rodrigo.deeplarva.routes.PicturesActivity
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

    override fun onBind(intent: Intent?): IBinder? {
        var subSampleId = intent?.getLongExtra("subSampleId", 0)
        Log.d(TAG, "onBind: ${subSampleId}")
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service Started")
        var subSampleId = intent?.getLongExtra("subSampleId", 0)
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

        sender.notify(1)
        GlobalScope.launch {
            delay(2000L)
            sender.notify(30)
            delay(2000L)
            sender.notify(60)
            delay(2000L)
            sender.notify(70)
            delay(10000L)
            sender.notify(90)
            withContext(Dispatchers.Main) {
                this@PredictionService.onDestroy()
            }
        }

        // If we get killed, after returning from here, restart
        return START_STICKY
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