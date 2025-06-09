package com.filantrop.connectivitylogger

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.filantrop.connectivitylogger.model.MainViewModel
import com.filantrop.connectivitylogger.service.BackgroundService


class MainActivity : ComponentActivity() {
    private val viewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }

    private var serviceBinder: BackgroundService.ServiceBinder? = null

    private val serviceConnection = object : android.content.ServiceConnection {
        override fun onServiceConnected(
            name: android.content.ComponentName?,
            binder: android.os.IBinder?
        ) {
            serviceBinder = binder as BackgroundService.ServiceBinder
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
            MaterialTheme { // Use Material Theme for consistent styling
                ControlSwitch(viewModel)
            }
        }

        bindService(
            Intent(this, BackgroundService::class.java).setAction(ON_BIND),
            serviceConnection,
            BIND_AUTO_CREATE
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(serviceConnection)
    }

    companion object {
        private val TAG = MainActivity::class.java.canonicalName
        private const val ON_BIND = "ON_BIND"
    }

}

@Composable
private fun ControlSwitch(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val serviceRunning by mainViewModel.serviceState.collectAsState()

        Text(
            text = if (serviceRunning) "Статус: Сервис запущен" else "Статус: Сервис не запущен",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Switch(
            checked = serviceRunning,
            onCheckedChange = {},
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = {
                mainViewModel.startStopService(context)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!serviceRunning) Color.Green else Color.Red,
                contentColor = if (!serviceRunning) Color.White else Color.Black
            ),
        ) {
            Text(
                text = if (serviceRunning) "Остановить" else "Запустить"
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewSwitchWithViewModel() {
    val viewModel = MainViewModel()
    MaterialTheme {
        ControlSwitch(viewModel)
    }
}
