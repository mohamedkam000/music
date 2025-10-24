package com.app.music.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.app.music.player.TrackRepository
import kotlinx.coroutines.*

class MusicPlayerService : Service() {
    companion object {
        const val ACTION_PLAY_RANDOM = "ACTION_PLAY_RANDOM"
        const val ACTION_PLAY_TRACK = "ACTION_PLAY_TRACK"
        const val NOTIF_CHANNEL_ID = "music_player_channel"
        const val NOTIF_ID = 1
    }

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
        startForeground(NOTIF_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                ACTION_PLAY_RANDOM -> playRandom()
                ACTION_PLAY_TRACK -> {
                    val uriString = intent.getStringExtra("track_uri")
                    uriString?.let { playUri(it) }
                }
            }
        }
        return START_STICKY
    }

    private fun playRandom() {
        val repo = TrackRepository(this)
        val tracks = repo.tracks.value
        if (tracks.isEmpty()) return
        val track = tracks.random()
        playUri(track.uri.toString())
    }

    private fun playUri(uriString: String) {
        player.setMediaItem(MediaItem.fromUri(uriString))
        player.prepare()
        player.playWhenReady = true
        startForeground(NOTIF_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val builder = Notification.Builder(this, NOTIF_CHANNEL_ID)
        builder.setContentTitle("Music")
//        builder.setContentText("Playing music")
        builder.setSmallIcon(android.R.drawable.ic_media_play)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NOTIF_CHANNEL_ID)
        }
        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val chan = NotificationChannel(NOTIF_CHANNEL_ID, "Music player", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(chan)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        player.release()
        mediaSession.release()
        super.onDestroy()
    }
}