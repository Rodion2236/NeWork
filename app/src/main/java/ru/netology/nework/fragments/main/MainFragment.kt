package ru.netology.nework.fragments.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.data.local.TokenStorage
import ru.netology.nework.databinding.FragmentMainBinding
import ru.netology.nework.fragments.item.EventsFragment
import ru.netology.nework.fragments.item.PostsFragment
import ru.netology.nework.fragments.item.UserFragment
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {

    @Inject
    lateinit var tokenStorage: TokenStorage

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainBinding.bind(view)

        setupToolbar()

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.postsFragment -> {
                    switchChildFragment(PostsFragment())
                    true
                }
                R.id.eventsFragment -> {
                    switchChildFragment(EventsFragment())
                    true
                }
                R.id.usersFragment -> {
                    switchChildFragment(UserFragment())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            switchChildFragment(PostsFragment())
            binding.bottomNavigation.selectedItemId = R.id.postsFragment
        }
    }

    private fun setupToolbar() {
        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.user -> {
                    navigateToMyProfile()
                    true
                }
                else -> false
            }
        }
    }

    private fun navigateToMyProfile() {
        val currentUserId = tokenStorage.getUserId()
        Log.d("MainFragment", "Navigating to profile with userId: $currentUserId")

        val bundle = Bundle().apply {
            putString("userId", currentUserId)
        }

        try {
            findNavController().navigate(
                R.id.action_global_to_detailUserFragment,
                bundle
            )
            Log.d("MainFragment", "Navigation successful")
        } catch (e: Exception) {
            Log.e("MainFragment", "Navigation failed", e)
        }
    }

    private fun switchChildFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}