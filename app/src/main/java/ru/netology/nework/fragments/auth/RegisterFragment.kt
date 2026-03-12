package ru.netology.nework.fragments.auth

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentRegisterBinding
import ru.netology.nework.presentation.auth.RegisterUiState.AvatarSelected
import ru.netology.nework.presentation.auth.RegisterUiState.Error
import ru.netology.nework.presentation.auth.RegisterUiState.Idle
import ru.netology.nework.presentation.auth.RegisterUiState.Loading
import ru.netology.nework.presentation.auth.RegisterUiState.Success
import ru.netology.nework.presentation.auth.RegisterUiState.ValidationError
import ru.netology.nework.presentation.auth.RegisterViewModel
import ru.netology.nework.util.AndroidUtils.hideKeyboard
import ru.netology.nework.util.onTextChanged

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val viewModel: RegisterViewModel by viewModels()
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private var selectedAvatarUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedAvatarUri = it
            viewModel.onAvatarSelected(it)
            binding.preview.setImageURI(it)
            binding.removePhoto.visibility = View.VISIBLE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is Loading -> setLoading(true)

                        is Success -> {
                            setLoading(false)
                            findNavController().popBackStack()
                        }

                        is Error -> {
                            setLoading(false)
                            Snackbar.make(
                                requireView(),
                                getString(state.messageRes),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }

                        is ValidationError -> {
                            setLoading(false)
                            state.loginError?.let {
                                binding.loginLayout.error = getString(
                                    when (it) {
                                        "empty_login" -> R.string.empty_login
                                        "incorrect_login" -> R.string.incorrect_login
                                        else -> R.string.empty_field
                                    }
                                )
                            }
                            state.nameError?.let {
                                binding.nameLayout.error = getString(
                                    when (it) {
                                        "name_is_empty" -> R.string.name_is_empty
                                        else -> R.string.empty_field
                                    }
                                )
                            }
                            state.passwordError?.let {
                                binding.passLayout.error = getString(
                                    when (it) {
                                        "empty_password" -> R.string.empty_password
                                        "incorrect_password" -> R.string.incorrect_password
                                        else -> R.string.empty_field
                                    }
                                )
                            }
                            state.repeatPasswordError?.let {
                                binding.repeatPassLayout.error = getString(
                                    when (it) {
                                        "passwords_don't_match" -> R.string.passwords_dont_match
                                        else -> R.string.empty_field
                                    }
                                )
                            }
                        }

                        is AvatarSelected -> {}

                        is Idle -> {
                            setLoading(false)
                            clearErrors()
                        }
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.pickPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.removePhoto.setOnClickListener {
            selectedAvatarUri = null
            binding.preview.setImageResource(R.drawable.ic_photo_camera_64)
            binding.removePhoto.visibility = View.GONE
        }

        binding.buttonLogin.setOnClickListener {
            hideKeyboard(binding.buttonLogin)

            viewModel.onRegisterClicked(
                login = binding.loginTextField.text.toString().trim(),
                name = binding.nameTextField.text.toString().trim(),
                password = binding.passwordTextField.text.toString(),
                repeatPassword = binding.repeatPasswordTextField.text.toString(),
                avatarUri = selectedAvatarUri
            )
        }

        binding.loginTextField.onTextChanged { if (binding.loginLayout.error != null) binding.loginLayout.error = null }
        binding.nameTextField.onTextChanged { if (binding.nameLayout.error != null) binding.nameLayout.error = null }
        binding.passwordTextField.onTextChanged { if (binding.passLayout.error != null) binding.passLayout.error = null }
        binding.repeatPasswordTextField.onTextChanged { if (binding.repeatPassLayout.error != null) binding.repeatPassLayout.error = null }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !isLoading
        binding.pickPhoto.isEnabled = !isLoading
    }

    private fun clearErrors() {
        binding.loginLayout.error = null
        binding.nameLayout.error = null
        binding.passLayout.error = null
        binding.repeatPassLayout.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.resetState()
    }
}