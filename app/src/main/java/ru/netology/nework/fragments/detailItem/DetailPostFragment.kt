package ru.netology.nework.fragments.detailItem

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentDetailPostBinding
import ru.netology.nework.domain.model.AttachmentType
import ru.netology.nework.domain.model.Post
import ru.netology.nework.domain.model.User as DomainUser
import ru.netology.nework.presentation.detailpost.DetailPostUiState
import ru.netology.nework.presentation.detailpost.DetailPostViewModel
import ru.netology.nework.presentation.detailpost.adapter.AvatarAdapter
import ru.netology.nework.util.DateUtils
import ru.netology.nework.util.VideoPlayerManager
import ru.netology.nework.util.load
import javax.inject.Inject

@AndroidEntryPoint
class DetailPostFragment : Fragment(R.layout.fragment_detail_post) {

    private val viewModel: DetailPostViewModel by viewModels()

    @Inject
    lateinit var playerManager: VideoPlayerManager

    private var _binding: FragmentDetailPostBinding? = null
    private val binding get() = _binding!!
    private var currentAudioUrl: String? = null

    private val likersAdapter: AvatarAdapter by lazy { AvatarAdapter() }
    private val mentionedAdapter: AvatarAdapter by lazy { AvatarAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailPostBinding.bind(view)

        setupRecyclerViews()
        setupObservers()
        setupToolbar()
        setupClicks()
    }

    private fun setupRecyclerViews() {
        binding.recyclerLikers.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerLikers.adapter = likersAdapter

        binding.recyclerMentioned.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerMentioned.adapter = mentionedAdapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is DetailPostUiState.Loading -> {}
                        is DetailPostUiState.Success -> {
                            bindPost(state.post)
                        }
                        is DetailPostUiState.Error -> {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.connection_error),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun bindPost(post: Post) {
        binding.authorName.text = post.author
        binding.lastWork.text = post.authorJob ?: getString(R.string.in_search_work)

        binding.avatar.load(
            url = post.authorAvatar?.trim(),
            placeholder = R.drawable.ic_account_circle_24,
            error = R.drawable.ic_account_circle_24,
            roundedCorners = 24
        )

        binding.datePublished.text = DateUtils.formatIsoDate(
            isoString = post.published,
            errorText = getString(R.string.date_error)
        )
        binding.content.text = post.content

        binding.buttonLike.isChecked = post.likedByMe

        val likers = post.users.values.map { preview ->
            DomainUser(
                id = "",
                login = "",
                name = preview.name,
                avatar = preview.avatar
            )
        }.take(10)
        likersAdapter.submitList(likers)

        val mentionedUsers = post.users
            .filterKeys { it in post.mentionIds }
            .values
            .map { preview ->
                DomainUser(
                    id = "",
                    login = "",
                    name = preview.name,
                    avatar = preview.avatar
                )
            }
        mentionedAdapter.submitList(mentionedUsers)

        setupAttachment(post)
        setupMap(post)
    }

    private fun setupAttachment(post: Post) {
        when (post.attachment?.type) {
            AttachmentType.IMAGE -> {
                binding.imageContent.visibility = View.VISIBLE
                binding.videoContent.visibility = View.GONE
                binding.audioContent.visibility = View.GONE

                val imageUrl = post.attachment.url?.trim()
                if (!imageUrl.isNullOrBlank()) {
                    binding.imageContent.load(
                        url = imageUrl,
                        placeholder = R.drawable.ic_image_24,
                        error = R.drawable.ic_broken_image_24,
                        centerCrop = true
                    )
                }
            }
            AttachmentType.VIDEO -> {
                binding.imageContent.visibility = View.GONE
                binding.videoContent.visibility = View.VISIBLE
                binding.audioContent.visibility = View.GONE

                val videoUrl = post.attachment.url?.trim()
                if (!videoUrl.isNullOrBlank()) {
                    binding.videoContent.player = playerManager.getPlayer()
                    playerManager.setMediaUrl(videoUrl)
                }
            }
            AttachmentType.AUDIO -> {
                binding.imageContent.visibility = View.GONE
                binding.videoContent.visibility = View.GONE
                binding.audioContent.visibility = View.VISIBLE

                currentAudioUrl = post.attachment.url?.trim()
            }
            else -> {
                binding.imageContent.visibility = View.GONE
                binding.videoContent.visibility = View.GONE
                binding.audioContent.visibility = View.GONE
            }
        }
    }

    private fun setupMap(post: Post) {
        if (post.coords != null && post.coords.lat != 0.0 && post.coords.long != 0.0) {
            binding.map.visibility = View.VISIBLE

            try {
                MapKitFactory.initialize(requireContext())
            } catch (_: IllegalStateException) {
            }

            binding.map.mapWindow.map.move(
                CameraPosition(
                    Point(post.coords.lat, post.coords.long),
                    15f,
                    0f,
                    0f
                )
            )
        } else {
            binding.map.visibility = View.GONE
        }
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            val sourceTab = arguments?.getInt("sourceTab")

            val navController = findNavController()
            val backStackEntry = navController.getBackStackEntry(R.id.mainFragment)
            backStackEntry.savedStateHandle.set("restoreTab", sourceTab)

            navController.navigateUp()
        }
    }

    private fun setupClicks() {
        binding.buttonLike.setOnClickListener { viewModel.toggleLike() }

        binding.playPauseAudio.setOnClickListener {
            if (playerManager.isPlaying()) {
                playerManager.pause()
            } else {
                currentAudioUrl?.let { url ->
                    playerManager.setMediaUrl(url)
                    playerManager.play()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        playerManager.release()
        binding.videoContent.player = null
        binding.recyclerLikers.adapter = null
        binding.recyclerMentioned.adapter = null
        currentAudioUrl = null
        _binding = null

    }
}