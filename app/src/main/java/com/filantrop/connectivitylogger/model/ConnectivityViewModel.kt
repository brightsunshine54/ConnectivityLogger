package com.filantrop.connectivitylogger.model

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filantrop.connectivitylogger.service.ConnectivityLoggerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConnectivityViewModel : ViewModel() {
    private val _serviceState = MutableStateFlow(false)
    val serviceState: StateFlow<Boolean> = _serviceState

    fun bindService(service: ConnectivityLoggerService) {
        service.running.observeForever { newData ->
            Log.d(TAG, "service.running: ${newData}")
            viewModelScope.launch {
                _serviceState.value = newData
            }
        }
    }

    fun unbindService(serviceBinder: ConnectivityLoggerService.ServiceBinder?) {
        if (serviceBinder != null) {
            serviceBinder.getService().running.value = false
        }
    }

    fun startService(context: Context) {
        Log.d(TAG, "Requesting service start")
        try {
            context.startService(getServiceIntent(context, ConnectivityLoggerService.ACTION_START))
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service", e)
        }
    }

    fun stopService(context: Context) {
        Log.d(TAG, "Requesting service stop")
        try {
            context.startService(getServiceIntent(context, ConnectivityLoggerService.ACTION_STOP))
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping service", e)
        }
    }

    private fun getServiceIntent(context: Context, action: String): Intent {
        val intent = Intent(context, ConnectivityLoggerService::class.java)
        intent.action = action
        return intent
    }


    fun startStopService(context: Context) {
        Log.d(TAG, "startStopService() Thread.id: ${Thread.currentThread().id}")

        if (_serviceState.value) {
            stopService(context)
        } else {
            startService(context)
        }
    }

    companion object {
        private val TAG = ConnectivityViewModel::class.java.canonicalName
    }
}
