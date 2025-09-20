package net.flamgop.meowfetch

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch
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
    val lazyListState = rememberLazyListState()
    val horizontalScrollState = rememberScrollState()
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        controller.adbServer.discoverDevices()
        controller.devices = controller.adbServer.devices()
    }

    Scaffold(
        snackbarHost = { FancySnackbar(controller.snackbarState) }
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
                    onShowSettings = { showSettings = true },
                    logging = controller.logging,
                    saving = controller.saving,
                )

                ConsoleView(
                    terminalLines = controller.terminalLines,
                    listState = lazyListState,
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