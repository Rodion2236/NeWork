package ru.netology.nework.presentation.events.adapter

import android.content.Intent
import android.net.Uri
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
    private val onJoinClick: (String) -> Unit,
    private val playerManager: VideoPlayerManager,
    private val currentUserId: String?
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
            VIEW_TYPE_IMAGE -> ImageEventViewHolder(
                binding, onLikeClick, onEventClick, onMenuClick, onJoinClick, currentUserId
            )
            VIEW_TYPE_VIDEO -> VideoEventViewHolder(
                binding, onLikeClick, onEventClick, onMenuClick, onJoinClick, currentUserId, playerManager
            )
            VIEW_TYPE_AUDIO -> AudioEventViewHolder(
                binding, onLikeClick, onEventClick, onMenuClick, onJoinClick, currentUserId, playerManager
            )
            else -> TextEventViewHolder(
                binding, onLikeClick, onEventClick, onMenuClick, onJoinClick, currentUserId
            )
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
    private val onMenuClick: (Event, anchor: View) -> Unit,
    private val onJoinClick: (String) -> Unit,
    private val currentUserId: String?
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

    protected fun isLikedByCurrentUser(event: Event): Boolean {
        return currentUserId != null && event.likeOwnerIds.contains(currentUserId)
    }

    protected fun setupClicks(event: Event) {
        binding.root.setOnClickListener { onEventClick(event) }

        binding.buttonLike.setOnClickListener {
            val currentLiked = isLikedByCurrentUser(event)
            val newLiked = !currentLiked
            binding.buttonLike.isChecked = newLiked
            binding.likeCount.text = (event.likeCount + if (newLiked) 1 else -1).toString()
            onLikeClick(event.id, currentLiked)
        }

        binding.buttonOption.visibility = if (event.authorId == currentUserId) View.VISIBLE else View.GONE
        binding.buttonOption.setOnClickListener { view ->
            if (event.authorId == currentUserId) {
                onMenuClick(event, view)
            }
        }

        binding.buttonGroup.setOnClickListener {
            onJoinClick(event.id)
        }

        binding.buttonPlayEvent.visibility = if (event.type == EventType.ONLINE && !event.link.isNullOrBlank()) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.buttonPlayEvent.setOnClickListener {
            event.link?.let { link ->
                val url = if (link.startsWith("http")) link else "https://$link"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                binding.root.context.startActivity(intent)
            }
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
    onMenuClick: (Event, View) -> Unit,
    onJoinClick: (String) -> Unit,
    currentUserId: String?
) : EventViewHolder(binding, onLikeClick, onEventClick, onMenuClick, onJoinClick, currentUserId) {

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

        val isLiked = isLikedByCurrentUser(event)
        binding.buttonLike.isChecked = isLiked
        binding.likeCount.text = event.likeCount.toString()

        enableLinks(binding.content)
        setupClicks(event)
    }
}

class ImageEventViewHolder(
    binding: CardEventBinding,
    onLikeClick: (String, Boolean) -> Unit,
    onEventClick: (Event) -> Unit,
    onMenuClick: (Event, anchor: View) -> Unit,
    onJoinClick: (String) -> Unit,
    currentUserId: String?
) : EventViewHolder(binding, onLikeClick, onEventClick, onMenuClick, onJoinClick, currentUserId) {

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

        val isLiked = isLikedByCurrentUser(event)
        binding.buttonLike.isChecked = isLiked
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
    onJoinClick: (String) -> Unit,
    currentUserId: String?,
    private val playerManager: VideoPlayerManager
) : EventViewHolder(binding, onLikeClick, onEventClick, onMenuClick, onJoinClick, currentUserId) {

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

        val videoUrl = event.attachment?.url?.trim()
        if (!videoUrl.isNullOrBlank()) {
            binding.videoContent.visibility = View.VISIBLE
            playerManager.setMediaUrl(videoUrl)
        } else {
            binding.videoContent.visibility = View.GONE
        }

        val isLiked = isLikedByCurrentUser(event)
        binding.buttonLike.isChecked = isLiked
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
}

class AudioEventViewHolder(
    binding: CardEventBinding,
    onLikeClick: (String, Boolean) -> Unit,
    onEventClick: (Event) -> Unit,
    onMenuClick: (Event, anchor: View) -> Unit,
    onJoinClick: (String) -> Unit,
    currentUserId: String?,
    private val playerManager: VideoPlayerManager
) : EventViewHolder(binding, onLikeClick, onEventClick, onMenuClick, onJoinClick, currentUserId) {

    private var currentEventId: String? = null
    private var currentAudioUrl: String? = null
    private var isBound = false

    init {
        binding.playPauseAudio.setOnClickListener {
            currentAudioUrl?.let { url ->
                if (playerManager.isPlaying() && currentEventId != null) {
                    playerManager.pause()
                } else {
                    playerManager.setMediaUrl(url)
                    playerManager.play()
                }
                updatePlayButtonIcon()
            }
        }
    }

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

        currentEventId = event.id
        currentAudioUrl = event.attachment?.url?.trim()
        isBound = true

        val isLiked = isLikedByCurrentUser(event)
        binding.buttonLike.isChecked = isLiked
        binding.likeCount.text = event.likeCount.toString()

        enableLinks(binding.content)
        setupClicks(event)
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
    }

    override fun onViewRecycled() {
        super.onViewRecycled()
        detachAudio()
    }
}