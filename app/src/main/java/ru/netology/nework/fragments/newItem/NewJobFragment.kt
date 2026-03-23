package ru.netology.nework.fragments.newItem

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewJobBinding
import ru.netology.nework.presentation.newjob.NewJobViewModel
import ru.netology.nework.presentation.newjobs.NewJobUiState

@AndroidEntryPoint
class NewJobFragment : Fragment(R.layout.fragment_new_job) {

    private val viewModel: NewJobViewModel by viewModels()

    private var _binding: FragmentNewJobBinding? = null
    private val binding get() = _binding!!

    private var startDate: Long? = null
    private var endDate: Long? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNewJobBinding.bind(view)

        setupClicks()
        setupObservers()
        setupToolbar()
    }

    private fun setupClicks() {
        binding.startWork.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_start_date))
                .build()
            datePicker.addOnPositiveButtonClickListener { timestamp ->
                startDate = timestamp
                binding.startWork.text = formatTimestamp(timestamp)
            }
            datePicker.show(parentFragmentManager, "START_DATE_PICKER")
        }

        binding.finishWork.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_dates))
                .build()
            datePicker.addOnPositiveButtonClickListener { timestamp ->
                endDate = timestamp
                binding.finishWork.text = formatTimestamp(timestamp)
            }
            datePicker.show(parentFragmentManager, "END_DATE_PICKER")
        }

        binding.buttonJobCreate.setOnClickListener {
            val name = binding.nameTextField.text?.toString()?.trim()
            val position = binding.positionTextField.text?.toString()?.trim()
            val link = binding.linkTextField.text?.toString()?.trim()

            if (name.isNullOrBlank()) {
                binding.nameLayout.error = getString(R.string.empty_field)
                return@setOnClickListener
            }

            if (startDate == null) {
                Toast.makeText(requireContext(), "Выберите дату начала", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.createJob(name, position, startDate!!, endDate, link)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is NewJobUiState.Loading -> {}
                        is NewJobUiState.Ready -> {}
                        is NewJobUiState.Success -> {
                            Toast.makeText(requireContext(), "Работа создана", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                        is NewJobUiState.Error -> {
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
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}