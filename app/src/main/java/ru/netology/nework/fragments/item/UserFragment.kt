package ru.netology.nework.fragments.item

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentUserBinding
import ru.netology.nework.domain.model.User
import ru.netology.nework.fragments.main.MainFragment
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
            onUserClick = { user -> openUserDetail(user) },
            showCheckbox = false,
            onUserToggle = null
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
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.users.collect { users ->
                    adapter.submitList(users)
                    if (binding.swipeRefresh.isRefreshing) {
                        binding.swipeRefresh.isRefreshing = false
                    }
                }
            }
        }
    }

    private fun setupClicks() {
        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                try {
                    viewModel.refresh()
                } finally {
                    if (binding.swipeRefresh.isRefreshing) {
                        binding.swipeRefresh.isRefreshing = false
                    }
                }
            }
        }
    }

    private fun openUserDetail(user: User) {
        val mainFragment = parentFragment as? MainFragment
        val sourceTab = mainFragment?.getCurrentTabIndex() ?: 0

        val bundle = Bundle().apply {
            putString(BundleKeys.USER_ID, user.id)
            putInt("sourceTab", sourceTab)
        }
        findNavController().navigate(R.id.action_global_to_detailUserFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}