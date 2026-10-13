// Says which folder (package) this file belongs to.
package com.vu.nit3213app.ui.dashboard

// StringRes: marks a number as the ID of a text in strings.xml.
import androidx.annotation.StringRes
// Entity: one tidy item from the list.
import com.vu.nit3213app.domain.model.Entity

/**
 * The three things the Dashboard screen can be showing.
 *
 * A sealed interface means the screen is always in exactly one of these states,
 * and a "when" over it must cover all three, so no case can be forgotten. There's
 * never a list and an error on screen together, so they're separate options.
 */
// Creates the state type. "sealed" means the only options are the three listed inside.
sealed interface DashboardUiState {

    // Option 1: still waiting for the server. Shows the spinner.
    data object Loading : DashboardUiState

    // Option 2: the list arrived. It might be empty, and the screen handles that.
    data class Success(
        // The items to show.
        val entities: List<Entity>,
        // The total count to show above the list.
        val total: Int
    ) : DashboardUiState

    // Option 3: something went wrong. messageRes is the ID of the error text in strings.xml.
    data class Error(@StringRes val messageRes: Int) : DashboardUiState
}
