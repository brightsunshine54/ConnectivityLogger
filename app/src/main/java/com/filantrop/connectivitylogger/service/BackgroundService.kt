package com.filantrop.connectivitylogger.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BackgroundService : Service() {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor();

    private val binder: IBinder = ServiceBinder()

    inner class ServiceBinder : Binder() {

        fun getService(): BackgroundService {
            return this@BackgroundService
        }
    }

    private val _running = MutableLiveData(false)
    val running: MutableLiveData<Boolean> get() = _running

    override fun onBind(intent: Intent): IBinder {
        Log.i(TAG, "onBind: ")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "onUnbind: ")
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        Log.i(TAG, "onRebind: ")
        super.onRebind(intent)
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy: ")
    }

    override fun onCreate() {
        Log.i(TAG, "onCreate: ")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand: Thread.id: ${Thread.currentThread().id}")

        when (intent?.action) {
            ACTION_START -> {
                if (running.value == false) {
                    startForeground()
                    startBackgroundWork()
                }
            }

            ACTION_STOP -> {
                stopBackgroundWork()
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForeground() {
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Background Work Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Background Work")
            .setContentText("Service is running")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    private fun startBackgroundWork() {
        running.value = true
        executor.submit {
            var count = 0
            while (running.value == true) {
                Log.i(
                    TAG,
                    "onStartCommand: working... ${count++} Thread.id: ${Thread.currentThread().id}"
                )
                Thread.sleep(1000)
            }
        }
    }

    private fun stopBackgroundWork() {
        running.value = false
    }


    companion object {
        private val TAG = BackgroundService::class.java.canonicalName

        const val CHANNEL_ID = "BackgroundWorkChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

}
