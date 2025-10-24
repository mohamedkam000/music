package com.app.music.player

import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TrackRepository(private val context: Context) {
    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks = _tracks.asStateFlow()
    var playerState: PlayerState = PlayerState()

    init {
        loadTracks()
    }

    private fun loadTracks() {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC}=1"
        val query = context.contentResolver.query(
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )
        val list = mutableListOf<Track>()
        query?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol) ?: "Unknown"
                val artist = cursor.getString(artistCol)
                val contentUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL).buildUpon().appendPath(id.toString()).build()
                list += Track(id, title, artist, contentUri)
            }
        }
        _tracks.value = list
    }
}

data class PlayerState(
    var currentTrackUri: String? = null,
    var positionMs: Long = 0L,
    var isPlaying: Boolean = false
)