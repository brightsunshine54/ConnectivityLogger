package com.filantrop.connectivitylogger.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.ext.SdkExtensions
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val AVAILABLE = "available"
private const val LOST = "lost"
private const val CHANGED = "changed"
private const val LOG_FILE_NAME = "network_log.txt"

class ConnectivityNetworkCallback(
    private val connectivityManager: ConnectivityManager,
    private val context: Context
) :
    ConnectivityManager.NetworkCallback() {
    private val simpleDateFormat: SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())


    override fun onAvailable(network: Network) {
        super.onAvailable(network)

        Log.i(TAG, "onAvailable(): Thread.id: ${Thread.currentThread().id}")

        connectivityManager.getNetworkCapabilities(network)?.let { capabilities ->
            val connectionType = getConnectionType(capabilities)
            val isActive =
                if (connectivityManager.activeNetwork?.networkHandle == network.networkHandle) true
                else false
            val hasInternet =
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            logToFile(
                logMessage(
                    AVAILABLE,
                    network.networkHandle,
                    connectionType,
                    isActive,
                    hasInternet,
                    getIP4Address()
                )
            )
        }
    }

    override fun onLost(network: Network) {
        super.onLost(network)

        Log.i(TAG, "onLost(): Thread.id: ${Thread.currentThread().id}")

        connectivityManager.getNetworkCapabilities(network)?.let { capabilities ->
            val connectionType = getConnectionType(capabilities)
            val isActive =
                if (connectivityManager.activeNetwork?.networkHandle == network.networkHandle) true
                else false
            val hasInternet =
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            logToFile(
                logMessage(
                    LOST,
                    network.networkHandle,
                    connectionType,
                    isActive,
                    hasInternet,
                    getIP4Address()
                )
            )
        }
    }

    override fun onCapabilitiesChanged(
        network: Network,
        capabilities: NetworkCapabilities
    ) {
        super.onCapabilitiesChanged(network, capabilities)

        Log.i(TAG, "onCapabilitiesChanged(): Thread.id: ${Thread.currentThread().id}")

        val connectionType = getConnectionType(capabilities)
        val isActive =
            if (connectivityManager.activeNetwork?.networkHandle == network.networkHandle) true
            else false
        val hasInternet =
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        logToFile(
            logMessage(
                CHANGED,
                network.networkHandle,
                connectionType,
                isActive,
                hasInternet,
                getIP4Address()
            )
        )
    }

    private fun logMessage(
        event: String,
        id: Long,
        connectionType: String,
        isActive: Boolean,
        hasInternet: Boolean,
        ipAddress: String
    ) =
        "Network $event! id: $id, type: $connectionType, active: $isActive Internet: $hasInternet, IP: $ipAddress\n"

    private fun getConnectionType(capabilities: NetworkCapabilities) = when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_SATELLITE) -> "Satellite"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_USB) -> "USB"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) -> "Wi-Fi Aware"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN) -> "LoWPAN"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "Bluetooth"
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
        else -> "Unknown"
    }

    private fun getIP4Address(): String {
        return try {
            val networkInterfaces = java.net.NetworkInterface.getNetworkInterfaces()
            for (networkInterface in networkInterfaces) {
                for (inetAddress in networkInterface.inetAddresses) {
                    if (!inetAddress.isLoopbackAddress && inetAddress is java.net.Inet4Address) {
                        return inetAddress.hostAddress
                    }
                }
            }
            "Unknown"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun logToFile(message: String) {
        val file = File(context.filesDir, LOG_FILE_NAME)
        Log.d(TAG, "logFile: ${file.absoluteFile} message: $message")
        try {
            FileWriter(file, true).use { writer ->
                writer.write("[${getCurrentTime()}]: ${message}\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("FileLogger", "Error writing to log file: ${e.message}")
        }
    }

    private fun getCurrentTime(): String {
        return simpleDateFormat.format(Date())
    }

    companion object {
        fun createNetworkRequest(): NetworkRequest {
            val builder = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_USB)
                .addTransportType(NetworkCapabilities.TRANSPORT_BLUETOOTH)
                .addTransportType(NetworkCapabilities.TRANSPORT_LOWPAN)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)

            if (SdkExtensions.getExtensionVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) >= 12) {
                builder.addTransportType(NetworkCapabilities.TRANSPORT_SATELLITE)
            }

            return builder.build()
        }

        private val TAG = ConnectivityNetworkCallback::class.java.canonicalName
    }
}