// Says which folder (package) this file belongs to.
package com.vu.nit3213app.ui.login

// StringRes: marks a number as the ID of a text in strings.xml.
import androidx.annotation.StringRes
// ViewModel: the base class for holding screen state that survives rotation.
import androidx.lifecycle.ViewModel
// viewModelScope: a place to start coroutines that stop when the ViewModel is cleared.
import androidx.lifecycle.viewModelScope
// R: the generated list of IDs for layouts, strings and images.
import com.vu.nit3213app.R
// DataResult: our "worked or failed" result type.
import com.vu.nit3213app.domain.model.DataResult
// AuthRepository: the interface for logging in.
import com.vu.nit3213app.domain.repository.AuthRepository
// toMessageRes: turns a DataError into the ID of its message text.
import com.vu.nit3213app.ui.common.toMessageRes
// HiltViewModel: the Hilt label that lets Hilt build this ViewModel.
import dagger.hilt.android.lifecycle.HiltViewModel
// MutableStateFlow: a value holder that can be changed and watched.
import kotlinx.coroutines.flow.MutableStateFlow
// StateFlow: a read-only value holder that can be watched.
import kotlinx.coroutines.flow.StateFlow
// asStateFlow: gives a read-only view of a MutableStateFlow.
import kotlinx.coroutines.flow.asStateFlow
// update: changes a StateFlow's value safely in one step.
import kotlinx.coroutines.flow.update
// launch: starts a coroutine.
import kotlinx.coroutines.launch
// Inject: the label that tells Hilt it can build this class.
import javax.inject.Inject

/**
 * Holds the Login screen's state and logic: validating the form, calling the
 * auth endpoint and reporting the result.
 *
 * The ViewModel only knows the AuthRepository interface, never the Retrofit
 * details, which is what makes it simple to test with a mock. The form is
 * checked here before any request goes out, which gives the user instant
 * feedback and avoids a slow trip to a server that might still be waking up.
 */
// Lets Hilt build this ViewModel.
@HiltViewModel
// Creates the ViewModel class. "@Inject constructor" lets Hilt fill in the value below.
class LoginViewModel @Inject constructor(
    // Asks Hilt for the repository that does the login.
    private val authRepository: AuthRepository
// Builds on Android's ViewModel class.
) : ViewModel() {

    // The state the screen shows. Starts as a blank form. Private, so only this class can change it.
    private val _uiState = MutableStateFlow(LoginUiState())
    // A read-only view of the state, for the Fragment to watch. The Fragment can't change it.
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** Called when the user taps "Sign in" or presses Done on the keyboard. */
    fun login(username: String, password: String) {
        // If a sign in is already running, stop here.
        // Why: stops a fast double tap from sending two requests.
        if (_uiState.value.isLoading) return

        // Removes spaces from the start and end of the student ID.
        // Why: keyboards often add a space after a word, and the server would reject it.
        val trimmedUsername = username.trim()
        // Removes spaces from the first name. Capital letters are kept, because they matter.
        val trimmedPassword = password.trim()

        // Checks the student ID. Gives an error message ID, or null if it's fine.
        val usernameError = validateUsername(trimmedUsername)
        // Checks the first name the same way.
        // Why both before stopping: so the user sees every problem at once.
        val passwordError = validatePassword(trimmedPassword)
        // If either field has a problem...
        if (usernameError != null || passwordError != null) {
            // ...update the state to show the errors.
            _uiState.update {
                // copy() keeps everything else the same and changes only these fields.
                it.copy(
                    // The student ID error, or null.
                    usernameError = usernameError,
                    // The first name error, or null.
                    passwordError = passwordError,
                    // Clears any old server error, which would be confusing next to the new ones.
                    loginError = null
                )
            }
            // Stops here. Nothing is sent to the server.
            return
        }

        // Both fields are fine, so switch on loading and clear all errors.
        _uiState.update {
            // Spinner on, every error off.
            it.copy(isLoading = true, usernameError = null, passwordError = null, loginError = null)
        }

        // Starts a coroutine, so waiting for the server doesn't freeze the app.
        // Why viewModelScope: it's cancelled automatically if the user leaves the screen.
        viewModelScope.launch {
            // Sends the details to the repository, waits for the answer, then checks which kind it is.
            when (val result = authRepository.login(trimmedUsername, trimmedPassword)) {
                // It worked: update the state...
                is DataResult.Success -> _uiState.update {
                    // ...spinner off, and store the keypass. The Fragment sees it and goes to the Dashboard.
                    it.copy(isLoading = false, keypass = result.data)
                }

                // It failed: update the state...
                is DataResult.Error -> _uiState.update {
                    // ...spinner off, and store the message for the error kind (such as no internet).
                    it.copy(isLoading = false, loginError = result.error.toMessageRes())
                }
            }
        }
    }

    /**
     * Clears the student ID error as soon as the user starts fixing it.
     */
    // The Fragment calls this every time the student ID text changes.
    fun onUsernameChanged() {
        // Removes the student ID error and the server error.
        _uiState.update { it.copy(usernameError = null, loginError = null) }
    }

    /** Clears the first name error as soon as the user starts fixing it. */
    // The Fragment calls this every time the first name text changes.
    fun onPasswordChanged() {
        // Removes the first name error and the server error.
        _uiState.update { it.copy(passwordError = null, loginError = null) }
    }

    /**
     * Called by the Fragment once it has moved to the Dashboard.
     */
    // Clears the keypass after it's been used.
    fun onNavigatedToDashboard() {
        // Sets the keypass back to null.
        // Why: so the move to the Dashboard can't happen a second time.
        _uiState.update { it.copy(keypass = null) }
    }

    // Marks the returned number as a strings.xml ID.
    @StringRes
    // Checks the student ID. Returns an error message ID, or null if it's fine.
    private fun validateUsername(username: String): Int? = when {
        // Empty: "Please enter your student ID".
        // Why first: an empty box should get "required", not a confusing format error.
        username.isEmpty() -> R.string.error_username_required
        // Not digits only (such as "s12345678"): "Enter your student ID as numbers only...".
        !STUDENT_ID_PATTERN.matches(username) -> R.string.error_username_format
        // Otherwise it's fine.
        else -> null
    }

    // Marks the returned number as a strings.xml ID.
    @StringRes
    // Checks the first name. Only checks it isn't empty.
    // Why no pattern: names have hyphens, apostrophes and accents, so the server decides.
    private fun validatePassword(password: String): Int? =
        // Empty: "Please enter your first name". Otherwise null, meaning fine.
        if (password.isEmpty()) R.string.error_password_required else null

    // Holds fixed values for this class only.
    private companion object {
        // A pattern meaning "digits only", such as 12345678.
        // ^ means start, \\d+ means one or more digits, $ means end. So "s123" or "123abc" fail.
        val STUDENT_ID_PATTERN = Regex("^\\d+$")
    }
}
