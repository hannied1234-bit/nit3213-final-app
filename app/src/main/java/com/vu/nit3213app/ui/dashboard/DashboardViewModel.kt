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
 * - [SavedStateHandle], which already contains the navigation arguments. The
 *   "keypass" passed from the Login screen with Safe Args can be read from it,
 *   so the Fragment does not have to hand it over manually.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val keypass: String? = savedStateHandle[KEYPASS_ARGUMENT]

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Load straight away. Because the ViewModel survives rotation, this only
        // runs once per visit to the screen, not every time the device turns.
        loadEntities()
    }

    /** Fetches the list. Also called by the "Try again" button after an error. */
    fun loadEntities() {
        val currentKeypass = keypass
        if (currentKeypass.isNullOrBlank()) {
            _uiState.value = DashboardUiState.Error(R.string.error_missing_keypass)
            return
        }

        _uiState.value = DashboardUiState.Loading

        viewModelScope.launch {
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
        /** Must match the argument name in nav_graph.xml. */
        const val KEYPASS_ARGUMENT = "keypass"
    }
}
