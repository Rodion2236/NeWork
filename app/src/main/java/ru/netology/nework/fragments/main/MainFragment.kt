package ru.netology.nework.fragments.main

import android.os.Bundle
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
import ru.netology.nework.util.BundleKeys
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

        val navController = findNavController()
        val currentBackStackEntry = navController.getBackStackEntry(R.id.mainFragment)

        currentBackStackEntry.savedStateHandle.getLiveData<Int>("restoreTab")
            .observe(viewLifecycleOwner) { tabPosition ->
                tabPosition?.let { position ->
                    val itemId = when (position) {
                        0 -> R.id.postsFragment
                        1 -> R.id.eventsFragment
                        2 -> R.id.usersFragment
                        else -> R.id.postsFragment
                    }
                    binding.bottomNavigation.selectedItemId = itemId
                    currentBackStackEntry.savedStateHandle.remove<Int>("restoreTab")
                }
            }

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

    fun getCurrentTabIndex(): Int {
        return when (binding.bottomNavigation.selectedItemId) {
            R.id.postsFragment -> 0
            R.id.eventsFragment -> 1
            R.id.usersFragment -> 2
            else -> 0
        }
    }

    private fun navigateToMyProfile() {
        val currentUserId = tokenStorage.getUserId() ?: return

        val currentTabId = binding.bottomNavigation.selectedItemId
        val sourceTab = when (currentTabId) {
            R.id.postsFragment -> 0
            R.id.eventsFragment -> 1
            R.id.usersFragment -> 2
            else -> 0
        }

        val bundle = Bundle().apply {
            putString(BundleKeys.USER_ID, currentUserId)
            putInt("sourceTab", sourceTab)
        }
        findNavController().navigate(
            R.id.action_global_to_detailUserFragment,
            bundle
        )
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