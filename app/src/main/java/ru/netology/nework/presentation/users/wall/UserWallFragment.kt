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
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.data.local.TokenStorage
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.domain.model.Post
import ru.netology.nework.presentation.feed.adapter.PostAdapter
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
            playerManager = playerManager
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPostsBinding.bind(view)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewPost.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPost.adapter = adapter

        lifecycleScope.launch {
            adapter.loadStateFlow.collect { loadState ->
                when (loadState.refresh) {
                    is LoadState.Loading -> binding.swipeRefresh.isRefreshing = true
                    is LoadState.Error -> {
                        binding.swipeRefresh.isRefreshing = false
                        Snackbar.make(
                            binding.root,
                            R.string.connection_error,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    else -> binding.swipeRefresh.isRefreshing = false
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        playerManager.release()
    }
}