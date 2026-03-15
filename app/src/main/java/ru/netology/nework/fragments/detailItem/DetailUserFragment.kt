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
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentDetailUserBinding
import ru.netology.nework.presentation.users.DetailUserUiState
import ru.netology.nework.presentation.users.DetailUserViewModel
import ru.netology.nework.presentation.users.jobs.UserJobsFragment
import ru.netology.nework.presentation.users.wall.UserWallFragment
import ru.netology.nework.util.load

@AndroidEntryPoint
class DetailUserFragment : Fragment(R.layout.fragment_detail_user) {

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
                    1 -> UserJobsFragment.newInstance(userId)
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
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}