package com.example.yiyuezhiming.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,  // milliseconds
    val uri: String,     // content:// URI string
    val albumArtUri: String?  // content:// media album art URI
)
