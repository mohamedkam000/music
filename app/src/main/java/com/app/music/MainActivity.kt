package com.app.music

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.music.media.MusicPlayerService
import com.app.music.player.Track
import com.app.music.player.TrackRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val mediaAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> }

    private val postNotiPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaAudioPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        postNotiPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    
        intent?.data?.let { uri ->
            setContent {
                MaterialTheme {
                    QuickPlayScreen(uri)
                }
            }
            return
        }

        setContent {
            val repo = remember { TrackRepository(this) }
            val tracks by repo.tracks.collectAsState(initial = emptyList())
            val playerState = remember { repo.playerState }
            val coroutineScope = rememberCoroutineScope()
    
            MaterialTheme {
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            val intent = Intent(this, MusicPlayerService::class.java).apply {
                                action = MusicPlayerService.ACTION_PLAY_RANDOM
                            }
                            startService(intent)
                        }) {
                            Text("Play")
                        }
                    },
                    bottomBar = {
                        PlayerBottomBar(repo)
                    }
                ) { padding ->
                    LazyColumn(modifier = Modifier.padding(padding)) {
                        items(tracks) { track ->
                            TrackRow(track) {
                                val intent = Intent(this@MainActivity, MusicPlayerService::class.java).apply {
                                    action = MusicPlayerService.ACTION_PLAY_TRACK
                                    putExtra("track_uri", track.uri.toString())
                                }
                                startService(intent)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackRow(track: Track, onClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(16.dp)) {
        Text(track.title)
        Text(track.artist ?: "Unknown", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun QuickPlayScreen(uri: Uri) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("testing external file", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = {
                val intent = Intent(context, MusicPlayerService::class.java).apply {
                    action = MusicPlayerService.ACTION_PLAY_TRACK
                    putExtra("track_uri", uri.toString())
                }
                context.startService(intent)
            }) { Text("Play") }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                val intent = Intent(context, MusicPlayerService::class.java).apply {
                    action = MusicPlayerService.ACTION_TOGGLE
                }
                context.startService(intent)
            }) { Text("Pause") }
        }
    }
}