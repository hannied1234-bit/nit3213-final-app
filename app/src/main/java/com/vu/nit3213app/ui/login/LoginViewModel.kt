package com.vu.nit3213app.ui.login

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vu.nit3213app.R
import com.vu.nit3213app.domain.model.DataResult
import com.vu.nit3213app.domain.repository.AuthRepository
import com.vu.nit3213app.ui.common.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Holds the Login screen's state and logic: validating the form, calling the
 * auth endpoint and reporting the result.
 *
 * @HiltViewModel plus the @Inject constructor lets Hilt create this ViewModel and
 * pass in an [AuthRepository]. The ViewModel only knows the interface, never the
 * Retrofit details, which is what makes it simple to test with a mock.
 *
 * The form is checked here before any request goes out. Catching an empty
 * field locally gives the user instant feedback, and it avoids a slow round
 * trip to a server that might still be waking up.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // The mutable version is private so only this class can change the state.
    // The Fragment gets a read-only StateFlow (the pattern from Lecture 5).
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** Called when the user taps "Sign in" or presses Done on the keyboard. */
    fun login(username: String, password: String) {
        // Ignore extra taps while a request is already running.
        // The button is disabled while loading, but a fast double tap can land
        // before the screen redraws. This check stops a second request either way.
        if (_uiState.value.isLoading) return

        // Stray spaces are a common typing mistake on mobile keyboards.
        // Autocomplete often adds a space after a word, and the API would
        // reject "Adam " even though the user typed their name correctly.
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()

        // Both fields are checked before returning, so the user sees every
        // problem at once instead of fixing them one at a time.
        val usernameError = validateUsername(trimmedUsername)
        val passwordError = validatePassword(trimmedPassword)
        if (usernameError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    usernameError = usernameError,
                    passwordError = passwordError,
                    // An old server error would be misleading next to new
                    // field errors, so it's cleared.
                    loginError = null
                )
            }
            return
        }

        // update {} changes the state atomically. It reads the current value,
        // applies the copy and writes it back in one step, so two updates
        // can't overwrite each other.
        _uiState.update {
            it.copy(isLoading = true, usernameError = null, passwordError = null, loginError = null)
        }

        // viewModelScope is cancelled automatically when the ViewModel is cleared,
        // so the request cannot outlive the screen and leak memory.
        viewModelScope.launch {
            when (val result = authRepository.login(trimmedUsername, trimmedPassword)) {
                // The keypass goes into the state. The Fragment sees it and
                // moves to the Dashboard.
                is DataResult.Success -> _uiState.update {
                    it.copy(isLoading = false, keypass = result.data)
                }

                // The repository has already sorted the failure into a
                // DataError. Here it only needs a message.
                is DataResult.Error -> _uiState.update {
                    it.copy(isLoading = false, loginError = result.error.toMessageRes())
                }
            }
        }
    }

    /**
     * Clears the first name error as soon as the user starts fixing it.
     * The server error is cleared too, because the user is changing the very
     * thing it complained about.
     */
    fun onUsernameChanged() {
        _uiState.update { it.copy(usernameError = null, loginError = null) }
    }

    /** Clears the student ID error as soon as the user starts fixing it. */
    fun onPasswordChanged() {
        _uiState.update { it.copy(passwordError = null, loginError = null) }
    }

    /**
     * Called by the Fragment once it has moved to the Dashboard. Clearing the
     * keypass means the move happens only once, even if the state is drawn again.
     */
    fun onNavigatedToDashboard() {
        _uiState.update { it.copy(keypass = null) }
    }

    // Each validator returns a message ID for a problem, or null when the input
    // is fine. Returning the ID means the caller can drop it straight into the
    // state without any more checks.
    @StringRes
    private fun validateUsername(username: String): Int? =
        if (username.isEmpty()) R.string.error_username_required else null

    // Checked in order. A blank field gets "required" instead of a format
    // error, which would be confusing for an empty box.
    @StringRes
    private fun validatePassword(password: String): Int? = when {
        password.isEmpty() -> R.string.error_password_required
        !STUDENT_ID_PATTERN.matches(password) -> R.string.error_password_format
        else -> null
    }

    private companion object {
        // A lowercase "s" followed by digits, for example s12345678.
        // ^ and $ anchor the pattern to the whole string, so "s123abc" or
        // "xs123" fail. matches() needs a full match anyway, but the anchors
        // make the intent obvious to a reader.
        val STUDENT_ID_PATTERN = Regex("^s\\d+$")
    }
}
