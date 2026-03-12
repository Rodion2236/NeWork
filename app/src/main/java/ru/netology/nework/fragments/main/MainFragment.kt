package ru.netology.nework.fragments.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.data.local.TokenStorage
import ru.netology.nework.databinding.FragmentMainBinding
import ru.netology.nework.fragments.item.PostsFragment
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

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.postsFragment -> {
                    switchChildFragment(PostsFragment())
                    true
                }
                R.id.eventsFragment -> {
                    // TODO: Переключить на события
                    // switchChildFragment(EventsFragment())
                    true
                }
                R.id.usersFragment -> {
                    // TODO: Переключить на пользователей
                    // switchChildFragment(UsersFragment())
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