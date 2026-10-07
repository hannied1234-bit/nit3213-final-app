package com.vu.nit3213app.ui.dashboard

import androidx.annotation.StringRes
import com.vu.nit3213app.domain.model.Entity

/**
 * The three things the Dashboard screen can be showing.
 *
 * A sealed interface means the screen is always in exactly one of these states,
 * and a "when" over it must cover all three, so no case can be forgotten.
 */
sealed interface DashboardUiState {

    data object Loading : DashboardUiState

    data class Success(
        val entities: List<Entity>,
        val total: Int
    ) : DashboardUiState

    data class Error(@StringRes val messageRes: Int) : DashboardUiState
}
