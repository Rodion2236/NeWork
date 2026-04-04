package ru.netology.nework.fragments.item

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentUserBinding
import ru.netology.nework.domain.model.User
import ru.netology.nework.presentation.users.UsersUiState
import ru.netology.nework.presentation.users.UsersViewModel
import ru.netology.nework.presentation.users.adapter.UserAdapter
import ru.netology.nework.util.BundleKeys

@AndroidEntryPoint
class UserFragment : Fragment(R.layout.fragment_user) {

    private val viewModel: UsersViewModel by viewModels()

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!

    private val adapter: UserAdapter by lazy {
        UserAdapter(
            onUserClick = { user -> openUserDetail(user) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserBinding.bind(view)

        setupRecyclerView()
        setupObservers()
        setupClicks()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewUser.adapter = adapter

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
                viewModel.users.collect { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UsersUiState.Error -> Snackbar.make(
                            binding.root,
                            getString(R.string.connection_error),
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
    }

    private fun openUserDetail(user: User) {
        val bundle = Bundle().apply {
            putString(BundleKeys.USER_ID, user.id)
        }
        findNavController().navigate(R.id.action_global_to_detailUserFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}