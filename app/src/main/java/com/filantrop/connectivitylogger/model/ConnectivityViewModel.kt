package com.filantrop.connectivitylogger.model

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.filantrop.connectivitylogger.service.ConnectivityLoggerService
import com.filantrop.connectivitylogger.service.LOG_FILE_NAME_PATTERN
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class ConnectivityViewModel(application: Application) :
    AndroidViewModel(application) {
    private val _serviceState = MutableStateFlow(false)
    val serviceState: StateFlow<Boolean> = _serviceState

    fun bindService(service: ConnectivityLoggerService) {
        service.running.observeForever { newData ->
            Log.d(TAG, "service.running: $newData")
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

    private val _files = mutableStateListOf<FileItem>()
    val files: List<FileItem> = _files

    init {
        viewModelScope.launch {
            _files.clear()
            _files.addAll(getFilesWithSizes(application.applicationContext))
        }

        //todo replace on listening service state (refresh on change)
        viewModelScope.launch {
            while (true) {
                _files.clear()
                _files.addAll(getFilesWithSizes(application.applicationContext))
                delay(3000)
            }
        }
    }

    fun deleteFile(file: FileItem): Boolean {
        return try {
            val fileToDelete = file.file
            if (fileToDelete.exists() && fileToDelete.delete()) {
                _files.remove(file)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun getFilesWithSizes(context: Context): List<FileItem> {
        val filesDir = context.filesDir
        return filesDir.listFiles()?.filter { it.name.startsWith(LOG_FILE_NAME_PATTERN) }
            ?.map { file ->
                FileItem(
                    name = file.name,
                    size = file.length(),
                    file = file
                )
            } ?: emptyList()
    }

    data class FileItem(val name: String, val size: Long, val file: File)

    private fun startService(context: Context) {
        Log.d(TAG, "Requesting service start")
        try {
            context.startService(getServiceIntent(context, ConnectivityLoggerService.ACTION_START))
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service", e)
        }
    }

    private fun stopService(context: Context) {
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
