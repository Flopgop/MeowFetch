package net.flamgop.meowfetch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.openFileSaver
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Paths

enum class LogLevel(val qualifier: String) {
    VERBOSE("V"),
    DEBUG("D"),
    INFO("I"),
    WARNING("W"),
    ERROR("E"),
    ASSERT("A"),
    SILENT("S")
}

fun LogLevel.prettyName(): String =
    name.lowercase().replaceFirstChar { it.uppercase() }

class SettingsState(
    initialPath: String = Paths.get("log.txt").toAbsolutePath().toString(),
    initialFilter: String = "com.qcxr.qcxr",
    initialLogLevel: LogLevel = LogLevel.DEBUG,
) {
    var logPath by mutableStateOf(initialPath)
    var logFilter by mutableStateOf(initialFilter)
    var logLevel by mutableStateOf(initialLogLevel)
}

fun isStringValidPackage(str: String): Boolean {
    val packageRegex = Regex(
        "^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+",
    )
    return str == "*" || str.matches(packageRegex)
}

@Composable
fun PackageTextField(
    logFilter: String,
    onLogFilterChange: (String) -> Unit,
    defaultValue: String = "com.qcxr.qcxr"
) {
    var text by remember { mutableStateOf(logFilter) }
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier
            .heightIn(min = 36.dp)
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (isFocused && !focusState.isFocused) {
                    // focus lost
                    if (!isStringValidPackage(text)) {
                        text = defaultValue // revert to default
                        onLogFilterChange(defaultValue)
                    } else {
                        onLogFilterChange(text)
                    }
                }
                isFocused = focusState.isFocused
            },
        label = { Text("Log Filter") },
        placeholder = { Text(defaultValue) },
        isError = !isStringValidPackage(text),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                if (!isStringValidPackage(text)) {
                    text = defaultValue
                    onLogFilterChange(defaultValue)
                } else {
                    onLogFilterChange(text)
                }
            }
        )
    )
}

@Composable
fun FileSelectOption(
    path: String,
    ifBlank: String = "Select a path",
    label: String,
    suggestedName: String,
    extension: String?,
    onSelected: (path: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val textScrollState = rememberScrollState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = path.ifBlank { ifBlank },
            onValueChange = {}, // not mutable
            Modifier.weight(1f).heightIn(min = 36.dp),
            label = {
                Row(modifier = Modifier.horizontalScroll(textScrollState)) {
                    Text(label, softWrap = false)
                }
            },
            singleLine = true,
        )
        Box(Modifier.padding(top = with(LocalDensity.current) { 8.sp.toDp() })) {
            Button(
                modifier = Modifier.height(36.dp),
                onClick = {
                    scope.launch {
                        val file = FileKit.openFileSaver(
                            suggestedName,
                            extension,
                            PlatformFile(File(path).parentFile.absolutePath)
                        )
                        if (file != null) {
                            onSelected(file.absolutePath())
                        }
                    }
                }
            ) {
                Text("Browse")
            }
        }
    }
}

@Composable
fun LogLevelSelector(
    logLevel: LogLevel,
    onSelected: (LogLevel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val intSource = remember { MutableInteractionSource() }

    Box {
        Box(modifier = Modifier.clickable {
            expanded = true
        }.heightIn(min = 36.dp)) {
            TextFieldDefaults.OutlinedTextFieldDecorationBox(
                value = ".",
                visualTransformation = VisualTransformation.None,
                innerTextField = {
                    Text(
                        logLevel.prettyName(),
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface
                    )
                },
                placeholder = {},
                label = { Text("Log Level") },
                leadingIcon = null,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        tint = MaterialTheme.colors.onSurface,
                        contentDescription = null
                    )
                },
                singleLine = true,
                enabled = true,
                isError = false,
                interactionSource = intSource,
                shape = MaterialTheme.shapes.small,
                colors = TextFieldDefaults.outlinedTextFieldColors(),
                border = {
                    TextFieldDefaults.BorderBox(
                        enabled = true,
                        isError = false,
                        interactionSource = intSource,
                        colors = TextFieldDefaults.outlinedTextFieldColors(),
                        shape = MaterialTheme.shapes.small,
                    )
                },
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            LogLevel.entries.forEach { level ->
                DropdownMenuItem(onClick = {
                    onSelected(level)
                    expanded = false
                }) {
                    Text("${level.prettyName()} (${level.qualifier})")
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    settingsState: SettingsState,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colors.surface,
            elevation = 8.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Settings", style = MaterialTheme.typography.h5)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FileSelectOption(settingsState.logPath, suggestedName = "log.txt", extension = "txt", label = "Log Path", onSelected = { settingsState.logPath = it })
                }
                PackageTextField(logFilter = settingsState.logFilter, onLogFilterChange = { settingsState.logFilter = it })
                LogLevelSelector(logLevel = settingsState.logLevel, onSelected = { settingsState.logLevel = it })

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}