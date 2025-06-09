package com.filantrop.connectivitylogger

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.filantrop.connectivitylogger.service.NetworkMonitorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NetworkMonitorApp()
        }
    }
}

@Composable
fun NetworkMonitorApp() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val status = remember { mutableStateOf("Сервис не запущен") }

                    Text(
                        text = "Статус: ${status.value}",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Button(
                        onClick = {
                            if (checkPermissions(context)) {
                                NetworkMonitorService.startService(context)
                                status.value = "Сервис запущен"
                                showSnackbar(
                                    coroutineScope,
                                    snackbarHostState,
                                    "Мониторинг сети запущен"
                                )
                            }
                        },
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text("Запустить мониторинг")
                    }

                    Button(
                        onClick = {
                            NetworkMonitorService.stopService(context)
                            status.value = "Сервис остановлен"
                            showSnackbar(
                                coroutineScope,
                                snackbarHostState,
                                "Мониторинг сети остановлен"
                            )
                        },
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text("Остановить мониторинг")
                    }

                    Button(
                        onClick = {
                            showSnackbar(
                                coroutineScope,
                                snackbarHostState,
                                "Лог сохранен в файл network_log.txt"
                            )
                        }
                    ) {
                        Text("Показать лог")
                    }
                }
            }
        }
    }
}

private fun checkPermissions(context: Context): Boolean {
    val permissions = mutableListOf<String>()

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_NETWORK_STATE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        permissions.add(Manifest.permission.ACCESS_NETWORK_STATE)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (permissions.isNotEmpty()) {
        ActivityCompat.requestPermissions(
            context as ComponentActivity,
            permissions.toTypedArray(),
            PERMISSION_REQUEST_CODE
        )
        return false
    }

    return true
}

private fun showSnackbar(
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    message: String
) {
    coroutineScope.launch {
        snackbarHostState.showSnackbar(message)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNetworkMonitorApp() {
    NetworkMonitorApp()
}

private const val PERMISSION_REQUEST_CODE = 1001