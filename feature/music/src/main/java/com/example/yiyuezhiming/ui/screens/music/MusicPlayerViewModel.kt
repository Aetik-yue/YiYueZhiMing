package com.example.yiyuezhiming.ui.screens.music

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.yiyuezhiming.core.player.PlaybackService
import com.example.yiyuezhiming.data.MusicRepository
import com.example.yiyuezhiming.model.Song
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MusicPlayerState(
    val songs: List<Song> = emptyList(),
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val position: Long = 0L,
    val duration: Long = 0L,
    val isShuffleOn: Boolean = false,
    val repeatMode: Int = 0,
    val isLoading: Boolean = true,
    val hasPermission: Boolean = false
) {
    val currentSong: Song? get() = queue.getOrNull(currentIndex)
    val progress: Float get() = if (duration > 0) position.toFloat() / duration else 0f
}

@HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    private val repository: MusicRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(MusicPlayerState())
    val state: StateFlow<MusicPlayerState> = _state.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var positionPollingJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) startPositionPolling() else stopPositionPolling()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val index = controller?.currentMediaItemIndex ?: -1
            _state.update {
                it.copy(
                    currentIndex = index,
                    duration = controller?.duration?.coerceAtLeast(0L) ?: 0L
                )
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                _state.update {
                    it.copy(duration = controller?.duration?.coerceAtLeast(0L) ?: 0L)
                }
            }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _state.update { it.copy(isShuffleOn = shuffleModeEnabled) }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            val mapped = when (repeatMode) {
                Player.REPEAT_MODE_ALL -> 1
                Player.REPEAT_MODE_ONE -> 2
                else -> 0
            }
            _state.update { it.copy(repeatMode = mapped) }
        }
    }

    init {
        checkPermissionAndLoad()
    }

    private fun checkPermissionAndLoad() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val granted = ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
        _state.update { it.copy(hasPermission = granted) }
        if (granted) {
            loadSongs()
        } else {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(hasPermission = granted) }
        if (granted) {
            loadSongs()
        }
    }

    private fun loadSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val songs = repository.scanDeviceMusic()
                _state.update {
                    it.copy(
                        songs = songs,
                        queue = songs,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun connectToService() {
        if (controllerFuture != null) return
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future
        viewModelScope.launch {
            try {
                val mediaController = future.await()
                controller = mediaController
                mediaController.addListener(playerListener)
                _state.update {
                    it.copy(
                        isPlaying = mediaController.isPlaying,
                        currentIndex = mediaController.currentMediaItemIndex,
                        duration = mediaController.duration.coerceAtLeast(0L),
                        isShuffleOn = mediaController.shuffleModeEnabled,
                        repeatMode = when (mediaController.repeatMode) {
                            Player.REPEAT_MODE_ALL -> 1
                            Player.REPEAT_MODE_ONE -> 2
                            else -> 0
                        }
                    )
                }
                if (mediaController.isPlaying) startPositionPolling()
            } catch (e: Exception) {
                // Service not available
            }
        }
    }

    fun playSong(index: Int) {
        val mediaController = controller ?: return
        val queue = _state.value.queue
        if (index < 0 || index >= queue.size) return

        val mediaItems = queue.map { song -> MediaItem.fromUri(song.uri) }
        mediaController.setMediaItems(mediaItems, index, 0L)
        mediaController.prepare()
        mediaController.playWhenReady = true
        _state.update {
            it.copy(currentIndex = index, isPlaying = true)
        }
    }

    fun togglePlayPause() {
        val mediaController = controller ?: return
        if (mediaController.isPlaying) {
            mediaController.pause()
        } else {
            mediaController.play()
        }
    }

    fun next() {
        controller?.seekToNext()
    }

    fun previous() {
        controller?.seekToPrevious()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _state.update { it.copy(position = positionMs) }
    }

    fun toggleShuffle() {
        val mediaController = controller ?: return
        mediaController.shuffleModeEnabled = !mediaController.shuffleModeEnabled
    }

    fun cycleRepeatMode() {
        val mediaController = controller ?: return
        val nextMode = when (mediaController.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        mediaController.repeatMode = nextMode
    }

    private fun startPositionPolling() {
        if (positionPollingJob?.isActive == true) return
        positionPollingJob = viewModelScope.launch {
            while (isActive) {
                val mediaController = controller
                if (mediaController != null) {
                    _state.update {
                        it.copy(
                            position = mediaController.currentPosition.coerceAtLeast(0L),
                            duration = mediaController.duration.coerceAtLeast(0L)
                        )
                    }
                }
                delay(500L)
            }
        }
    }

    private fun stopPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPositionPolling()
        controller?.removeListener(playerListener)
        controllerFuture?.let { future ->
            MediaController.releaseFuture(future)
        }
        controller = null
        controllerFuture = null
    }
}
