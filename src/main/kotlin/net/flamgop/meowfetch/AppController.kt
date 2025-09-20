package net.flamgop.meowfetch

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import net.flamgop.adb.server.AdbServer
import net.flamgop.adb.server.device.AdbDevice
import java.awt.Desktop
import java.io.File

private fun StringBuilder.extractCompleteLines(): List<String> {
    val text = toString()
    val lines = text.replace("\r\n", "\n").split("\n")
    clear()

    return if (text.endsWith("\n")) {
        // all lines are complete
        lines.filter { it.isNotEmpty() } // drop empty if you want
    } else {
        // last element is incomplete, keep it
        val complete = lines.dropLast(1)
        append(lines.last()) // put remainder back
        complete
    }
}

class AppController {
    var snackbarState = SnackbarHostState()
    var devices by mutableStateOf(listOf<AdbDevice>())
    var selectedDevice by mutableStateOf(0)

    var terminalLines = mutableStateListOf<TerminalLine>(StatusLine("Hello World! You're using Meowfetch v3.0! :3"))

    var saving by mutableStateOf(false)
    var logging by mutableStateOf(false)
    val settingsState = SettingsState()
    val adbServer: AdbServer = AdbServer.start()
    private var logJob: Job? = null
    private var saveLogJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun refreshDevices() {
        scope.launch(Dispatchers.IO) {
            adbServer.discoverDevices()
            devices = adbServer.devices()
            terminalLines += StatusLine(if (devices.isEmpty()) "No Devices Found" else "${devices.size} Device(s) Found")
        }
    }

    fun selectDevice(device: Int) {
        selectedDevice = Math.clamp(device.toLong(), 0, devices.size)
    }

    fun toggleLogging() {
        logging = !logging
        if (!logging) {
            logJob?.cancel()
            saving = true
            // display snackbar with indefinite duration that just says "Saving..."
            scope.launch(Dispatchers.IO) {
                saveLogJob?.cancelAndJoin()
                val savingSnackbarJob = withContext(Dispatchers.Main) {
                    scope.launch {
                        snackbarState.showSnackbar(
                            message = "Saving...",
                            duration = SnackbarDuration.Indefinite
                        )
                    }
                }
                saveLogJob = scope.launch(Dispatchers.IO) {
                    val file = saveTerminal()
                    withContext(Dispatchers.Main) {
                        terminalLines += StatusLine("Stopped Logging")
                        saving = false
                    }

                    savingSnackbarJob.cancel()

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
            }
        } else {
            if (devices.isEmpty()) {
                logging = false
                terminalLines += StatusLine("No devices connected! Can't start logging.")
                return
            }
            logJob = scope.launch(Dispatchers.IO) {
                val id = devices[selectedDevice].openStream("shell:logcat ${settingsState.logFilter}:${settingsState.logLevel.qualifier}${if (settingsState.logFilter != "*") " *:S" else ""}")
                val iterator = devices[selectedDevice].stream(id).iterator()
                try {
                    val lineBuilder = StringBuilder()
                    while (isActive) {
                        val text = iterator.next()
                        lineBuilder.append(text)
                        // split into lines and append to terminalLines with LogcatLine("text")
                        val completeLines = lineBuilder.extractCompleteLines()
                        if (completeLines.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                terminalLines += completeLines.map { LogcatLine(it) }
                            }
                        }
                    }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) {
                        terminalLines += StatusLine("There was an error while reading the logs (Did you unplug the device?)")
                        logging = false
                    }
                } finally {
                    devices[selectedDevice].closeStream(id)
                }
            }
            terminalLines += StatusLine("Started Logging with command \"logcat ${settingsState.logFilter}:${settingsState.logLevel.qualifier}${if (settingsState.logFilter != "*") " *:S" else ""}\"")
        }
    }

    private fun saveTerminal(): File {
        val file = File(settingsState.logPath)
        file.parentFile?.mkdirs()
        file.writeText("")
        for (line in terminalLines) {
            when (line) {
                is StatusLine -> {}
                is LogcatLine -> {
                    file.appendText(line.text + "\n")
                }
            }
        }
        return file
    }

    fun clearConsole() {
        terminalLines.clear()
    }
}