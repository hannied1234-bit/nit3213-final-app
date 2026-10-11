package com.vu.nit3213app.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vu.nit3213app.R
import com.vu.nit3213app.domain.model.DataResult
import com.vu.nit3213app.domain.repository.DashboardRepository
import com.vu.nit3213app.ui.common.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Loads the entity list for the Dashboard screen.
 *
 * Hilt injects two things:
 * - [DashboardRepository], to fetch the data.
 * - [SavedStateHandle], which already contains the arguments passed to this
 *   screen. The "keypass" passed from the Login screen with Safe Args can be
 *   read from it, so the Fragment does not have to hand it over manually.
 *
 * A ViewModel outlives its Fragment's view. Rotating the phone destroys and
 * rebuilds the Fragment, but this object and its state stay put. That's why
 * the list doesn't reload on every rotation.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Nullable because a SavedStateHandle lookup can't promise the key exists.
    // In the running app Safe Args always provides it, but loadEntities()
    // still checks, so a missing keypass shows an error rather than a crash.
    // SavedStateHandle also survives the process being killed in the
    // background, so the keypass is still here when the user comes back.
    private val keypass: String? = savedStateHandle[KEYPASS_ARGUMENT]

    // The mutable version is private so only this class can change the state.
    // The Fragment gets a read-only StateFlow. It starts as Loading because
    // init kicks off a request straight away.
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Load straight away. Because the ViewModel survives rotation, this only
        // runs once per visit to the screen, not every time the device turns.
        loadEntities()
    }

    /** Fetches the list. Also called by the "Try again" button after an error. */
    fun loadEntities() {
        // Copied into a local val so Kotlin can smart-cast it to a non-null
        // String after the check below. Smart casts don't work directly on a
        // class property.
        val currentKeypass = keypass
        if (currentKeypass.isNullOrBlank()) {
            _uiState.value = DashboardUiState.Error(R.string.error_missing_keypass)
            return
        }

        // Set before the coroutine starts, so "Try again" swaps the error
        // message for the spinner on the same frame the user taps it.
        _uiState.value = DashboardUiState.Loading

        // viewModelScope is cancelled when the ViewModel is cleared (the user
        // leaves the Dashboard for good), so a slow request can't write to a
        // screen that no longer exists.
        viewModelScope.launch {
            // The "when" is an expression, so its result is assigned straight
            // to the state. DataResult is sealed, so leaving out a branch
            // wouldn't compile.
            _uiState.value = when (val result = dashboardRepository.getDashboard(currentKeypass)) {
                is DataResult.Success -> DashboardUiState.Success(
                    entities = result.data.entities,
                    total = result.data.total
                )

                is DataResult.Error -> DashboardUiState.Error(result.error.toMessageRes())
            }
        }
    }

    companion object {
        /**
         * Must match the argument name in nav_graph.xml. It's public so
         * DashboardViewModelTest can build a SavedStateHandle with the same key.
         */
        const val KEYPASS_ARGUMENT = "keypass"
    }
}
