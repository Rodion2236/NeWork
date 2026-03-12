package ru.netology.nework.util

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
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

    @OptIn(UnstableApi::class)
    fun getPlayer(): ExoPlayer {
        if (player == null) {
            player = ExoPlayer.Builder(context).build().apply {
                playWhenReady = false
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        android.util.Log.e("MediaPlayer", "Error: ${error.message}")
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
        if (getPlayer().playbackState == Player.STATE_IDLE) {
            getPlayer().prepare()
        }
        getPlayer().play()
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
    }

    fun isPlaying(): Boolean = player?.isPlaying == true

    fun getCurrentPosition(): Long = player?.currentPosition ?: 0L

    fun getDuration(): Long = player?.duration ?: 0L
}