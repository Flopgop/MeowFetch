package net.flamgop.meowfetch

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.material.ripple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import dadb.Dadb
import kotlinx.coroutines.delay
import net.flamgop.meowfetch.resources.Res
import org.jetbrains.compose.resources.painterResource

import net.flamgop.meowfetch.resources.kitty_cat;
import net.flamgop.meowfetch.resources.kitty_cat_dark;
import org.jetbrains.jewel.foundation.DisabledAppearanceValues
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.default
import org.jetbrains.jewel.intui.standalone.theme.light
import org.jetbrains.jewel.intui.standalone.theme.lightThemeDefinition
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.light
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.styling.TitleBarColors
import org.jetbrains.jewel.window.styling.TitleBarStyle

@Composable
@Preview
fun App() {
    val controller = remember { AppController() }
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        controller.devices = Dadb.list()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(controller.snackbarState) { snackbarData ->
                val durationMillis = when (snackbarData.duration) {
                    SnackbarDuration.Indefinite -> Long.MAX_VALUE
                    SnackbarDuration.Long -> 10000L
                    SnackbarDuration.Short -> 4000L
                }
                var progress by remember { mutableStateOf(1f) }

                LaunchedEffect(snackbarData) {
                    val steps = 100
                    val delayTime = durationMillis / steps
                    for (i in 0 until steps) {
                        progress = 1f - i / steps.toFloat()
                        delay(delayTime)
                    }
                    progress = 0f
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Row(
                        modifier = Modifier
                            .wrapContentWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            progress = { 1 - progress },
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(24.dp)
                                .scale(-1f, 1f),
                            color = MaterialTheme.colorScheme.primary,
                        )

                        Spacer(modifier = Modifier.size(20.dp))

                        Text(
                            text = snackbarData.message,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.size(20.dp))

                        snackbarData.actionLabel?.let { label ->
                            Button(
                                onClick = { snackbarData.performAction() },
                                colors = ButtonDefaults.textButtonColors(),
                                elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text(label, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Box(
                            modifier =
                                Modifier
                                    .size(36.dp)
                                    .minimumInteractiveComponentSize()
                                    .clickable(
                                        onClick = { snackbarData.dismiss() },
                                        enabled = true,
                                        role = Role.Button,
                                        interactionSource = null,
                                        indication = ripple(bounded = false, radius = 18.dp),
                                    ),
                            contentAlignment = Alignment.Center,
                        ) {
                            val contentAlpha = LocalContentAlpha.current
                            CompositionLocalProvider(LocalContentAlpha provides contentAlpha, content = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                )
                            })
                        }
                    }
                }
            }
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {
            MaterialTheme {
                DevicePanel(
                    devices = controller.devices,
                    onRefreshDevices = { controller.refreshDevices() },
                    onToggleLogging = { controller.toggleLogging() },
                    onClearConsole = { controller.clearConsole() },
                    logging = controller.logging,
                    onShowSettings = { showSettings = true }
                )

                ConsoleView(
                    terminalText = controller.terminalText,
                    verticalScrollState = verticalScrollState,
                    horizontalScrollState = horizontalScrollState
                )
            }

            if (showSettings) {
                SettingsScreen(
                    controller.settingsState,
                    onDismiss = { showSettings = false }
                )
            }
        }
    }
}

fun main() = application {
    val titleStyle = TitleBarStyle.light(TitleBarColors.light(backgroundColor = MaterialTheme.colorScheme.tertiary))
    IntUiTheme(
        JewelTheme.lightThemeDefinition(disabledAppearanceValues = DisabledAppearanceValues.light()),
        styling = ComponentStyling.default().decoratedWindow(
            titleBarStyle = titleStyle
        ),
    ) {
        DecoratedWindow(
            onCloseRequest = ::exitApplication,
            title = "Meowfetch",
            icon = painterResource(if (isSystemInDarkTheme()) Res.drawable.kitty_cat else Res.drawable.kitty_cat_dark),
        ) {
            TitleBar(
                gradientStartColor = MaterialTheme.colorScheme.primary,
                style = titleStyle
            ) {
                Text("Meowfetch", color = MaterialTheme.colorScheme.onPrimary)
            }
            App()
        }
    }
}