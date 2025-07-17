package com.supernova.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.supernova.ui.theme.SupernovaColors

/** Row showing channel number and name */
@Composable
fun ChannelRow(number: Int, name: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(SupernovaColors.SurfaceVariant)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = number.toString(), fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = name, fontSize = 14.sp)
    }
}

/** Program block with start/end time */
@Composable
fun TimeSlot(start: String, end: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(4.dp)) {
        Text(text = "$start - $end", fontSize = 12.sp)
    }
}

/** Focusable program tile with metadata */
@Composable
fun ProgramCard(title: String, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .width(120.dp)
            .height(60.dp)
            .focusable()
            .onFocusChanged { focused = it.isFocused }
            .padding(2.dp),
        border = if (focused) BorderStroke(2.dp, SupernovaColors.Focus) else null,
        colors = CardDefaults.cardColors(containerColor = SupernovaColors.Surface)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = title, fontSize = 12.sp)
        }
    }
}

/** Horizontal header showing times */
@Composable
fun TimeHeader(times: List<String>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .background(SupernovaColors.SurfaceVariant)
    ) {
        times.forEach { time ->
            Box(modifier = Modifier.width(120.dp).padding(4.dp)) {
                Text(text = time, fontSize = 12.sp)
            }
        }
    }
}

/** Scrollable grid of programs per channel */
@Composable
fun EPGGrid(
    channels: List<String>,
    times: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        TimeHeader(times)
        channels.forEach { channel ->
            Row {
                ChannelRow(number = channels.indexOf(channel) + 1, name = channel)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    times.forEach { _ ->
                        ProgramCard(title = "...")
                    }
                }
            }
        }
    }
}
