package ru.netology.nework.fragments.newItem

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
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
import ru.netology.nework.databinding.FragmentNewEventBinding
import ru.netology.nework.presentation.newevent.NewEventUiState
import ru.netology.nework.presentation.newevent.NewEventViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class NewEventFragment : Fragment(R.layout.fragment_new_event) {

    private val viewModel: NewEventViewModel by viewModels()

    private var _binding: FragmentNewEventBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNewEventBinding.bind(view)

        setFragmentResultListener("maps_result") { _, bundle ->
            val lat = bundle.getDouble("lat")
            val long = bundle.getDouble("long")
            viewModel.onLocationSelected(lat, long)
        }

        setupClicks()
        setupObservers()
        setupToolbar()
    }

    private fun setupClicks() {
        binding.buttonSetDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_date))
                .build()
            datePicker.addOnPositiveButtonClickListener { timestamp: Long ->
                viewModel.onDateSelected(timestamp)
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        binding.addPhoto.setOnClickListener {
            Toast.makeText(requireContext(), "Добавление фото", Toast.LENGTH_SHORT).show()
        }

        binding.addFile.setOnClickListener {
            Toast.makeText(requireContext(), "Добавление файла", Toast.LENGTH_SHORT).show()
        }

        binding.addUser.setOnClickListener {
            Toast.makeText(requireContext(), "Выбор пользователей", Toast.LENGTH_SHORT).show()
        }

        binding.addLocation.setOnClickListener {
            findNavController().navigate(R.id.action_global_to_mapsFragment)
        }

        binding.removeLocation.setOnClickListener {
            viewModel.onLocationRemoved()
        }

        binding.removeImageAttachment.setOnClickListener {
            // TODO: Логика удаления фото
        }

        binding.topAppBar.menu.findItem(R.id.save)?.setOnMenuItemClickListener {
            val content = binding.textEvent.text?.toString()?.trim()
            if (content.isNullOrBlank()) {
                binding.textEvent.error = getString(R.string.empty_field)
                return@setOnMenuItemClickListener true
            }
            viewModel.createEvent(content)
            true
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is NewEventUiState.Loading -> {}
                        is NewEventUiState.Ready -> {}
                        is NewEventUiState.Success -> {
                            Toast.makeText(requireContext(), "Событие создано", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                        is NewEventUiState.Error -> {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.connection_error),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        is NewEventUiState.DateSelected -> {
                            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            val dateString = sdf.format(Date(state.timestamp))
                            binding.buttonSetDate.contentDescription = dateString
                            Toast.makeText(requireContext(), "Дата: $dateString", Toast.LENGTH_SHORT).show()
                        }
                        is NewEventUiState.LocationSelected -> {
                            binding.mapContainer.visibility = View.VISIBLE
                        }
                        is NewEventUiState.LocationRemoved -> {
                            binding.mapContainer.visibility = View.GONE
                        }

                        is NewEventUiState.TypeSelected -> {}
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