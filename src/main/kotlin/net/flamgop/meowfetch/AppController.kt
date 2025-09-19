package net.flamgop.meowfetch

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dadb.Dadb
import kotlinx.coroutines.*
import java.awt.Desktop
import java.io.File

class AppController {
    var snackbarState = SnackbarHostState()
    var devices by mutableStateOf(listOf<Dadb>())
    var selectedDevice by mutableStateOf(0)
    var terminalText by mutableStateOf("> Hello World! You're using Meowfetch v3.0! :3\n")
    var logging by mutableStateOf(false)
    val settingsState = SettingsState()
    private var logJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun refreshDevices() {
        devices = Dadb.list()
        terminalText += if (devices.isEmpty()) "> No Devices Found\n" else "> ${devices.size} Device(s) Found\n"
    }

    fun selectDevice(device: Int) {
        selectedDevice = Math.clamp(device.toLong(), 0, devices.size)
    }

    fun toggleLogging() {
        logging = !logging
        if (!logging) {
            logJob?.cancel()
            val file = saveTerminal()
            terminalText += "> Stopped Logging\n"
            scope.launch(Dispatchers.IO) {
                val result = snackbarState.showSnackbar(
                    message ="Saved file to ${file.absolutePath}",
                    actionLabel = "Open",
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    withContext(Dispatchers.Main) {
                        try {
                            if (Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().open(file.parentFile)
                            }
                        } catch (e: Exception) {
                            println("Failed to open file explorer: ${e.message}")
                        }
                    }
                }
            }
        } else {
            if (devices.isEmpty()) {
                logging = false
                terminalText += "> No devices connected! Can't start logging.\n"
                return
            }
            logJob = scope.launch(Dispatchers.IO) {
                val shellStream = devices[selectedDevice].openShell("logcat ${settingsState.logFilter}:${settingsState.logLevel.qualifier} *:S")
                try {
                    while (isActive) {
                        val chunk = shellStream.read().payload
                        val text = chunk.decodeToString()
                        withContext(Dispatchers.Main) {
                            terminalText += text
                        }
                    }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) {
                        terminalText += "\n> There was an error while reading the logs (Did you unplug the device?)\n"
                        logging = false
                    }
                } finally {
                    shellStream.close()
                }
            }
            terminalText += "> Started Logging\n"
        }
    }

    private fun saveTerminal(): File {
        val file = File(settingsState.logPath)
        file.parentFile?.mkdirs()
        file.writeText(terminalText)
        return file
    }

    fun clearConsole() {
        terminalText = ""
    }
}