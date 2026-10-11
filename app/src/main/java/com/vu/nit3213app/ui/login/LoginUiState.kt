package com.vu.nit3213app.ui.login

import androidx.annotation.StringRes

/**
 * Everything the Login screen needs to draw itself at any moment.
 *
 * The Fragment simply renders whatever state it is given, so all decisions live
 * in the ViewModel where they can be unit tested. Errors are string resource IDs
 * (or null when there is no error).
 *
 * This is one data class with several fields, unlike the sealed
 * DashboardUiState. On this screen several things really can be true at once.
 * Both fields can show errors together, for example. Separate fields describe
 * that directly.
 *
 * Every field has a default, so LoginUiState() is the blank form the screen
 * starts with. Changes are made with copy(), which keeps every other field as
 * it was.
 */
data class LoginUiState(
    /** True while the auth request is running. Shows the spinner and locks the form. */
    val isLoading: Boolean = false,
    /** Error under the first name field, such as "Enter your first name". */
    @StringRes val usernameError: Int? = null,
    /** Error under the student ID field, for a blank or badly formatted ID. */
    @StringRes val passwordError: Int? = null,
    /** Error from the request itself, shown in the banner under the form. */
    @StringRes val loginError: Int? = null,
    /**
     * Set once login succeeds. The Fragment moves to the Dashboard and then
     * calls [LoginViewModel.onNavigatedToDashboard], which clears this so the
     * move only happens once.
     */
    val keypass: String? = null
)
