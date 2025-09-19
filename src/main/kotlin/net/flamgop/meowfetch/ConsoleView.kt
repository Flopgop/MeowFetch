package net.flamgop.meowfetch

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun ConsoleView(
    terminalText: String,
    verticalScrollState: ScrollState,
    horizontalScrollState: ScrollState
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .background(MaterialTheme.colors.surface)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp))
                .padding(end = 12.dp, bottom = 12.dp)
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState)
        ) {
            SelectionContainer {
                Text(
                    terminalText,
                    fontFamily = FontFamily.Monospace,
                    softWrap = false
                )
            }
        }

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(verticalScrollState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
        )

        HorizontalScrollbar(
            adapter = rememberScrollbarAdapter(horizontalScrollState),
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
        )
    }
}