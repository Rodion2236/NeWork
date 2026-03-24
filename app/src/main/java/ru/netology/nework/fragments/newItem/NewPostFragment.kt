package ru.netology.nework.fragments.newItem

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import ru.netology.nework.databinding.FragmentNewPostBinding
import ru.netology.nework.presentation.newpost.NewPostUiState
import ru.netology.nework.presentation.newpost.NewPostViewModel
import ru.netology.nework.util.load

@AndroidEntryPoint
class NewPostFragment : Fragment(R.layout.fragment_new_post) {

    private val viewModel: NewPostViewModel by viewModels()

    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    private val filePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onFileSelected(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNewPostBinding.bind(view)

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
        binding.addPhoto.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.addFile.setOnClickListener {
            filePicker.launch("*/*")
        }

        binding.addUsers.setOnClickListener {
            // TODO: Открыть диалог выбора пользователей
            Toast.makeText(requireContext(), "Выбор пользователей", Toast.LENGTH_SHORT).show()
        }

        binding.addLocation.setOnClickListener {
            findNavController().navigate(R.id.action_global_to_mapsFragment)
        }

        binding.removeImageAttachment.setOnClickListener {
            viewModel.onImageRemoved()
        }

        binding.removeLocation.setOnClickListener {
            viewModel.onLocationRemoved()
        }

        binding.topAppBar.menu.findItem(R.id.save)?.setOnMenuItemClickListener {
            val content = binding.textPost.text?.toString()?.trim()
            if (content.isNullOrBlank()) {
                binding.textPost.error = getString(R.string.empty_field)
                return@setOnMenuItemClickListener true
            }
            viewModel.createPost(content)
            true
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is NewPostUiState.Loading -> {}
                        is NewPostUiState.Ready -> {}
                        is NewPostUiState.Success -> {
                            Toast.makeText(requireContext(), "Пост создан", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                        is NewPostUiState.Error -> {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.connection_error),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        is NewPostUiState.ImageSelected -> {
                            binding.imageAttachmentContainer.visibility = View.VISIBLE
                            binding.imageAttachment.visibility = View.VISIBLE
                            binding.imageAttachment.load(
                                url = state.uri.toString(),
                                placeholder = R.drawable.ic_image_24,
                                error = R.drawable.ic_broken_image_24
                            )
                        }
                        is NewPostUiState.ImageRemoved -> {
                            binding.imageAttachmentContainer.visibility = View.GONE
                            binding.imageAttachment.visibility = View.GONE
                        }
                        is NewPostUiState.LocationSelected -> {
                            binding.mapContainer.visibility = View.VISIBLE
                        }
                        is NewPostUiState.LocationRemoved -> {
                            binding.mapContainer.visibility = View.GONE
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