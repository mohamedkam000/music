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
        val MIN_FILE_SIZE_BYTES = 1 * 1024 * 1024 // 1 MB minimum size

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND " +
                        "${MediaStore.Audio.Media.SIZE} >= ? AND " +
                        "${MediaStore.Audio.Media.DATA} NOT LIKE ?"

        val selectionArgs = arrayOf(
            MIN_FILE_SIZE_BYTES.toString(),
            "%/Android/media/%"
        )

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val query = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
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
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
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