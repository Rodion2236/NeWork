package ru.netology.nework.fragments.item

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.data.local.TokenStorage
import ru.netology.nework.databinding.FragmentEventBinding
import ru.netology.nework.domain.model.Event
import ru.netology.nework.domain.model.Post
import ru.netology.nework.fragments.main.MainFragment
import ru.netology.nework.presentation.events.EventsUiState
import ru.netology.nework.presentation.events.EventsViewModel
import ru.netology.nework.presentation.events.adapter.EventAdapter
import ru.netology.nework.util.BundleKeys
import ru.netology.nework.util.VideoPlayerManager
import javax.inject.Inject

@AndroidEntryPoint
class EventsFragment : Fragment(R.layout.fragment_event) {

    private val viewModel: EventsViewModel by viewModels()

    @Inject
    lateinit var playerManager: VideoPlayerManager

    @Inject
    lateinit var tokenStorage: TokenStorage

    private var _binding: FragmentEventBinding? = null
    private val binding get() = _binding!!

    private val adapter: EventAdapter by lazy {
        EventAdapter(
            onLikeClick = { id, liked -> viewModel.toggleLike(id, liked) },
            onEventClick = { event -> openEventDetail(event) },
            onMenuClick = { event, anchor -> showEventMenu(event, anchor) },
            playerManager = playerManager
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEventBinding.bind(view)

        setupRecyclerView()
        setupObservers()
        setupClicks()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewEvent.adapter = adapter

        lifecycleScope.launch {
            adapter.loadStateFlow.collect { loadState ->
                when (loadState.refresh) {
                    is LoadState.Loading -> binding.swipeRefreshEvent.isRefreshing = true
                    is LoadState.NotLoading -> binding.swipeRefreshEvent.isRefreshing = false
                    is LoadState.Error -> {
                        binding.swipeRefreshEvent.isRefreshing = false
                        Snackbar.make(
                            binding.root,
                            R.string.connection_error,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.recyclerViewEvent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    playerManager.pause()
                }
            }
        })
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is EventsUiState.Error -> Snackbar.make(
                            binding.root,
                            getString(R.string.connection_error),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        is EventsUiState.Success -> Snackbar.make(
                            binding.root,
                            getString(R.string.event_action_success),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        else -> {}
                    }
                }
            }
        }
    }

    private fun setupClicks() {
        binding.swipeRefreshEvent.setOnRefreshListener {
            adapter.refresh()
        }

        binding.buttonNewEvent.setOnClickListener {
            findNavController().navigate(R.id.newEventFragment)
        }
    }

    private fun showEventMenu(event: Event, anchor: View) {
        val currentUserId = tokenStorage.getUserId()
        if (event.authorId != currentUserId) {
            return
        }

        PopupMenu(requireContext(), anchor).apply {
            inflate(R.menu.event_options)

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.edit -> {
                        // TODO: Навигация на редактирование
                    }
                    R.id.delete -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.delete)
                            .setMessage(R.string.confirm_delete_event)
                            .setPositiveButton(R.string.ok) { _, _ ->
                                viewModel.deleteEvent(event.id)
                            }
                            .setNegativeButton(R.string.cancel, null)
                            .show()
                    }
                }
                true
            }
            show()
        }
    }

    private fun openEventDetail(event: Event) {
        val mainFragment = parentFragment as? MainFragment
        val sourceTab = mainFragment?.getCurrentTabIndex() ?: 0

        val bundle = Bundle().apply {
            putString(BundleKeys.EVENT_ID, event.id)
            putInt("sourceTab", sourceTab)
        }
        findNavController().navigate(R.id.action_global_to_detailEventFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        playerManager.release()
        binding.recyclerViewEvent.adapter = null
        _binding = null
    }
}