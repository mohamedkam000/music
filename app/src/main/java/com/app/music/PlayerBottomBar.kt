package com.app.music

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlayerBottomBar(repo: com.app.music.player.TrackRepository) {
    val state = repo.playerState
    if (state.currentTrackUri == null) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = state.currentTrackUri ?: "")
            Row {
                Button(onClick = { }) { Text("Prev") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { }) { Text(if (state.isPlaying) "Pause" else "Play") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { }) { Text("Next") }
            }
        }
    }
}