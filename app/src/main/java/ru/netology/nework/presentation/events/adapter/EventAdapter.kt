package ru.netology.nework.presentation.events.adapter

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardEventBinding
import ru.netology.nework.domain.model.AttachmentType
import ru.netology.nework.domain.model.Event
import ru.netology.nework.domain.model.EventType
import ru.netology.nework.util.DateUtils
import ru.netology.nework.util.VideoPlayerManager
import ru.netology.nework.util.clearImage
import ru.netology.nework.util.load

class EventAdapter(
    private val onLikeClick: (String, Boolean) -> Unit,
    private val onEventClick: (Event) -> Unit,
    private val onMenuClick: (Event, anchor: View) -> Unit,
    private val playerManager: VideoPlayerManager
) : PagingDataAdapter<Event, EventViewHolder>(EventDiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)?.attachment?.type) {
            AttachmentType.IMAGE -> VIEW_TYPE_IMAGE
            AttachmentType.VIDEO -> VIEW_TYPE_VIDEO
            AttachmentType.AUDIO -> VIEW_TYPE_AUDIO
            else -> VIEW_TYPE_TEXT
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = CardEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return when (viewType) {
            VIEW_TYPE_IMAGE -> ImageEventViewHolder(binding, onLikeClick, onEventClick, onMenuClick)
            VIEW_TYPE_VIDEO -> VideoEventViewHolder(binding, onLikeClick, onEventClick, onMenuClick, playerManager)
            VIEW_TYPE_AUDIO -> AudioEventViewHolder(binding, onLikeClick, onEventClick, onMenuClick, playerManager)
            else -> TextEventViewHolder(binding, onLikeClick, onEventClick, onMenuClick)
        }
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
            if (holder is VideoEventViewHolder) {
                holder.attachPlayer()
            }
        }
    }

    override fun onViewRecycled(holder: EventViewHolder) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()
    }

    companion object {
        private const val VIEW_TYPE_TEXT = 0
        private const val VIEW_TYPE_IMAGE = 1
        private const val VIEW_TYPE_VIDEO = 2
        private const val VIEW_TYPE_AUDIO = 3
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Event, newItem: Event) = oldItem == newItem
}

abstract class EventViewHolder(
    protected val binding: CardEventBinding,
    private val onLikeClick: (String, Boolean) -> Unit,
    private val onEventClick: (Event) -> Unit,
    private val onMenuClick: (Event, anchor: View) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    abstract fun bind(event: Event)

    protected fun loadAvatar(avatarUrl: String?) {
        binding.avatar.load(
            url = avatarUrl,
            placeholder = R.drawable.ic_account_circle_24,
            error = R.drawable.ic_account_circle_24,
            roundedCorners = 24
        )
    }

    open fun onViewRecycled() {
        binding.avatar.clearImage()
    }

    protected fun setupClicks(event: Event) {
        binding.root.setOnClickListener { onEventClick(event) }
        binding.buttonLike.setOnClickListener {
            onLikeClick(event.id, event.likedByMe)
        }
        binding.buttonOption.setOnClickListener { view ->
            onMenuClick(event, view)
        }
    }

    protected fun enableLinks(content: TextView) {
        content.movementMethod = LinkMovementMethod.getInstance()
        content.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            false
        }
    }
}

class TextEventViewHolder(
    binding: CardEventBinding,
    onLikeClick: (String, Boolean) -> Unit,
    onEventClick: (Event) -> Unit,
    onMenuClick: (Event, View) -> Unit
) : EventViewHolder(binding, onLikeClick, onEventClick, onMenuClick) {

    override fun bind(event: Event) {
        binding.authorName.text = event.author
        binding.content.text = event.content

        binding.typeEvent.text = when (event.type) {
            EventType.ONLINE -> binding.root.context.getString(R.string.online)
            EventType.OFFLINE -> binding.root.context.getString(R.string.offline)
        }
        binding.dateEvent.text = DateUtils.formatIsoDate(event.datetime)
        binding.datePublication.text = DateUtils.formatIsoDate(event.published)

        binding.imageContent.visibility = View.GONE
        binding.videoContent.visibility = View.GONE
        binding.audioContent.visibility = View.GONE

        binding.buttonPlayEvent.visibility = if (event.type == EventType.ONLINE) View.VISIBLE else View.GONE

        binding.buttonLike.isChecked = event.likedByMe
        binding.likeCount.text = event.likeCount.toString()
        enableLinks(binding.content)
        setupClicks(event)
    }
}

class ImageEventViewHolder(
    binding: CardEventBinding,
    onLikeClick: (String, Boolean) -> Unit,
    onEventClick: (Event) -> Unit,
    onMenuClick: (Event, anchor: View) -> Unit
) : EventViewHolder(binding, onLikeClick, onEventClick, onMenuClick) {

    override fun bind(event: Event) {
        binding.authorName.text = event.author
        binding.content.text = event.content

        binding.typeEvent.text = when (event.type) {
            EventType.ONLINE -> binding.root.context.getString(R.string.online)
            EventType.OFFLINE -> binding.root.context.getString(R.string.offline)
        }
        binding.dateEvent.text = DateUtils.formatIsoDate(event.datetime)
        binding.datePublication.text = DateUtils.formatIsoDate(event.published)

        loadAvatar(event.authorAvatar)

        binding.imageContent.visibility = View.VISIBLE
        binding.videoContent.visibility = View.GONE
        binding.audioContent.visibility = View.GONE
        binding.buttonPlayEvent.visibility = if (event.type == EventType.ONLINE) View.VISIBLE else View.GONE

        val imageUrl = event.attachment?.url?.trim()
        if (!imageUrl.isNullOrBlank()) {
            binding.imageContent.load(
                url = imageUrl,
                placeholder = R.drawable.ic_image_24,
                error = R.drawable.ic_broken_image_24,
                centerCrop = true
            )
        } else {
            binding.imageContent.visibility = View.GONE
        }

        binding.buttonLike.isChecked = event.likedByMe
        binding.likeCount.text = event.likeCount.toString()
        enableLinks(binding.content)
        setupClicks(event)
    }

    override fun onViewRecycled() {
        super.onViewRecycled()
        binding.imageContent.clearImage()
    }
}

class VideoEventViewHolder(
    binding: CardEventBinding,
    onLikeClick: (String, Boolean) -> Unit,
    onEventClick: (Event) -> Unit,
    onMenuClick: (Event, anchor: View) -> Unit,
    private val playerManager: VideoPlayerManager
) : EventViewHolder(binding, onLikeClick, onEventClick, onMenuClick) {

    private var isPlayerAttached = false

    override fun bind(event: Event) {
        binding.authorName.text = event.author
        binding.content.text = event.content

        binding.typeEvent.text = when (event.type) {
            EventType.ONLINE -> binding.root.context.getString(R.string.online)
            EventType.OFFLINE -> binding.root.context.getString(R.string.offline)
        }
        binding.dateEvent.text = DateUtils.formatIsoDate(event.datetime)
        binding.datePublication.text = DateUtils.formatIsoDate(event.published)

        loadAvatar(event.authorAvatar)

        binding.imageContent.visibility = View.GONE
        binding.videoContent.visibility = View.VISIBLE
        binding.audioContent.visibility = View.GONE
        binding.buttonPlayEvent.visibility = if (event.type == EventType.ONLINE) View.VISIBLE else View.GONE
        setupPlayerView(event.attachment?.url?.trim())

        binding.buttonLike.isChecked = event.likedByMe
        binding.likeCount.text = event.likeCount.toString()
        enableLinks(binding.content)
        setupClicks(event)
    }

    fun attachPlayer() {
        if (!isPlayerAttached) {
            binding.videoContent.player = playerManager.getPlayer()
            isPlayerAttached = true
        }
    }

    fun detachPlayer() {
        if (isPlayerAttached) {
            playerManager.pause()
            binding.videoContent.player = null
            isPlayerAttached = false
        }
    }

    override fun onViewRecycled() {
        super.onViewRecycled()
        detachPlayer()
    }

    private fun setupPlayerView(url: String?) {
        if (url.isNullOrBlank()) {
            binding.videoContent.visibility = View.GONE
            return
        }
        binding.videoContent.visibility = View.VISIBLE
        playerManager.setMediaUrl(url)
        binding.videoContent.useController = true
    }
}

class AudioEventViewHolder(
    binding: CardEventBinding,
    onLikeClick: (String, Boolean) -> Unit,
    onEventClick: (Event) -> Unit,
    onMenuClick: (Event, anchor: View) -> Unit,
    private val playerManager: VideoPlayerManager
) : EventViewHolder(binding, onLikeClick, onEventClick, onMenuClick) {

    private var currentEventId: String? = null
    private var isBound = false

    override fun bind(event: Event) {
        binding.authorName.text = event.author
        binding.content.text = event.content

        binding.typeEvent.text = when (event.type) {
            EventType.ONLINE -> binding.root.context.getString(R.string.online)
            EventType.OFFLINE -> binding.root.context.getString(R.string.offline)
        }
        binding.dateEvent.text = DateUtils.formatIsoDate(event.datetime)
        binding.datePublication.text = DateUtils.formatIsoDate(event.published)

        loadAvatar(event.authorAvatar)

        binding.imageContent.visibility = View.GONE
        binding.videoContent.visibility = View.GONE
        binding.audioContent.visibility = View.VISIBLE
        binding.buttonPlayEvent.visibility = if (event.type == EventType.ONLINE) View.VISIBLE else View.GONE

        setupAudioPlayer(event, event.attachment?.url?.trim())

        binding.buttonLike.isChecked = event.likedByMe
        binding.likeCount.text = event.likeCount.toString()
        enableLinks(binding.content)
        setupClicks(event)
    }

    private fun setupAudioPlayer(event: Event, url: String?) {
        currentEventId = event.id
        isBound = true

        binding.playPauseAudio.setOnClickListener {
            if (!url.isNullOrBlank()) {
                if (playerManager.isPlaying() && currentEventId == event.id) {
                    playerManager.pause()
                } else {
                    playerManager.setMediaUrl(url)
                    playerManager.play()
                }
                updatePlayButtonIcon()
            }
        }

        playerManager.onPlaybackStateChanged = { isPlaying ->
            if (isBound && currentEventId == event.id) {
                updatePlayButtonIcon()
            }
        }
        updatePlayButtonIcon()
    }

    private fun updatePlayButtonIcon() {
        val isPlaying = playerManager.isPlaying()
        val isCurrent = currentEventId != null

        binding.playPauseAudio.icon = if (isPlaying && isCurrent) {
            binding.root.context.getDrawable(R.drawable.ic_pause_24)
        } else {
            binding.root.context.getDrawable(R.drawable.ic_play_arrow_24)
        }
    }

    fun detachAudio() {
        isBound = false
        playerManager.onPlaybackStateChanged = null
    }

    override fun onViewRecycled() {
        super.onViewRecycled()
        detachAudio()
    }
}