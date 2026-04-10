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
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentDetailEventBinding
import ru.netology.nework.domain.model.AttachmentType
import ru.netology.nework.domain.model.Event
import ru.netology.nework.domain.model.EventType
import ru.netology.nework.domain.model.User as DomainUser
import ru.netology.nework.presentation.detailevent.DetailEventUiState
import ru.netology.nework.presentation.detailevent.DetailEventViewModel
import ru.netology.nework.presentation.detailpost.adapter.AvatarAdapter
import ru.netology.nework.util.DateUtils
import ru.netology.nework.util.VideoPlayerManager
import ru.netology.nework.util.load
import javax.inject.Inject

@AndroidEntryPoint
class DetailEventFragment : Fragment(R.layout.fragment_detail_event) {

    private val viewModel: DetailEventViewModel by viewModels()

    @Inject
    lateinit var playerManager: VideoPlayerManager

    private var _binding: FragmentDetailEventBinding? = null
    private val binding get() = _binding!!
    private var currentAudioUrl: String? = null

    private val speakersAdapter: AvatarAdapter by lazy { AvatarAdapter() }
    private val likersAdapter: AvatarAdapter by lazy { AvatarAdapter() }
    private val participantsAdapter: AvatarAdapter by lazy { AvatarAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailEventBinding.bind(view)

        setupRecyclerViews()
        setupObservers()
        setupToolbar()
        setupClicks()
    }

    private fun setupRecyclerViews() {
        binding.recyclerSpeaker.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerSpeaker.adapter = speakersAdapter

        binding.recyclerLikers.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerLikers.adapter = likersAdapter

        binding.recyclerParticipant.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerParticipant.adapter = participantsAdapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is DetailEventUiState.Loading -> {}
                        is DetailEventUiState.Success -> {
                            bindEvent(state.event)
                        }
                        is DetailEventUiState.Error -> {
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

    private fun bindEvent(event: Event) {
        binding.authorName.text = event.author
        binding.lastWork.text = event.authorJob ?: getString(R.string.in_search_work)

        binding.avatar.load(
            url = event.authorAvatar?.trim(),
            placeholder = R.drawable.ic_account_circle_24,
            error = R.drawable.ic_account_circle_24,
            roundedCorners = 24
        )

        binding.typeEvent.text = when (event.type) {
            EventType.ONLINE -> getString(R.string.online)
            EventType.OFFLINE -> getString(R.string.offline)
        }
        binding.dateEvent.text = DateUtils.formatIsoDate(event.datetime)

        binding.content.text = event.content

        binding.buttonLike.isChecked = event.likedByMe

        binding.participantsButton.isChecked = event.participatedByMe
        binding.participantsHeader.text = getString(R.string.participants_count, event.participantsIds.size)

        val speakers = event.users.filterKeys { it in event.speakerIds }.values.map { preview ->
            DomainUser(
                id = "",
                login = "",
                name = preview.name,
                avatar = preview.avatar
            )
        }
        speakersAdapter.submitList(speakers)

        val likers = event.users.values.map { preview ->
            DomainUser(
                id = "",
                login = "",
                name = preview.name,
                avatar = preview.avatar
            )
        }.take(10)
        likersAdapter.submitList(likers)

        val participants = event.users.filterKeys { it in event.participantsIds }.values.map { preview ->
            DomainUser(
                id = "",
                login = "",
                name = preview.name,
                avatar = preview.avatar
            )
        }
        participantsAdapter.submitList(participants)

        setupAttachment(event)
        setupMap(event)

        binding.buttonPlayEvent.visibility = if (event.type == EventType.ONLINE) View.VISIBLE else View.GONE
    }

    private fun setupAttachment(event: Event) {
        when (event.attachment?.type) {
            AttachmentType.IMAGE -> {
                binding.imageContent.visibility = View.VISIBLE
                binding.videoContent.visibility = View.GONE
                binding.audioContent.visibility = View.GONE

                val imageUrl = event.attachment.url?.trim()
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

                val videoUrl = event.attachment.url?.trim()
                if (!videoUrl.isNullOrBlank()) {
                    binding.videoContent.player = playerManager.getPlayer()
                    playerManager.setMediaUrl(videoUrl)
                }
            }
            AttachmentType.AUDIO -> {
                binding.imageContent.visibility = View.GONE
                binding.videoContent.visibility = View.GONE
                binding.audioContent.visibility = View.VISIBLE

                currentAudioUrl = event.attachment.url?.trim()
            }
            else -> {
                binding.imageContent.visibility = View.GONE
                binding.videoContent.visibility = View.GONE
                binding.audioContent.visibility = View.GONE
            }
        }
    }

    private fun setupMap(event: Event) {
        if (event.coords != null && event.coords.lat != 0.0 && event.coords.long != 0.0) {
            binding.map.visibility = View.VISIBLE

            try {
                MapKitFactory.initialize(requireContext())
            } catch (_: IllegalStateException) { }

            binding.map.mapWindow.map.move(
                CameraPosition(
                    Point(event.coords.lat, event.coords.long),
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
        binding.participantsButton.setOnClickListener { viewModel.toggleParticipation() }

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
        binding.recyclerSpeaker.adapter = null
        binding.recyclerLikers.adapter = null
        binding.recyclerParticipant.adapter = null
        currentAudioUrl = null
        _binding = null
    }
}