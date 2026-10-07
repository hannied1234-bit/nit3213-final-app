package com.vu.nit3213app.ui.login

import androidx.annotation.StringRes

/**
 * Everything the Login screen needs to draw itself at any moment.
 *
 * The Fragment simply renders whatever state it is given, so all decisions live
 * in the ViewModel where they can be unit tested. Errors are string resource IDs
 * (or null when there is no error).
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    @StringRes val usernameError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val loginError: Int? = null,
    /**
     * Set once login succeeds. The Fragment navigates to the Dashboard and then
     * calls [LoginViewModel.onNavigatedToDashboard] so it only navigates once.
     */
    val keypass: String? = null
)
