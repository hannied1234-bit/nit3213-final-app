package com.vu.nit3213app.ui.common

import androidx.annotation.StringRes
import com.vu.nit3213app.R
import com.vu.nit3213app.domain.model.DataError

/**
 * Picks the user-facing message for each [DataError].
 *
 * The ViewModels expose string resource IDs rather than text, so they do not need
 * an Android Context. That keeps them easy to unit test, and the text itself
 * stays in strings.xml where it can be translated.
 *
 * It's written as an extension function, so callers write
 * result.error.toMessageRes(), which reads naturally. It lives in the UI layer
 * because R.string belongs to Android, and the domain layer has to stay free
 * of Android code.
 *
 * @StringRes on the return type lets Android Lint warn if the result is ever
 * passed somewhere expecting a different kind of resource, such as a colour.
 *
 * The "when" has no else branch. If a new DataError is added later, this
 * stops compiling until it gets a message, so no error can reach the user
 * without one.
 */
@StringRes
fun DataError.toMessageRes(): Int = when (this) {
    DataError.INVALID_CREDENTIALS -> R.string.error_invalid_credentials
    DataError.NOT_FOUND -> R.string.error_not_found
    DataError.TIMEOUT -> R.string.error_timeout
    DataError.NO_CONNECTION -> R.string.error_no_connection
    DataError.SERVER -> R.string.error_server
    DataError.UNKNOWN -> R.string.error_unknown
}
