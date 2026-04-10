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
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.domain.model.Post
import ru.netology.nework.presentation.feed.FeedUiState
import ru.netology.nework.presentation.feed.FeedViewModel
import ru.netology.nework.presentation.feed.adapter.PostAdapter
import ru.netology.nework.util.BundleKeys
import ru.netology.nework.util.VideoPlayerManager
import javax.inject.Inject

@AndroidEntryPoint
class PostsFragment : Fragment(R.layout.fragment_posts) {

    private val viewModel: FeedViewModel by viewModels()

    @Inject
    lateinit var playerManager: VideoPlayerManager

    @Inject
    lateinit var tokenStorage: TokenStorage

    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!

    private val adapter: PostAdapter by lazy {
        PostAdapter(
            onLikeClick = { id, liked -> viewModel.toggleLike(id, liked) },
            onPostClick = { post -> openPostDetail(post) },
            onMenuClick = { post, anchor -> showPostMenu(post, anchor) },
            playerManager = playerManager,
            currentUserId = tokenStorage.getUserId()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPostsBinding.bind(view)

        setupRecyclerView()
        setupObservers()
        setupClicks()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewPost.adapter = adapter

        lifecycleScope.launch {
            adapter.loadStateFlow.collect { loadState ->
                when (loadState.refresh) {
                    is LoadState.Loading -> {
                        binding.swipeRefresh.isRefreshing = true
                    }
                    is LoadState.NotLoading -> {
                        binding.swipeRefresh.isRefreshing = false
                    }
                    is LoadState.Error -> {
                        binding.swipeRefresh.isRefreshing = false
                        Snackbar.make(
                            binding.root,
                            R.string.connection_error,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.recyclerViewPost.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                viewModel.posts.collect { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is FeedUiState.Error -> Snackbar.make(
                            binding.root,
                            getString(R.string.connection_error),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        is FeedUiState.Success -> Snackbar.make(
                            binding.root,
                            getString(R.string.post_deleted),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        else -> {}
                    }
                }
            }
        }
    }

    private fun setupClicks() {
        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }

        binding.buttonNewPost.setOnClickListener {
            findNavController().navigate(R.id.newPostFragment)
        }
    }

    private fun showPostMenu(post: Post, anchor: View) {
        val currentUserId = tokenStorage.getUserId()
        if (post.authorId != currentUserId) {
            return
        }

        PopupMenu(requireContext(), anchor).apply {
            inflate(R.menu.post_options)

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.edit -> {
                        // TODO: Навигация на редактирование
                    }
                    R.id.delete -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.delete)
                            .setMessage(R.string.confirm_delete_post)
                            .setPositiveButton(R.string.ok) { _, _ ->
                                viewModel.deletePost(post.id)
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

    private fun openPostDetail(post: Post) {
        val bundle = Bundle().apply {
            putString(BundleKeys.POST_ID, post.id)
        }
        findNavController().navigate(R.id.action_global_to_detailPostFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        playerManager.release()
        binding.recyclerViewPost.adapter = null
        _binding = null
    }
}