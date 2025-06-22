package com.filantrop.connectivitylogger

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.filantrop.connectivitylogger.model.ConnectivityViewModel
import com.filantrop.connectivitylogger.service.ConnectivityLoggerService
import com.filantrop.connectivitylogger.ui.theme.ConnectivityLoggerTheme
import com.filantrop.connectivitylogger.ui.theme.SpecialGreen
import com.filantrop.connectivitylogger.ui.theme.SpecialRed
import com.filantrop.connectivitylogger.utils.FileSharingHelper
import java.io.File
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.pow


class MainActivity : ComponentActivity() {
    private val viewModel by lazy { ViewModelProvider(this)[ConnectivityViewModel::class.java] }

    private var serviceBinder: ConnectivityLoggerService.ServiceBinder? = null
    private val serviceConnection = object : android.content.ServiceConnection {
        override fun onServiceConnected(
            name: android.content.ComponentName?, binder: android.os.IBinder?
        ) {
            serviceBinder = binder as ConnectivityLoggerService.ServiceBinder
            viewModel.bindService(serviceBinder!!.getService())
        }

        override fun onServiceDisconnected(name: android.content.ComponentName?) {
            viewModel.unbindService(serviceBinder)
            serviceBinder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ConnectivityLoggerTheme {
                AllTogether(viewModel)
            }

        }

        bindService(
            Intent(
                this,
                ConnectivityLoggerService::class.java
            ).setAction(ConnectivityLoggerService.ON_BIND),
            serviceConnection,
            BIND_AUTO_CREATE
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(serviceConnection)
    }
}

@Composable
private fun AllTogether(
    connectivityViewModel: ConnectivityViewModel
) {
    Column {
        ControlSwitch(connectivityViewModel)
        FileListScreen(connectivityViewModel)
    }
}

@Composable
private fun ControlSwitch(
    connectivityViewModel: ConnectivityViewModel
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val serviceRunning by connectivityViewModel.serviceState.collectAsState()

        Text(
            text = if (serviceRunning) "Service running" else "Service not running",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp, top = 24.dp)
        )
        Switch(
            checked = serviceRunning,
            onCheckedChange = {},
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = {
                connectivityViewModel.startStopService(context)
            },
            modifier = Modifier.padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!serviceRunning) SpecialGreen else SpecialRed,
                contentColor = if (!serviceRunning) Color.White else Color.Black
            ),
        ) {
            Text(
                text = if (serviceRunning) "Stop" else "Start",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
fun FileListScreen(viewModel: ConnectivityViewModel) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.Gray),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(viewModel.files, key = { it.name }) { file ->
                FileListItem(file, viewModel, context)
            }
        }
    }
}

@Composable
fun FileListItem(
    file: ConnectivityViewModel.FileItem,
    viewModel: ConnectivityViewModel,
    context: Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(text = file.name, fontWeight = FontWeight.Bold)
                Text(text = "Size: ${formatFileSize(file.size)}")
                Row {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpecialRed
                        ),
                        onClick = {
                            // todo: add dialog with request for confirmation
                            viewModel.deleteFile(file)
                        }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            FileSharingHelper.shareFile(context, file.file)
                        }) {
                        Icon(Icons.Default.Share, "SHARE")
                    }
                }
            }
        }
    }
}

fun formatFileSize(sizeInBytes: Long, decimals: Int = 2): String {
    if (sizeInBytes <= 0) return "0 B"

    val units = listOf("B", "KB", "MB", "GB", "TB")
    val base = 1024
    val exponent = floor(log(sizeInBytes.toDouble(), base.toDouble()))
    val unitIndex = exponent.coerceAtMost(units.size - 1.0).toInt()
    val convertedSize = sizeInBytes / base.toDouble().pow(unitIndex)

    val fixedDecimals = if (unitIndex > 0) decimals else 0
    return "%.${fixedDecimals}f %s".format(convertedSize, units[unitIndex])
}

@Preview
@Composable
fun PreviewFileListItem() {
    val viewModel = ConnectivityViewModel(Application())
    ConnectivityLoggerTheme {
        FileListItem(
            ConnectivityViewModel.FileItem(
                "network-log_2025-06-23_03-28-16",
                100L,
                File("")
            ),
            viewModel,
            LocalContext.current
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewControlSwitch() {
    val viewModel = ConnectivityViewModel(Application())
    ConnectivityLoggerTheme {
        ControlSwitch(viewModel)
    }
}