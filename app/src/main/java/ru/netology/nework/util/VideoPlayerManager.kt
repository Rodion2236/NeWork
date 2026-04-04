package ru.netology.nework.util

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var player: ExoPlayer? = null
    var onPlaybackStateChanged: ((Boolean) -> Unit)? = null
    var onVideoError: ((String) -> Unit)? = null

    @OptIn(UnstableApi::class)
    fun getPlayer(): ExoPlayer {
        if (player == null) {
            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    15_000,
                    60_000,
                    3_000,
                    6_000
                )
                .build()

            player = ExoPlayer.Builder(context)
                .setLoadControl(loadControl)
                .setHandleAudioBecomingNoisy(true)
                .build()
                .apply {
                    playWhenReady = false
                    addListener(object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            Log.e("MediaPlayer", "Error: ${error.message}", error)
                            onVideoError?.invoke(error.message ?: "Unknown error")
                        }

                        override fun onPlaybackStateChanged(state: Int) {
                            when (state) {
                                Player.STATE_READY -> {
                                    Log.d("MediaPlayer", "Video ready")
                                }
                                Player.STATE_BUFFERING -> {
                                    Log.d("MediaPlayer", "Buffering...")
                                }
                                Player.STATE_ENDED -> {
                                    Log.d("MediaPlayer", "Video ended")
                                }
                                Player.STATE_IDLE -> {
                                    Log.d("MediaPlayer", "Player idle")
                                }
                            }
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            onPlaybackStateChanged?.invoke(isPlaying)
                        }
                    })
                }
        }
        return player!!
    }

    fun setMediaUrl(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        getPlayer().setMediaItem(mediaItem)
        getPlayer().prepare()
    }

    fun play() {
        val gp = getPlayer()
        if (gp.playbackState == Player.STATE_IDLE) {
            gp.prepare()
        }
        gp.play()
    }

    fun pause() = getPlayer().pause()

    fun togglePlayPause() {
        if (getPlayer().isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun stop() = getPlayer().stop()

    fun detachPlayer() = pause()

    fun release() {
        player?.release()
        player = null
        onPlaybackStateChanged = null
        onVideoError = null
    }

    fun isPlaying(): Boolean = player?.isPlaying == true

    fun getCurrentPosition(): Long = player?.currentPosition ?: 0L

    fun getDuration(): Long = player?.duration ?: 0L
}