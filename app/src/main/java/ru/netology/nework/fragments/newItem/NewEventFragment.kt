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
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewEventBinding
import ru.netology.nework.fragments.dialog.BottomSheetDialogFragment
import ru.netology.nework.presentation.newevent.NewEventUiState
import ru.netology.nework.presentation.newevent.NewEventViewModel
import ru.netology.nework.util.BundleKeys
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

        val isEditMode = arguments?.getBoolean("isEditMode") ?: false
        val eventId = arguments?.getString(BundleKeys.EVENT_ID)

        if (isEditMode && eventId != null) {
            viewModel.initEditMode(eventId)
            binding.topAppBar.title = getString(R.string.edit_event)

            arguments?.getString("originalContent")?.let { content ->
                binding.textEvent.setText(content)
                binding.textEvent.setSelection(content.length)
            }
        }

        setFragmentResultListener(BundleKeys.MAPS_RESULT) { _, bundle ->
            val lat = bundle.getDouble(BundleKeys.LAT)
            val long = bundle.getDouble(BundleKeys.LNG)
            viewModel.onLocationSelected(lat, long)
        }

        setFragmentResultListener(BottomSheetDialogFragment.RESULT_KEY) { _, bundle ->
            bundle.getString(BundleKeys.EVENT_TYPE)?.let { typeStr ->
                runCatching { ru.netology.nework.domain.model.EventType.valueOf(typeStr) }
                    .onSuccess { type ->
                        viewModel.onTypeSelected(type)
                    }
            }

            bundle.getLong(BundleKeys.EVENT_DATETIME, 0L).takeIf { it > 0 }?.let { timestamp ->
                viewModel.onDateSelected(timestamp)
                binding.buttonSetDate.contentDescription = formatTimestampForUi(timestamp)
                Toast.makeText(requireContext(), "Дата: ${formatTimestampForUi(timestamp)}", Toast.LENGTH_SHORT).show()
            }
        }

        setupClicks(isEditMode)
        setupObservers()
        setupToolbar()
    }

    private fun setupClicks(isEditMode: Boolean) {
        binding.buttonSetDate.setOnClickListener {
            BottomSheetDialogFragment().show(parentFragmentManager, BottomSheetDialogFragment.TAG)
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
            if (isEditMode) {
                viewModel.updateEvent(content)
            } else {
                viewModel.createEvent(content)
            }
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
                            Toast.makeText(
                                requireContext(),
                                if (arguments?.getBoolean("isEditMode") == true)
                                    getString(R.string.event_updated)
                                else
                                    getString(R.string.event_created),
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().navigateUp()
                        }
                        is NewEventUiState.Error -> {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.connection_error),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        is NewEventUiState.DateSelected -> {}
                        is NewEventUiState.LocationSelected -> {
                            binding.mapContainer.visibility = View.VISIBLE
                        }
                        is NewEventUiState.LocationRemoved -> {
                            binding.mapContainer.visibility = View.GONE
                        }
                        is NewEventUiState.TypeSelected -> {
                            Toast.makeText(requireContext(), "Тип: ${state.type}", Toast.LENGTH_SHORT).show()
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

    private fun formatTimestampForUi(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}