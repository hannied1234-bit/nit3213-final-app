package com.vu.nit3213app.ui.dashboard

import androidx.annotation.StringRes
import com.vu.nit3213app.domain.model.Entity

/**
 * The three things the Dashboard screen can be showing.
 *
 * A sealed interface means the screen is always in exactly one of these states,
 * and a "when" over it must cover all three, so no case can be forgotten.
 *
 * Compare this with LoginUiState, which is a single data class full of flags.
 * The Dashboard's states really are separate. There's never a list and an error
 * on screen together. A sealed type makes those mixed states impossible to
 * build. Loading can't accidentally carry an old error message, because it has
 * no field to hold one.
 */
sealed interface DashboardUiState {

    // data object, not plain object, so toString() prints "Loading". That makes
    // test failures and log output easier to read.
    data object Loading : DashboardUiState

    /** The request worked. [entities] may be empty, and the screen handles that. */
    data class Success(
        val entities: List<Entity>,
        val total: Int
    ) : DashboardUiState

    /** The request failed. [messageRes] is the string to show the user. */
    data class Error(@StringRes val messageRes: Int) : DashboardUiState
}
