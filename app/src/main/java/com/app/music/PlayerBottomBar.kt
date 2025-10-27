package com.app.music

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.music.player.TrackRepository

@Composable
fun PlayerBottomBar(repo: TrackRepository) {
    val state = repo.playerState
    if (state.currentTrackUri == null) return

    var expanded by remember { mutableStateOf(false) }
    val height by animateDpAsState(if (expanded) 180.dp else 72.dp, label = "")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clickable { expanded = !expanded }
            .padding(12.dp)
    ) {
        Text(text = state.currentTrackUri ?: "", maxLines = 1)
        Spacer(Modifier.height(if (expanded) 20.dp else 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { }) { Text("Prev") }
            Button(onClick = { }) { Text(if (state.isPlaying) "Pause" else "Play") }
            Button(onClick = { }) { Text("Next") }
        }

        AnimatedVisibility(visible = expanded) {
            Spacer(Modifier.height(24.dp))
        }
    }
}