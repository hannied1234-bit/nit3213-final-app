// Says which folder (package) this file belongs to.
package com.vu.nit3213app.ui.dashboard

// SavedStateHandle: holds the values passed to this screen, such as the keypass.
import androidx.lifecycle.SavedStateHandle
// ViewModel: the base class for holding screen state that survives rotation.
import androidx.lifecycle.ViewModel
// viewModelScope: a place to start coroutines that stop when the ViewModel is cleared.
import androidx.lifecycle.viewModelScope
// R: the generated list of IDs for layouts, strings and images.
import com.vu.nit3213app.R
// DataResult: our "worked or failed" result type.
import com.vu.nit3213app.domain.model.DataResult
// DashboardRepository: the interface for fetching the dashboard.
import com.vu.nit3213app.domain.repository.DashboardRepository
// toMessageRes: turns a DataError into the ID of its message text.
import com.vu.nit3213app.ui.common.toMessageRes
// HiltViewModel: the Hilt label that lets Hilt build this ViewModel.
import dagger.hilt.android.lifecycle.HiltViewModel
// MutableStateFlow: a value holder that can be changed and watched.
import kotlinx.coroutines.flow.MutableStateFlow
// StateFlow: a read-only value holder that can be watched.
import kotlinx.coroutines.flow.StateFlow
// asStateFlow: gives a read-only view of a MutableStateFlow.
import kotlinx.coroutines.flow.asStateFlow
// launch: starts a coroutine.
import kotlinx.coroutines.launch
// Inject: the label that tells Hilt it can build this class.
import javax.inject.Inject

/**
 * Loads the entity list for the Dashboard screen.
 *
 * Hilt injects two things: the [DashboardRepository], to fetch the data, and a
 * [SavedStateHandle], which already contains the keypass passed from the Login
 * screen with Safe Args.
 *
 * A ViewModel outlives its Fragment's views. Rotating the phone rebuilds the
 * Fragment, but this object and its state stay put, so the list doesn't reload.
 */
// Lets Hilt build this ViewModel.
@HiltViewModel
// Creates the ViewModel class. "@Inject constructor" lets Hilt fill in the two values below.
class DashboardViewModel @Inject constructor(
    // Asks Hilt for the repository that fetches the dashboard.
    private val dashboardRepository: DashboardRepository,
    // Asks Hilt for the screen's saved values, which include the keypass.
    savedStateHandle: SavedStateHandle
// Builds on Android's ViewModel class.
) : ViewModel() {

    // Reads the keypass from the saved values. "String?" means it could be missing (null).
    private val keypass: String? = savedStateHandle[KEYPASS_ARGUMENT]

    // The state the screen shows. Starts as Loading. Private, so only this class can change it.
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    // A read-only view of the state, for the Fragment to watch.
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Runs once, as soon as the ViewModel is created.
    init {
        // Starts loading the list straight away.
        // Why only once: the ViewModel survives rotation, so this doesn't rerun when the phone turns.
        loadEntities()
    }

    /** Fetches the list. Also called by the "Try again" button after an error. */
    fun loadEntities() {
        // Copies the keypass into a local value.
        // Why: Kotlin can then be sure it isn't null after the check below.
        val currentKeypass = keypass
        // Checks whether the keypass is missing or empty.
        if (currentKeypass.isNullOrBlank()) {
            // It is, so show the "session expired" error.
            _uiState.value = DashboardUiState.Error(R.string.error_missing_keypass)
            // Stops here without calling the server.
            return
        }

        // Shows the spinner. On "Try again" this swaps the error for the spinner straight away.
        _uiState.value = DashboardUiState.Loading

        // Starts a coroutine, so waiting for the server doesn't freeze the app.
        // Why viewModelScope: it's cancelled automatically if the user leaves the Dashboard.
        viewModelScope.launch {
            // Asks the repository for the list, waits for the answer, then sets the state from it.
            _uiState.value = when (val result = dashboardRepository.getDashboard(currentKeypass)) {
                // It worked: turn the result into a Success state.
                is DataResult.Success -> DashboardUiState.Success(
                    // The tidy items.
                    entities = result.data.entities,
                    // The total count.
                    total = result.data.total
                )

                // It failed: turn the error kind into its message ID and show an Error state.
                is DataResult.Error -> DashboardUiState.Error(result.error.toMessageRes())
            }
        }
    }

    // Holds values shared across the whole class.
    companion object {
        // The name the keypass is saved under. Must match the argument name in nav_graph.xml.
        // Why public: the unit test uses the same name to pass in a keypass.
        const val KEYPASS_ARGUMENT = "keypass"
    }
}
