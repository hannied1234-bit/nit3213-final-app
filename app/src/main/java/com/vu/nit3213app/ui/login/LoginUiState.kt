// Says which folder (package) this file belongs to.
package com.vu.nit3213app.ui.login

// StringRes: marks a number as the ID of a text in strings.xml.
import androidx.annotation.StringRes

/**
 * Everything the Login screen needs to draw itself at any moment.
 *
 * This is one data class with several fields, unlike the sealed
 * DashboardUiState. On this screen several things can be true at once, such as
 * both fields showing errors together.
 *
 * Every field has a default, so LoginUiState() is the blank form the screen
 * starts with. Changes are made with copy(), which keeps every other field as
 * it was.
 */
// Creates the state class for the Login screen.
data class LoginUiState(
    // True while signing in. Shows the spinner and locks the form. Starts as false.
    val isLoading: Boolean = false,
    // The ID of the error text under the student ID field. null means no error.
    @StringRes val usernameError: Int? = null,
    // The ID of the error text under the first name field. null means no error.
    @StringRes val passwordError: Int? = null,
    // The ID of the error from the server, shown in the red box. null means no error.
    @StringRes val loginError: Int? = null,
    // The keypass, set only after a successful login. The Fragment sees it and goes to the Dashboard.
    // Why it's cleared afterwards: so the move to the Dashboard only happens once.
    val keypass: String? = null
)
