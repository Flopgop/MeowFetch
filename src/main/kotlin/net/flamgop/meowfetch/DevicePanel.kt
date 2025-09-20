package net.flamgop.meowfetch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import net.flamgop.adb.server.device.AdbDevice

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DevicePanel(
    devices: List<AdbDevice>,
    onRefreshDevices: () -> Unit,
    onToggleLogging: () -> Unit,
    onClearConsole: () -> Unit,
    onShowSettings: () -> Unit,
    logging: Boolean,
    saving: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxHeight().width(220.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colors.surface)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Text("Meowfetch", style = MaterialTheme.typography.h4)
            Text("${devices.size} ${if (devices.size == 1) "Device" else "Devices"}", style = MaterialTheme.typography.subtitle2)

            Spacer(Modifier.height(8.dp))

            Button(onClick = onRefreshDevices, modifier = Modifier.fillMaxWidth(), enabled = !logging) {
                Text("Discover Devices")
            }

            Button(onClick = onToggleLogging, modifier = Modifier.fillMaxWidth(), enabled = devices.isNotEmpty() && !saving) {
                Text(if (logging) "Stop Logging" else "Start Logging")
            }

            Button(onClick = onClearConsole, modifier = Modifier.fillMaxWidth(), enabled = !logging && !saving) {
                Text("Clear Console")
            }
        }

        IconButton(onClick = onShowSettings, modifier = Modifier.align(Alignment.Start)) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
    }
}