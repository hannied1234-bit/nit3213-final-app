// Says which folder (package) this file belongs to.
package com.vu.nit3213app.ui.login

// Bundle: a box of saved values Android passes to screens.
import android.os.Bundle
// View: the base class for everything you see on screen.
import android.view.View
// EditorInfo: holds the IDs of keyboard buttons, such as "Done".
import android.view.inputmethod.EditorInfo
// WindowCompat: gives access to the window, used here to hide the keyboard.
import androidx.core.view.WindowCompat
// WindowInsetsCompat: describes system areas such as the keyboard.
import androidx.core.view.WindowInsetsCompat
// isVisible: a shortcut for showing or hiding a view with true or false.
import androidx.core.view.isVisible
// doAfterTextChanged: runs code every time the text in a field changes.
import androidx.core.widget.doAfterTextChanged
// Fragment: the base class for a screen inside the Activity.
import androidx.fragment.app.Fragment
// viewModels: the "by viewModels()" helper that creates the ViewModel.
import androidx.fragment.app.viewModels
// findNavController: finds the object that moves between screens.
import androidx.navigation.fragment.findNavController
// R: the generated list of IDs for layouts, strings and images.
import com.vu.nit3213app.R
// FragmentLoginBinding: generated from fragment_login.xml, holds every view on this screen.
import com.vu.nit3213app.databinding.FragmentLoginBinding
// collectWhenStarted: our helper that watches the ViewModel's state.
import com.vu.nit3213app.ui.common.collectWhenStarted
// AndroidEntryPoint: the Hilt label that lets Hilt inject into this class.
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
// Tells Hilt it's allowed to deliver things (the ViewModel) to this screen.
@AndroidEntryPoint
// Creates the Login screen class. Passing the layout ID makes Fragment build the screen from fragment_login.xml.
class LoginFragment : Fragment(R.layout.fragment_login) {

    // Gets this screen's ViewModel. Hilt builds it and gives it an AuthRepository.
    // Why: "by viewModels()" keeps the same ViewModel when the phone rotates,
    // so a sign in that's halfway through isn't lost.
    private val viewModel: LoginViewModel by viewModels()

    // Holds the views of this screen. It starts as null because the views don't exist yet.
    // Why nullable: the views only exist between onViewCreated and onDestroyView.
    private var _binding: FragmentLoginBinding? = null
    // A shortcut that returns _binding without the null check (the !! part).
    // Why: saves writing _binding!! everywhere. It crashes if used after the views are gone.
    private val binding get() = _binding!!

    // Android calls this function once the screen's views have been built.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Lets the parent Fragment class do its own set-up first.
        super.onViewCreated(view, savedInstanceState)
        // Connects the binding to the views that were just built.
        _binding = FragmentLoginBinding.bind(view)

        // Hooks up the button, the keyboard and the text fields (see the function below).
        setUpListeners()
        // Starts watching the ViewModel's state. Every time it changes, render() redraws the screen.
        // Why after the listeners: so the buttons already work when the first state is drawn.
        collectWhenStarted(viewModel.uiState, ::render)
    }

    /** Connects the buttons, keyboard and text fields to the ViewModel. */
    private fun setUpListeners() {
        // When the Sign in button is tapped, run submitLogin().
        binding.signInButton.setOnClickListener { submitLogin() }

        // Listens for keyboard buttons pressed while typing in the first name (password) field.
        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            // Checks whether the button pressed was "Done".
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // It was Done, so sign in, the same as tapping the button.
                submitLogin()
                // Returns true to say "handled", so the keyboard doesn't also do its default action.
                true
            // Any other keyboard button.
            } else {
                // Returns false to say "not handled", so the keyboard does its normal thing.
                false
            }
        }

        // Every time the student ID text changes, tell the ViewModel.
        // Why: the ViewModel then clears the red error under that field while the user fixes it.
        binding.usernameEditText.doAfterTextChanged { viewModel.onUsernameChanged() }
        // Every time the first name text changes, tell the ViewModel, which clears that field's error.
        binding.passwordEditText.doAfterTextChanged { viewModel.onPasswordChanged() }
    }

    /**
     * Sends the typed values to the ViewModel. The ViewModel trims and
     * validates them, so the raw text is passed exactly as typed.
     */
    private fun submitLogin() {
        // Hides the on-screen keyboard.
        // Why: so it doesn't cover any error message that appears under the fields.
        hideKeyboard()
        // Sends what the user typed to the ViewModel, which checks it and signs in.
        viewModel.login(
            // Reads the student ID field as text. orEmpty() turns a missing value into "".
            username = binding.usernameEditText.text?.toString().orEmpty(),
            // Reads the first name field as text, the same way.
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
        // Shows the student ID error under that field, or removes it when the state has none (null).
        binding.usernameInputLayout.error = state.usernameError?.let { getString(it) }
        // Shows the first name error under that field, or removes it when there's none.
        binding.passwordInputLayout.error = state.passwordError?.let { getString(it) }

        // Shows the red error box only when the server sent back an error.
        binding.loginErrorText.isVisible = state.loginError != null
        // If there's a server error, puts its message text into the red box.
        state.loginError?.let { binding.loginErrorText.setText(it) }

        // Shows the spinning circle while signing in, and hides it otherwise.
        binding.signInProgress.isVisible = state.isLoading
        // Greys out the Sign in button while signing in. The ! means "not", so loading = not enabled.
        binding.signInButton.isEnabled = !state.isLoading
        // Locks the student ID field while signing in.
        // Why: editing mid-request would make the result look like it belonged to the new text.
        binding.usernameEditText.isEnabled = !state.isLoading
        // Locks the first name field while signing in, for the same reason.
        binding.passwordEditText.isEnabled = !state.isLoading

        // If login worked, the state holds a keypass, so go to the Dashboard with it.
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
        // Tells the ViewModel to clear the keypass from the state.
        // Why first: so a later redraw of this screen can't trigger the move a second time.
        viewModel.onNavigatedToDashboard()
        // Builds the "go to Dashboard" action with the keypass attached.
        // Why Safe Args: LoginFragmentDirections is generated from nav_graph.xml,
        // so the compiler checks the keypass is passed correctly.
        val action = LoginFragmentDirections.actionLoginFragmentToDashboardFragment(keypass)
        // Performs the move to the Dashboard.
        // Why Back then exits: nav_graph.xml removes Login from the history (popUpToInclusive).
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
        // Gets the window's controller for system areas (keyboard, status bar)...
        WindowCompat.getInsetsController(requireActivity().window, requireView())
            // ...and tells it to hide the keyboard (ime means input method, the keyboard).
            .hide(WindowInsetsCompat.Type.ime())
    }

    // Android calls this when the screen's views are destroyed, for example when leaving the screen.
    override fun onDestroyView() {
        // Lets the parent Fragment class do its own clean-up first.
        super.onDestroyView()
        // Forgets the views so they can be removed from memory.
        // Why: holding them after they're gone is a memory leak.
        _binding = null
    }
}
