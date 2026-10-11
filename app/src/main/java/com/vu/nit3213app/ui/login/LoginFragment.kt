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
 * Data flows in one direction. User actions go up to the ViewModel as function
 * calls, and state comes back down through a StateFlow. The Fragment never
 * decides whether a student ID is valid or what an error means. It only shows
 * what the state says, which is why all of the login logic can be unit tested
 * without an emulator.
 *
 * @AndroidEntryPoint lets Hilt supply this Fragment's ViewModel.
 */
@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    // "by viewModels()" keeps the same ViewModel across screen rotations.
    // A request that's halfway through when the phone turns keeps running, and
    // the rebuilt screen picks up its result.
    private val viewModel: LoginViewModel by viewModels()

    // The binding only exists between onViewCreated and onDestroyView. Holding it
    // in a nullable field and clearing it in onDestroyView avoids a memory leak.
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        setUpListeners()
        // Called after the listeners are set, so the first render already has
        // working buttons. A StateFlow sends its current value right away, so
        // after a rotation any error or spinner reappears immediately.
        collectWhenStarted(viewModel.uiState, ::render)
    }

    /** Connects the buttons, keyboard and text fields to the ViewModel. */
    private fun setUpListeners() {
        binding.signInButton.setOnClickListener { submitLogin() }

        // Lets the user sign in straight from the keyboard's Done key.
        // The Done key comes from android:imeOptions="actionDone" on this field
        // in fragment_login.xml. Returning true says the key was handled, so the
        // keyboard doesn't also run its default action.
        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitLogin()
                true
            } else {
                false
            }
        }

        // Clears a field's error as soon as the user starts correcting it.
        // Leaving the red message up while they type would suggest the new
        // text is wrong too. The ViewModel decides what to clear, which keeps
        // that rule testable.
        binding.usernameEditText.doAfterTextChanged { viewModel.onUsernameChanged() }
        binding.passwordEditText.doAfterTextChanged { viewModel.onPasswordChanged() }
    }

    /**
     * Sends the typed values to the ViewModel. The ViewModel trims and
     * validates them, so the raw text is passed exactly as typed.
     */
    private fun submitLogin() {
        // Hiding the keyboard first means any error message below the fields
        // isn't covered by it.
        hideKeyboard()
        // text is nullable on an EditText, so orEmpty() turns a null into "".
        // The ViewModel then reports it as a missing field.
        viewModel.login(
            username = binding.usernameEditText.text?.toString().orEmpty(),
            password = binding.passwordEditText.text?.toString().orEmpty()
        )
    }

    /**
     * Draws one [LoginUiState] onto the screen.
     *
     * Every view is set on every call, so the screen always matches the state
     * exactly and nothing from an earlier state is left behind.
     */
    private fun render(state: LoginUiState) {
        // Setting a TextInputLayout's error to null removes the message and
        // the red outline, so a null in the state clears the error.
        binding.usernameInputLayout.error = state.usernameError?.let { getString(it) }
        binding.passwordInputLayout.error = state.passwordError?.let { getString(it) }

        // The banner under the fields is for errors from the server, such as
        // wrong credentials or no internet. Field problems stay on the fields.
        binding.loginErrorText.isVisible = state.loginError != null
        state.loginError?.let { binding.loginErrorText.setText(it) }

        // While a request is running, the inputs are disabled too. Editing the
        // fields mid-request would make the result look like it belonged to
        // the new text. The ViewModel ignores extra taps as well, so this is
        // mainly for the user's benefit.
        binding.signInProgress.isVisible = state.isLoading
        binding.signInButton.isEnabled = !state.isLoading
        binding.usernameEditText.isEnabled = !state.isLoading
        binding.passwordEditText.isEnabled = !state.isLoading

        // A keypass in the state means login worked and it's time to move on.
        state.keypass?.let { keypass -> navigateToDashboard(keypass) }
    }

    /**
     * Moves to the Dashboard with the keypass from a successful login.
     *
     * The keypass is a one-off event held in otherwise lasting state. If it
     * stayed in the state, coming back to this Fragment would see it again
     * and jump to the Dashboard a second time.
     */
    private fun navigateToDashboard(keypass: String) {
        // Clears the keypass first, so a later redraw of this screen can't
        // trigger the move again.
        viewModel.onNavigatedToDashboard()
        // Safe Args generated LoginFragmentDirections from nav_graph.xml, so the
        // keypass argument is checked by the compiler rather than at runtime.
        val action = LoginFragmentDirections.actionLoginFragmentToDashboardFragment(keypass)
        // The action pops Login off the back stack (popUpToInclusive in
        // nav_graph.xml), so Back on the Dashboard leaves the app.
        findNavController().navigate(action)
    }

    /**
     * Hides the on-screen keyboard using the window insets API.
     *
     * This replaces the older InputMethodManager approach, which needed a
     * system service and a window token. The insets controller takes the
     * window and any view inside it.
     */
    private fun hideKeyboard() {
        WindowCompat.getInsetsController(requireActivity().window, requireView())
            .hide(WindowInsetsCompat.Type.ime())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
