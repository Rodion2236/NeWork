package ru.netology.nework.fragments.auth

import android.os.Bundle
import android.view.View
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
import ru.netology.nework.databinding.FragmentLoginBinding
import ru.netology.nework.presentation.auth.LoginUiState.Error
import ru.netology.nework.presentation.auth.LoginUiState.Idle
import ru.netology.nework.presentation.auth.LoginUiState.Loading
import ru.netology.nework.presentation.auth.LoginUiState.Success
import ru.netology.nework.presentation.auth.LoginUiState.ValidationError
import ru.netology.nework.presentation.auth.LoginViewModel
import ru.netology.nework.util.AndroidUtils.hideKeyboard
import ru.netology.nework.util.ValidationError as FieldError
import ru.netology.nework.util.onTextChanged

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

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
                            state.loginError?.let { error ->
                                binding.loginLayout.error = when (error) {
                                    is FieldError.EmptyLogin -> getString(R.string.empty_login)
                                    else -> getString(R.string.empty_field)
                                }
                            }
                            state.passwordError?.let { error ->
                                binding.passwordLayout.error = when (error) {
                                    is FieldError.EmptyPassword -> getString(R.string.empty_password)
                                    else -> getString(R.string.empty_field)
                                }
                            }
                        }

                        is Idle -> {
                            setLoading(false)
                            binding.loginLayout.error = null
                            binding.passwordLayout.error = null
                        }
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonLogin.setOnClickListener {
            hideKeyboard(binding.buttonLogin)

            viewModel.onLoginClicked(
                login = binding.loginTextField.text.toString().trim(),
                password = binding.passwordTextField.text.toString()
            )
        }

        binding.buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.loginTextField.onTextChanged {
            if (binding.loginLayout.error != null) binding.loginLayout.error = null
        }
        binding.passwordTextField.onTextChanged {
            if (binding.passwordLayout.error != null) binding.passwordLayout.error = null
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !isLoading
        binding.loginTextField.isEnabled = !isLoading
        binding.passwordTextField.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.resetState()
    }
}