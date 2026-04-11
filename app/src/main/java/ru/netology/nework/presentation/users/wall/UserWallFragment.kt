package ru.netology.nework.presentation.users.wall

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.data.local.TokenStorage
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.domain.model.Post
import ru.netology.nework.presentation.feed.adapter.PostAdapter
import ru.netology.nework.util.BundleKeys
import ru.netology.nework.util.VideoPlayerManager
import javax.inject.Inject

@AndroidEntryPoint
class UserWallFragment : Fragment(R.layout.fragment_posts) {

    companion object {
        private const val ARG_USER_ID = "userId"

        fun newInstance(userId: String): UserWallFragment {
            return UserWallFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
            }
        }
    }

    private val viewModel: UserWallViewModel by viewModels()

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

        val userId = arguments?.getString(ARG_USER_ID) ?: ""
        val currentUserId = tokenStorage.getUserId() ?: ""
        val isOwnProfile = userId == currentUserId

        setupRecyclerView()
        setupObservers()
        setupFab(isOwnProfile, userId)
    }

    private fun setupRecyclerView() {
        binding.recyclerViewPost.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPost.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collect { loadState ->
                    when (loadState.refresh) {
                        is LoadState.Loading -> {
                            if (!binding.swipeRefresh.isRefreshing) {
                                binding.swipeRefresh.isRefreshing = true
                            }
                        }
                        is LoadState.NotLoading, is LoadState.Error -> {
                            if (binding.swipeRefresh.isRefreshing) {
                                binding.swipeRefresh.isRefreshing = false
                            }
                            if (loadState.refresh is LoadState.Error) {
                                Snackbar.make(
                                    binding.root,
                                    R.string.connection_error,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            binding.swipeRefresh.setOnRefreshListener {
                viewModel.refresh()

                viewLifecycleOwner.lifecycleScope.launch {
                    delay(100)
                    if (_binding != null && binding.swipeRefresh.isRefreshing) {
                        binding.swipeRefresh.isRefreshing = false
                    }
                }
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.posts.collect { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
        }
    }

    private fun setupFab(isOwnProfile: Boolean, userId: String) {
        binding.buttonNewPost.visibility = if (isOwnProfile) View.VISIBLE else View.GONE
        binding.buttonNewPost.setOnClickListener {
            val bundle = Bundle().apply {
                putString(BundleKeys.USER_ID, userId)
            }
            findNavController().navigate(R.id.newPostFragment, bundle)
        }
    }

    private fun openPostDetail(post: Post) {
        val bundle = Bundle().apply {
            putString("postId", post.id)
        }
        findNavController().navigate(R.id.detailPostFragment, bundle)
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
                        val bundle = Bundle().apply {
                            putString(BundleKeys.POST_ID, post.id)
                            putBoolean("isEditMode", true)
                            putString("originalContent", post.content)

                            post.attachment?.let { attachment ->
                                putString(BundleKeys.ATTACHMENT_URL, attachment.url)
                                putString(BundleKeys.ATTACHMENT_TYPE, attachment.type.name)
                            }

                            post.coords?.let { coords ->
                                putDouble(BundleKeys.LAT, coords.lat)
                                putDouble(BundleKeys.LNG, coords.long)
                            }

                            putStringArrayList(BundleKeys.MENTION_IDS, ArrayList(post.mentionIds))
                        }
                        findNavController().navigate(R.id.newPostFragment, bundle)
                        true
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
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        playerManager.release()
    }
}