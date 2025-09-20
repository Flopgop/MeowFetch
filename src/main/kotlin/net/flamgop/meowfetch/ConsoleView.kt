package net.flamgop.meowfetch

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

sealed interface TerminalLine {
    val text: String
}

data class LogcatLine(override val text: String) : TerminalLine
data class StatusLine(override val text: String) : TerminalLine

@Composable
fun ConsoleView(
    terminalLines: List<TerminalLine>,
    listState: LazyListState,
    horizontalScrollState: ScrollState
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp))
                .padding(end = 12.dp, bottom = 12.dp)
                .horizontalScroll(horizontalScrollState)
        ) {
            SelectionContainer {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(terminalLines) { line ->
                        when (line) {
                            is LogcatLine -> {
                                Text(
                                    line.text,
                                    fontFamily = FontFamily.Monospace,
                                    softWrap = false
                                )
                            }
                            is StatusLine -> {
                                Text(
                                    "> " + line.text,
                                    fontFamily = FontFamily.Monospace,
                                    softWrap = false,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }
        }

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(listState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
        )

        HorizontalScrollbar(
            adapter = rememberScrollbarAdapter(horizontalScrollState),
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
        )
    }
}