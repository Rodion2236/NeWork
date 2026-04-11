package ru.netology.nework.fragments.detailItem

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.data.local.TokenStorage
import ru.netology.nework.databinding.FragmentDetailUserBinding
import ru.netology.nework.fragments.item.JobsFragment
import ru.netology.nework.presentation.users.DetailUserUiState
import ru.netology.nework.presentation.users.DetailUserViewModel
import ru.netology.nework.presentation.users.wall.UserWallFragment
import ru.netology.nework.util.BundleKeys
import ru.netology.nework.util.load
import javax.inject.Inject

@AndroidEntryPoint
class DetailUserFragment : Fragment(R.layout.fragment_detail_user) {

    @Inject
    lateinit var tokenStorage: TokenStorage

    private val viewModel: DetailUserViewModel by viewModels()

    private var _binding: FragmentDetailUserBinding? = null
    private val binding get() = _binding!!

    private val tabTitles = listOf(R.string.wall, R.string.jobs)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailUserBinding.bind(view)

        setupViewPager()
        setupObservers()
        setupToolbar()
    }

    private fun setupViewPager() {
        binding.pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment {
                val userId = viewModel.userId
                return when (position) {
                    0 -> UserWallFragment.newInstance(userId)
                    1 -> {
                        JobsFragment().apply {
                            arguments = Bundle().apply {
                                putString(BundleKeys.USER_ID, userId)
                            }
                        }
                    }
                    else -> throw IllegalArgumentException("Invalid tab")
                }
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = getString(tabTitles[position])
        }.attach()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is DetailUserUiState.Loading -> {}
                        is DetailUserUiState.Success -> {
                            binding.topAppBar.title = state.user.name
                            binding.mainPhoto.load(
                                url = state.user.avatar,
                                placeholder = R.drawable.ic_account_circle_24,
                                error = R.drawable.ic_account_circle_24,
                                roundedCorners = 100
                            )
                        }
                        is DetailUserUiState.Error -> {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.user_not_found),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun setupToolbar() {
        val currentUserId = tokenStorage.getUserId()
        val isMyProfile = viewModel.userId == currentUserId

        if (isMyProfile) {
            binding.topAppBar.menu.clear()
            binding.topAppBar.inflateMenu(R.menu.user_menu)

            binding.topAppBar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.exit -> {
                        showLogoutConfirmation()
                        true
                    }
                    else -> false
                }
            }
        } else {
            binding.topAppBar.menu.clear()
        }

        binding.topAppBar.setNavigationOnClickListener {
            val sourceTab = arguments?.getInt("sourceTab")

            val navController = findNavController()
            val backStackEntry = navController.getBackStackEntry(R.id.mainFragment)
            backStackEntry.savedStateHandle.set("restoreTab", sourceTab)

            navController.navigateUp()
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_logout)
            .setPositiveButton(R.string.ok) { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun performLogout() {
        tokenStorage.clear()

        findNavController().apply {
            popBackStack(R.id.mainFragment, false)
            navigate(R.id.action_global_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}