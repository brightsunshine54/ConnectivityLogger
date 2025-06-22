package com.filantrop.connectivitylogger

import android.app.Application
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.filantrop.connectivitylogger.service.ConnectivityNetworkCallback
import com.filantrop.connectivitylogger.service.LOG_FILE_NAME_PATTERN
import com.filantrop.connectivitylogger.ui.theme.ConnectivityLoggerTheme
import java.io.File
import java.util.Date


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
                containerColor = if (!serviceRunning) Color.Green else Color.Red,
                contentColor = if (!serviceRunning) Color.White else Color.Black
            ),
        ) {
            Text(
                text = if (serviceRunning) "Stop" else "Start",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        /*        val file = File(context.filesDir, LOG_FILE_NAME_PATTERN)
                val isFileExist = file.exists()
                Button(
                    onClick = {
                        shareFile(context, file)
                    }, enabled = isFileExist, modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Share log file", style = MaterialTheme.typography.headlineSmall
                    )
                }*/
    }
}

@Composable
fun FileListScreen(viewModel: ConnectivityViewModel) {
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
                FileListItem(file)
            }
        }
    }
}

@Composable
fun FileListItem(file: ConnectivityViewModel.FileItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
/*            Column(
                modifier = Modifier.fillMaxWidth()
            ) {*/
                Column {
                    Text(text = file.name, fontWeight = FontWeight.Bold)
                    Text(text = "Size: ${file.size / 1024} KB")
                }
                Spacer(modifier = Modifier.weight(1f))
/*                Button(
                    modifier = Modifier.align(Alignment.End),
                    onClick = {
                        //OnDelete(file)
                    }) {
                    Icon(Icons.Default.Delete, "Delete")
                }*/
            //}
        }
    }
}

@Preview
@Composable
fun PreviewFileListItem() {
    val filename =
        LOG_FILE_NAME_PATTERN + ConnectivityNetworkCallback.simpleDateFormatFileName.format(Date())
    val fileItem = ConnectivityViewModel.FileItem(filename, 500L, File(filename))
    ConnectivityLoggerTheme {
        FileListItem(fileItem)
    }
}
/*
@Composable
fun OnDelete(file: ConnectivityViewModel.FileItem) {
    var showDialog by remember { mutableStateOf(false) }
    var fileName by remember { mutableStateOf("") }

    Button(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Add New File")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add File") },
            text = {
                TextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAdd(fileName)
                        fileName = ""
                        showDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}*/

@Preview(showBackground = true)
@Composable
fun PreviewControlSwitch() {
    val viewModel = ConnectivityViewModel(Application())
    ConnectivityLoggerTheme {
        ControlSwitch(viewModel)
    }
}