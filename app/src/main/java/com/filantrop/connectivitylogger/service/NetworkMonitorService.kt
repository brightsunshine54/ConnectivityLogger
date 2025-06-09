package com.filantrop.connectivitylogger.service

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NetworkMonitorService {

/*    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                val networkInfo = connectivityManager.getNetworkCapabilities(network)
                val connectionType = when {
                    networkInfo?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
                    networkInfo?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Мобильная сеть"
                    else -> "Другая сеть"
                }

                val isConnected = isInternetAvailable()
                val ipAddress = getIPAddress()

                val message = "[${getCurrentTime()}] Сеть доступна: $connectionType, " +
                        "Интернет: ${if (isConnected) "Да" else "Нет"}, IP: $ipAddress\n"

                logToFile(message)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                val message = "[${getCurrentTime()}] Сеть потеряна\n"
                logToFile(message)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val connectionType = when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Мобильная сеть"
                    else -> "Другая сеть"
                }

                val isConnected = isInternetAvailable()
                val ipAddress = getIPAddress()

                val message = "[${getCurrentTime()}] Изменение сети: $connectionType, " +
                        "Интернет: ${if (isConnected) "Да" else "Нет"}, IP: $ipAddress\n"

                logToFile(message)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    private fun isInternetAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun getIPAddress(): String {
        return try {
            val networkInterfaces = java.net.NetworkInterface.getNetworkInterfaces()
            for (networkInterface in networkInterfaces) {
                for (inetAddress in networkInterface.inetAddresses) {
                    if (!inetAddress.isLoopbackAddress && inetAddress is java.net.Inet4Address) {
                        return inetAddress.hostAddress
                    }
                }
            }
            "Неизвестен"
        } catch (e: Exception) {
            "Ошибка: ${e.message}"
        }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

*//*    private fun logToFile(message: String) {
        try {
            val file = File(getExternalFilesDir(null), "network_log.txt")
            Log.d(getTag(), "filepath: ${file.absoluteFile}")
            FileOutputStream(file, true).use {
                it.write(message.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }*//*

    private fun getTag(): String? {
        return this.javaClass.canonicalName
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Монитор сети",
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = "Сервис мониторинга сетевых событий"
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Монитор сети")
            .setContentText("Запись сетевых событий...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    companion object {
        private const val CHANNEL_ID = "network_monitor_channel"
        private const val NOTIFICATION_ID = 1

        fun startService(context: Context) {
            val intent = Intent(context, NetworkMonitorService::class.java)
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, NetworkMonitorService::class.java)
            context.stopService(intent)
        }
    }*/
}