// Says which folder (package) this file belongs to.
package com.vu.nit3213app.ui.common

// StringRes: marks a number as the ID of a text in strings.xml.
import androidx.annotation.StringRes
// R: the generated list of IDs for layouts, strings and images.
import com.vu.nit3213app.R
// DataError: our list of error kinds.
import com.vu.nit3213app.domain.model.DataError

/**
 * Picks the user-facing message for each [DataError].
 *
 * The ViewModels expose string resource IDs rather than text, so they do not need
 * an Android Context. That keeps them easy to unit test, and the text itself
 * stays in strings.xml where it can be translated.
 */
// Marks the returned number as a strings.xml ID, so Android Studio can warn about misuse.
@StringRes
// Adds a function to DataError, so you can write error.toMessageRes(). Returns the message's ID.
// Why no "else": if a new error kind is added, this won't build until it gets a message.
fun DataError.toMessageRes(): Int = when (this) {
    // Wrong details: "Sign in failed. Please check your student ID and first name..."
    DataError.INVALID_CREDENTIALS -> R.string.error_invalid_credentials
    // Not found: "We couldn't find any data for your topic."
    DataError.NOT_FOUND -> R.string.error_not_found
    // Too slow: "The server took too long to respond..."
    DataError.TIMEOUT -> R.string.error_timeout
    // No internet: "We couldn't reach the server..."
    DataError.NO_CONNECTION -> R.string.error_no_connection
    // Server problem: "The server ran into a problem..."
    DataError.SERVER -> R.string.error_server
    // Anything else: "Something went wrong. Please try again."
    DataError.UNKNOWN -> R.string.error_unknown
}
