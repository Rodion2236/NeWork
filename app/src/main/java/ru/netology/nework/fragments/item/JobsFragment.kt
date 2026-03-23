package ru.netology.nework.fragments.item

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.data.local.TokenStorage
import ru.netology.nework.databinding.FragmentJobsBinding
import ru.netology.nework.domain.model.Job
import ru.netology.nework.presentation.jobs.JobsUiState
import ru.netology.nework.presentation.jobs.JobsViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class JobsFragment : Fragment(R.layout.fragment_jobs) {

    private val viewModel: JobsViewModel by viewModels()

    @Inject
    lateinit var tokenStorage: TokenStorage

    private var _binding: FragmentJobsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentJobsBinding.bind(view)

        val userId = arguments?.getString("userId") ?: ""
        val currentUserId = tokenStorage.getUserId() ?: ""

        viewModel.setProfileIds(userId, currentUserId)

        setupClicks()
        setupObservers()
    }

    private fun setupClicks() {
        binding.buttonNewJob.setOnClickListener {
            findNavController().navigate(R.id.newJobFragment)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is JobsUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.containerJob.visibility = View.GONE
                            binding.emptyState.visibility = View.GONE
                            binding.buttonNewJob.visibility = View.GONE
                        }
                        is JobsUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.containerJob.visibility = View.VISIBLE
                            binding.emptyState.visibility = View.GONE
                            binding.buttonNewJob.visibility = if (viewModel.isOwnProfile) View.VISIBLE else View.GONE
                        }
                        is JobsUiState.Empty -> {
                            binding.progressBar.visibility = View.GONE
                            binding.containerJob.visibility = View.GONE
                            binding.emptyState.visibility = View.VISIBLE
                            binding.buttonNewJob.visibility = if (viewModel.isOwnProfile) View.VISIBLE else View.GONE
                        }
                        is JobsUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Snackbar.make(
                                binding.root,
                                getString(R.string.connection_error),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.jobs.collect { jobs ->
                    renderJobs(jobs)
                }
            }
        }
    }

    private fun renderJobs(jobs: List<Job>) {
        binding.containerJob.removeAllViews()

        if (jobs.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            return
        }

        binding.emptyState.visibility = View.GONE

        jobs.forEach { job ->
            val cardView = layoutInflater.inflate(
                R.layout.card_job,
                binding.containerJob,
                false
            )

            bindJob(cardView, job)
            binding.containerJob.addView(cardView)
        }
    }

    private fun bindJob(cardView: View, job: Job) {
        cardView.findViewById<TextView>(R.id.name).text = job.name
        cardView.findViewById<TextView>(R.id.position).text = job.position ?: ""
        cardView.findViewById<TextView>(R.id.position).visibility =
            if (job.position.isNullOrBlank()) View.GONE else View.VISIBLE

        val startDate = formatDate(job.start)
        val endDate = job.finish?.let { formatDate(it) } ?: getString(R.string.present)
        cardView.findViewById<TextView>(R.id.startFinish).text = "$startDate - $endDate"

        val linkView = cardView.findViewById<TextView>(R.id.link)
        if (!job.link.isNullOrBlank()) {
            linkView.text = job.link
            linkView.visibility = View.VISIBLE
            linkView.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, job.link.toUri())
                startActivity(intent)
            }
        } else {
            linkView.visibility = View.GONE
        }

        val deleteBtn = cardView.findViewById<MaterialButton>(R.id.buttonRemoveJob)
        if (viewModel.isOwnProfile) {
            deleteBtn.visibility = View.VISIBLE
            deleteBtn.setOnClickListener {
                Snackbar.make(cardView, "Удалить работу?", Snackbar.LENGTH_SHORT)
                    .setAction("Да") {
                        job.id.toIntOrNull()?.let { viewModel.deleteJob(it) }
                    }
                    .show()
            }
        } else {
            deleteBtn.visibility = View.GONE
        }
    }

    private fun formatDate(isoDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            val date = inputFormat.parse(isoDate)
            SimpleDateFormat("MM.yyyy", Locale.getDefault()).format(date ?: "")
        } catch (e: Exception) {
            isoDate.take(7).replace("-", ".")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}