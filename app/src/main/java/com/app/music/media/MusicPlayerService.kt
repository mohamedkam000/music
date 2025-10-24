package com.app.music.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.app.music.MainActivity
import com.app.music.player.TrackRepository
import kotlin.random.Random

class MusicPlayerService : Service() {
    companion object {
        const val ACTION_PLAY_RANDOM = "ACTION_PLAY_RANDOM"
        const val ACTION_PLAY_TRACK = "ACTION_PLAY_TRACK"
        const val ACTION_TOGGLE = "ACTION_TOGGLE"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREV = "ACTION_PREV"
        const val NOTIF_CHANNEL_ID = "music_player_channel"
        const val NOTIF_ID = 1
    }

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private val repo by lazy { TrackRepository(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
        startForeground(NOTIF_ID, buildNotification())
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                startForeground(NOTIF_ID, buildNotification())
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                startForeground(NOTIF_ID, buildNotification())
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_RANDOM -> playRandom()
            ACTION_PLAY_TRACK -> intent.getStringExtra("track_uri")?.let { playUri(it) }
            ACTION_TOGGLE -> togglePlayback()
            ACTION_NEXT -> playRandom()
            ACTION_PREV -> playRandom()
        }
        return START_STICKY
    }

    private fun playRandom() {
        val tracks = repo.tracks.value
        if (tracks.isEmpty()) return
        val track = tracks[Random.nextInt(tracks.size)]
        playUri(track.uri.toString())
    }

    private fun playUri(uriString: String) {
        player.setMediaItem(MediaItem.fromUri(uriString))
        player.prepare()
        player.playWhenReady = true
    }

    private fun togglePlayback() {
        player.playWhenReady = !player.playWhenReady
    }

    private fun buildNotification(): Notification {
        val currentTrack = player.currentMediaItem?.localConfiguration?.uri?.lastPathSegment ?: "Unknown Track"
        val openIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val prevIntent = PendingIntent.getService(this, 1, Intent(this, MusicPlayerService::class.java).apply { action = ACTION_PREV }, PendingIntent.FLAG_IMMUTABLE)
        val playPauseIntent = PendingIntent.getService(this, 2, Intent(this, MusicPlayerService::class.java).apply { action = ACTION_TOGGLE }, PendingIntent.FLAG_IMMUTABLE)
        val nextIntent = PendingIntent.getService(this, 3, Intent(this, MusicPlayerService::class.java).apply { action = ACTION_NEXT }, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle("Music")
            .setContentText(currentTrack)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(openIntent)
            .setOngoing(player.isPlaying)
            .addAction(android.R.drawable.ic_media_previous, "Prev", prevIntent)
            .addAction(if (player.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play, "Play/Pause", playPauseIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", nextIntent)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionCompatToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }

    private fun createNotificationChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        val chan = NotificationChannel(NOTIF_CHANNEL_ID, "Music player", NotificationManager.IMPORTANCE_LOW)
        nm.createNotificationChannel(chan)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        player.release()
        mediaSession.release()
        super.onDestroy()
    }
}