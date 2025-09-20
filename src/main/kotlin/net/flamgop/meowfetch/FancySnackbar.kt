package net.flamgop.meowfetch;

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration;
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable;
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.jewel.ui.component.Icon

@Composable
fun FancySnackbar(snackbarState: SnackbarHostState) {
    SnackbarHost(snackbarState) { snackbarData ->
        val durationMillis = when (snackbarData.visuals.duration) {
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
                if (snackbarData.visuals.duration != SnackbarDuration.Indefinite) {
                    CircularProgressIndicator(
                        progress = { 1 - progress },
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(-1f, 1f),
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .size(24.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.size(20.dp))

                Text(
                    text = snackbarData.visuals.message,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.size(20.dp))

                snackbarData.visuals.actionLabel?.let { label ->
                    Button(
                        onClick = { snackbarData.performAction() },
                        colors = ButtonDefaults.textButtonColors(),
                        elevation = ButtonDefaults.elevatedButtonElevation(0.dp, 0.dp, 0.dp, 0.dp),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(label, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .minimumInteractiveComponentSize()
                        .clickable(
                            onClick = { snackbarData.dismiss() },
                            enabled = true,
                            role = Role.Button,
                            interactionSource = null,
                            indication = ripple(bounded = false, radius = 18.dp, color = MaterialTheme.colorScheme.tertiary),
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
