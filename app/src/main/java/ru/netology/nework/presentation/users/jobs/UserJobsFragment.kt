package ru.netology.nework.presentation.users.jobs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentUserJobsBinding
import ru.netology.nework.presentation.users.jobs.adapter.JobAdapter

@AndroidEntryPoint
class UserJobsFragment : Fragment(R.layout.fragment_user_jobs) {

    companion object {
        private const val ARG_USER_ID = "userId"

        fun newInstance(userId: String): UserJobsFragment {
            return UserJobsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
            }
        }
    }

    private val viewModel: UserJobsViewModel by viewModels()

    private var _binding: FragmentUserJobsBinding? = null
    private val binding get() = _binding!!

    private val adapter: JobAdapter by lazy {
        JobAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserJobsBinding.bind(view)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewJobs.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewJobs.adapter = adapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.jobs.collect { jobs ->
                    adapter.submitList(jobs)

                    binding.emptyState.visibility = if (jobs.isEmpty()) View.VISIBLE else View.GONE
                    binding.recyclerViewJobs.visibility = if (jobs.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}