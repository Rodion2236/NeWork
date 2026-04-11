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
import ru.netology.nework.data.local.TokenStorage
import ru.netology.nework.databinding.FragmentNewPostBinding
import ru.netology.nework.fragments.dialog.UserSelectionDialogFragment
import ru.netology.nework.presentation.newpost.NewPostUiState
import ru.netology.nework.presentation.newpost.NewPostViewModel
import ru.netology.nework.util.BundleKeys
import ru.netology.nework.util.load
import javax.inject.Inject

@AndroidEntryPoint
class NewPostFragment : Fragment(R.layout.fragment_new_post) {

    private val viewModel: NewPostViewModel by viewModels()

    @Inject
    lateinit var tokenStorage: TokenStorage

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
        uri?.let {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "file"
            viewModel.onFileSelected(it, fileName)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNewPostBinding.bind(view)

        val isEditMode = arguments?.getBoolean("isEditMode") ?: false
        val postId = arguments?.getString(BundleKeys.POST_ID)
        val userId = arguments?.getString(BundleKeys.USER_ID)

        if (isEditMode && postId != null) {
            viewModel.initEditMode(
                postId = postId,
                attachmentUrl = arguments?.getString(BundleKeys.ATTACHMENT_URL),
                attachmentType = arguments?.getString(BundleKeys.ATTACHMENT_TYPE)
            )
            binding.topAppBar.title = getString(R.string.edit_post)

            arguments?.getString("originalContent")?.let { content ->
                binding.textPost.setText(content)
                binding.textPost.setSelection(content.length)
            }

            val attachmentUrl = arguments?.getString(BundleKeys.ATTACHMENT_URL)
            val attachmentType = arguments?.getString(BundleKeys.ATTACHMENT_TYPE)

            if (!attachmentUrl.isNullOrBlank() && !attachmentType.isNullOrBlank()) {
                binding.imageAttachmentContainer.visibility = View.VISIBLE

                when (attachmentType) {
                    "IMAGE" -> {
                        binding.imageAttachment.visibility = View.VISIBLE
                        binding.fileAttachmentPreview.visibility = View.GONE

                        binding.imageAttachment.load(
                            url = attachmentUrl,
                            placeholder = R.drawable.ic_image_24,
                            error = R.drawable.ic_broken_image_24
                        )
                    }
                    "VIDEO", "AUDIO" -> {
                        binding.imageAttachment.visibility = View.GONE
                        binding.fileAttachmentPreview.visibility = View.VISIBLE
                        binding.fileAttachmentPreview.text = attachmentUrl.substringAfterLast('/')
                    }
                }
            }

            val lat = arguments?.getDouble(BundleKeys.LAT)
            val long = arguments?.getDouble(BundleKeys.LNG)

            if (lat != null && long != null && (lat != 0.0 || long != 0.0)) {
                viewModel.onLocationSelected(lat, long)
                binding.mapContainer.visibility = View.VISIBLE
            }

            val mentionIds = arguments?.getStringArrayList(BundleKeys.MENTION_IDS) ?: emptyList()
            if (mentionIds.isNotEmpty()) {
                viewModel.onMentionsSelected(mentionIds)
            }
        }

        setFragmentResultListener(BundleKeys.MENTIONS_RESULT) { _, bundle ->
            val selectedIds = bundle.getStringArrayList(BundleKeys.SELECTED_USER_IDS) ?: emptyList()
            viewModel.onMentionsSelected(selectedIds)
        }

        setFragmentResultListener(BundleKeys.MAPS_RESULT) { _, bundle ->
            val lat = bundle.getDouble(BundleKeys.LAT)
            val long = bundle.getDouble(BundleKeys.LNG)
            viewModel.onLocationSelected(lat, long)
        }

        setupClicks(isEditMode, userId)
        setupObservers()
        setupToolbar()
    }

    private fun setupClicks(isEditMode: Boolean, userId: String?) {
        binding.addPhoto.setOnClickListener { imagePicker.launch("image/*") }
        binding.addFile.setOnClickListener { filePicker.launch("*/*") }
        binding.addUsers.setOnClickListener {
            UserSelectionDialogFragment().show(parentFragmentManager, UserSelectionDialogFragment.TAG)
        }
        binding.addLocation.setOnClickListener {
            findNavController().navigate(R.id.action_global_to_mapsFragment)
        }
        binding.removeImageAttachment.setOnClickListener { viewModel.onImageRemoved() }
        binding.removeLocation.setOnClickListener { viewModel.onLocationRemoved() }

        binding.topAppBar.menu.findItem(R.id.save)?.setOnMenuItemClickListener {
            val content = binding.textPost.text?.toString()?.trim()
            if (content.isNullOrBlank()) {
                binding.textPost.error = getString(R.string.empty_field)
                return@setOnMenuItemClickListener true
            }

            val finalUserId = userId ?: tokenStorage.getUserId()

            if (isEditMode) {
                viewModel.updatePost(content, finalUserId)
            } else {
                viewModel.createPost(content, finalUserId)
            }
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
                            Toast.makeText(
                                requireContext(),
                                if (arguments?.getBoolean("isEditMode") == true)
                                    getString(R.string.post_updated)
                                else
                                    getString(R.string.post_created),
                                Toast.LENGTH_SHORT
                            ).show()
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
                            android.util.Log.d("NewPost", "ImageSelected: showing imageAttachment")
                            binding.imageAttachmentContainer.visibility = View.VISIBLE
                            binding.imageAttachment.visibility = View.VISIBLE
                            binding.fileAttachmentPreview.visibility = View.GONE
                            binding.imageAttachment.load(
                                url = state.uri.toString(),
                                placeholder = R.drawable.ic_image_24,
                                error = R.drawable.ic_broken_image_24
                            )
                        }
                        is NewPostUiState.ImageRemoved -> {
                            binding.imageAttachmentContainer.visibility = View.GONE
                            binding.imageAttachment.visibility = View.GONE
                            binding.fileAttachmentPreview.visibility = View.GONE
                        }
                        is NewPostUiState.FileSelected -> {
                            binding.imageAttachmentContainer.visibility = View.VISIBLE
                            binding.imageAttachment.visibility = View.GONE
                            binding.fileAttachmentPreview.visibility = View.VISIBLE
                            binding.fileAttachmentPreview.text = state.fileName
                        }
                        is NewPostUiState.FileRemoved -> {
                            binding.imageAttachmentContainer.visibility = View.GONE
                            binding.imageAttachment.visibility = View.GONE
                            binding.fileAttachmentPreview.visibility = View.GONE
                        }
                        is NewPostUiState.LocationSelected -> {
                            android.util.Log.d("NewPost", "LocationSelected: showing mapContainer")
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