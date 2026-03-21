package ru.netology.nework.presentation.feed.adapter

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardPostBinding
import ru.netology.nework.domain.model.AttachmentType
import ru.netology.nework.domain.model.Post
import ru.netology.nework.util.DateUtils
import ru.netology.nework.util.VideoPlayerManager
import ru.netology.nework.util.clearImage
import ru.netology.nework.util.load

class PostAdapter(
    private val onLikeClick: (String, Boolean) -> Unit,
    private val onPostClick: (Post) -> Unit,
    private val onMenuClick: (Post, anchor: View) -> Unit,
    private val playerManager: VideoPlayerManager,
    private val currentUserId: String?
) : PagingDataAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)?.attachment?.type) {
            AttachmentType.IMAGE -> VIEW_TYPE_IMAGE
            AttachmentType.VIDEO -> VIEW_TYPE_VIDEO
            AttachmentType.AUDIO -> VIEW_TYPE_AUDIO
            else -> VIEW_TYPE_TEXT
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return when (viewType) {
            VIEW_TYPE_IMAGE -> ImagePostViewHolder(binding, onLikeClick, onPostClick, onMenuClick, currentUserId)
            VIEW_TYPE_VIDEO -> VideoPostViewHolder(binding, onLikeClick, onPostClick, onMenuClick, playerManager, currentUserId)
            VIEW_TYPE_AUDIO -> AudioPostViewHolder(binding, onLikeClick, onPostClick, onMenuClick, playerManager, currentUserId)
            else -> TextPostViewHolder(binding, onLikeClick, onPostClick, onMenuClick, currentUserId)
        }
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
            if (holder is VideoPostViewHolder) {
                holder.attachPlayer()
            }
        }
    }

    override fun onViewRecycled(holder: PostViewHolder) {
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

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
}

abstract class PostViewHolder(
    protected val binding: CardPostBinding,
    private val onLikeClick: (String, Boolean) -> Unit,
    private val onPostClick: (Post) -> Unit,
    private val onMenuClick: (Post, anchor: View) -> Unit,
    protected val currentUserId: String?
) : RecyclerView.ViewHolder(binding.root) {

    abstract fun bind(post: Post)

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

    protected fun setupClicks(post: Post) {
        binding.root.setOnClickListener { onPostClick(post) }
        binding.buttonLike.setOnClickListener {
            onLikeClick(post.id, post.likedByMe)
        }
        binding.buttonOption.setOnClickListener { view ->
            onMenuClick(post, view)
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

class TextPostViewHolder(
    binding: CardPostBinding,
    onLikeClick: (String, Boolean) -> Unit,
    onPostClick: (Post) -> Unit,
    onMenuClick: (Post, View) -> Unit,
    currentUserId: String?
) : PostViewHolder(binding, onLikeClick, onPostClick, onMenuClick, currentUserId) {

    override fun bind(post: Post) {
        binding.authorName.text = post.author
        binding.content.text = post.content
        binding.datePublication.text = DateUtils.formatIsoDate(post.published)
        binding.imageContent.visibility = View.GONE
        binding.videoContent.visibility = View.GONE
        binding.audioContent.visibility = View.GONE
        binding.buttonLike.isChecked = post.likedByMe
        binding.likeCount.text = post.likeCount.toString()
        binding.buttonOption.visibility = if (post.authorId == currentUserId) {
            View.VISIBLE
        } else {
            View.GONE
        }

        enableLinks(binding.content)
        setupClicks(post)
    }
}

class ImagePostViewHolder(
    binding: CardPostBinding,
    onLikeClick: (String, Boolean) -> Unit,
    onPostClick: (Post) -> Unit,
    onMenuClick: (Post, anchor: View) -> Unit,
    currentUserId: String?
) : PostViewHolder(binding, onLikeClick, onPostClick, onMenuClick, currentUserId) {

    override fun bind(post: Post) {
        binding.authorName.text = post.author
        binding.content.text = post.content
        binding.datePublication.text = DateUtils.formatIsoDate(post.published)

        loadAvatar(post.authorAvatar)

        binding.imageContent.visibility = View.VISIBLE
        binding.videoContent.visibility = View.GONE
        binding.audioContent.visibility = View.GONE

        binding.imageContent.load(
            url = post.attachment?.url,
            placeholder = R.drawable.ic_image_24,
            error = R.drawable.ic_broken_image_24,
            centerCrop = true
        )

        binding.buttonLike.isChecked = post.likedByMe
        binding.likeCount.text = post.likeCount.toString()
        binding.buttonOption.visibility = if (post.authorId == currentUserId) {
            View.VISIBLE
        } else {
            View.GONE
        }

        enableLinks(binding.content)
        setupClicks(post)
    }

    override fun onViewRecycled() {
        super.onViewRecycled()
        binding.imageContent.clearImage()
    }
}

class VideoPostViewHolder(
    binding: CardPostBinding,
    onLikeClick: (String, Boolean) -> Unit,
    onPostClick: (Post) -> Unit,
    onMenuClick: (Post, anchor: View) -> Unit,
    private val playerManager: VideoPlayerManager,
    currentUserId: String?
) : PostViewHolder(binding, onLikeClick, onPostClick, onMenuClick, currentUserId) {

    private var isPlayerAttached = false

    override fun bind(post: Post) {
        binding.authorName.text = post.author
        binding.content.text = post.content
        binding.datePublication.text = DateUtils.formatIsoDate(post.published)

        loadAvatar(post.authorAvatar)

        binding.imageContent.visibility = View.GONE
        binding.videoContent.visibility = View.VISIBLE
        binding.audioContent.visibility = View.GONE

        setupPlayerView(post.attachment?.url)

        binding.buttonLike.isChecked = post.likedByMe
        binding.likeCount.text = post.likeCount.toString()
        binding.buttonOption.visibility = if (post.authorId == currentUserId) {
            View.VISIBLE
        } else {
            View.GONE
        }

        enableLinks(binding.content)
        setupClicks(post)
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

class AudioPostViewHolder(
    binding: CardPostBinding,
    onLikeClick: (String, Boolean) -> Unit,
    onPostClick: (Post) -> Unit,
    onMenuClick: (Post, anchor: View) -> Unit,
    private val playerManager: VideoPlayerManager,
    currentUserId: String?
) : PostViewHolder(binding, onLikeClick, onPostClick, onMenuClick, currentUserId) {

    private var currentPostId: String? = null
    private var isBound = false

    override fun bind(post: Post) {
        binding.authorName.text = post.author
        binding.content.text = post.content
        binding.datePublication.text = DateUtils.formatIsoDate(post.published)

        loadAvatar(post.authorAvatar)

        binding.imageContent.visibility = View.GONE
        binding.videoContent.visibility = View.GONE
        binding.audioContent.visibility = View.VISIBLE

        setupAudioPlayer(post)

        binding.buttonLike.isChecked = post.likedByMe
        binding.likeCount.text = post.likeCount.toString()
        binding.buttonOption.visibility = if (post.authorId == currentUserId) {
            View.VISIBLE
        } else {
            View.GONE
        }

        enableLinks(binding.content)
        setupClicks(post)
    }

    private fun setupAudioPlayer(post: Post) {
        currentPostId = post.id
        isBound = true

        binding.playPauseAudio.setOnClickListener {
            if (post.attachment?.url != null) {
                if (playerManager.isPlaying() && currentPostId == post.id) {
                    playerManager.pause()
                } else {
                    playerManager.setMediaUrl(post.attachment.url)
                    playerManager.play()
                }
                updatePlayButtonIcon()
            }
        }

        playerManager.onPlaybackStateChanged = { isPlaying ->
            if (isBound && currentPostId == post.id) {
                updatePlayButtonIcon()
            }
        }
        updatePlayButtonIcon()
    }

    private fun updatePlayButtonIcon() {
        val isPlaying = playerManager.isPlaying()
        val isCurrent = currentPostId != null

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