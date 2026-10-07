package com.vu.nit3213app.ui.login

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.vu.nit3213app.R
import com.vu.nit3213app.databinding.FragmentLoginBinding
import com.vu.nit3213app.ui.common.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint

/**
 * The Login screen. It reads what the user types, passes it to [LoginViewModel]
 * and draws whatever [LoginUiState] the ViewModel sends back.
 *
 * @AndroidEntryPoint lets Hilt supply this Fragment's ViewModel.
 */
@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    // "by viewModels()" keeps the same ViewModel across screen rotations.
    private val viewModel: LoginViewModel by viewModels()

    // The binding only exists between onViewCreated and onDestroyView. Holding it
    // in a nullable field and clearing it in onDestroyView avoids a memory leak.
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        setUpListeners()
        collectWhenStarted(viewModel.uiState, ::render)
    }

    private fun setUpListeners() {
        binding.signInButton.setOnClickListener { submitLogin() }

        // Lets the user sign in straight from the keyboard's Done key.
        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitLogin()
                true
            } else {
                false
            }
        }

        binding.usernameEditText.doAfterTextChanged { viewModel.onUsernameChanged() }
        binding.passwordEditText.doAfterTextChanged { viewModel.onPasswordChanged() }
    }

    private fun submitLogin() {
        hideKeyboard()
        viewModel.login(
            username = binding.usernameEditText.text?.toString().orEmpty(),
            password = binding.passwordEditText.text?.toString().orEmpty()
        )
    }

    private fun render(state: LoginUiState) {
        binding.usernameInputLayout.error = state.usernameError?.let { getString(it) }
        binding.passwordInputLayout.error = state.passwordError?.let { getString(it) }

        binding.loginErrorText.isVisible = state.loginError != null
        state.loginError?.let { binding.loginErrorText.setText(it) }

        binding.signInProgress.isVisible = state.isLoading
        binding.signInButton.isEnabled = !state.isLoading
        binding.usernameEditText.isEnabled = !state.isLoading
        binding.passwordEditText.isEnabled = !state.isLoading

        state.keypass?.let { keypass -> navigateToDashboard(keypass) }
    }

    private fun navigateToDashboard(keypass: String) {
        viewModel.onNavigatedToDashboard()
        // Safe Args generated LoginFragmentDirections from nav_graph.xml, so the
        // keypass argument is checked by the compiler rather than at runtime.
        val action = LoginFragmentDirections.actionLoginFragmentToDashboardFragment(keypass)
        findNavController().navigate(action)
    }

    private fun hideKeyboard() {
        WindowCompat.getInsetsController(requireActivity().window, requireView())
            .hide(WindowInsetsCompat.Type.ime())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
