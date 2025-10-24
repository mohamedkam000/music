package com.app.music.player

import android.net.Uri

data class Track(
    val id: Long,
    val title: String,
    val artist: String?,
    val uri: Uri
)