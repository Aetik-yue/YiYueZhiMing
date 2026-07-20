package com.example.yiyuezhiming.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.yiyuezhiming.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val albumArtBaseUri: Uri = Uri.parse("content://media/external/audio/albumart")

    /**
     * Scans the device for all music files, filtering out ringtones and notifications.
     * Returns an empty list if permission is not granted or query fails.
     */
    fun scanDeviceMusic(): List<Song> {
        val songs = mutableListOf<Song>()

        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        // Filter: duration > 30 seconds (30000 ms) to exclude ringtones
        val selection = "${MediaStore.Audio.Media.DURATION} > ?"
        val selectionArgs = arrayOf("30000")

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val contentResolver: ContentResolver = context.contentResolver
            contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: ""
                    val artist = cursor.getString(artistColumn) ?: ""
                    val album = cursor.getString(albumColumn) ?: ""
                    val duration = cursor.getLong(durationColumn)
                    val data = cursor.getString(dataColumn) ?: ""
                    val albumId = cursor.getLong(albumIdColumn)

                    // Filter out ringtones and notification sounds
                    val lowerTitle = title.lowercase()
                    if (lowerTitle.startsWith("ringtone") || lowerTitle.contains("notification")) {
                        continue
                    }

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                    ).toString()

                    val albumArtUri = ContentUris.withAppendedId(
                        albumArtBaseUri, albumId
                    ).toString()

                    songs.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            uri = contentUri,
                            albumArtUri = albumArtUri
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted - return empty list
            return emptyList()
        } catch (e: Exception) {
            // Handle any other query failures gracefully
            return emptyList()
        }

        return songs
    }

    /**
     * Retrieves a single song by its MediaStore ID.
     */
    fun getSongById(id: Long): Song? {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media._ID} = ?"
        val selectionArgs = arrayOf(id.toString())

        try {
            val contentResolver: ContentResolver = context.contentResolver
            contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val songId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) ?: ""
                    val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: ""
                    val album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)) ?: ""
                    val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)) ?: ""
                    val albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId
                    ).toString()

                    val albumArtUri = ContentUris.withAppendedId(
                        albumArtBaseUri, albumId
                    ).toString()

                    return Song(
                        id = songId,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        uri = contentUri,
                        albumArtUri = albumArtUri
                    )
                }
            }
        } catch (e: SecurityException) {
            return null
        } catch (e: Exception) {
            return null
        }

        return null
    }
}
