// Says which folder (package) this file belongs to.
package com.vu.nit3213app.ui.common

// Fragment: the base class for a screen inside the Activity.
import androidx.fragment.app.Fragment
// Lifecycle: describes the stages a screen goes through, such as STARTED.
import androidx.lifecycle.Lifecycle
// lifecycleScope: a place to start coroutines that stop when the screen ends.
import androidx.lifecycle.lifecycleScope
// repeatOnLifecycle: runs code only while the screen is at a certain stage.
import androidx.lifecycle.repeatOnLifecycle
// Flow: a stream of values that arrive over time, like the ViewModel's state.
import kotlinx.coroutines.flow.Flow
// launch: starts a coroutine.
import kotlinx.coroutines.launch

/**
 * Collects a Flow from a Fragment safely.
 *
 * It starts collecting when the screen becomes visible and stops when it goes
 * into the background, so the screen is never updated while it's hidden. Using
 * viewLifecycleOwner ties collection to the Fragment's views, which are
 * destroyed and rebuilt as the user moves between screens.
 *
 * Both the Login and Dashboard screens need this, so it is written once here.
 */
// Adds a function to every Fragment. It watches a flow and runs "action" with each new value.
fun <T> Fragment.collectWhenStarted(flow: Flow<T>, action: (T) -> Unit) {
    // Starts a coroutine that lives as long as the screen's views.
    // Why viewLifecycleOwner: the Fragment can outlive its views, and touching gone views crashes.
    viewLifecycleOwner.lifecycleScope.launch {
        // Runs the code inside only while the screen is visible (STARTED).
        // It stops when the screen is hidden and starts again when it comes back.
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            // Waits for each new value from the flow and passes it to "action" (such as render).
            flow.collect { value -> action(value) }
        }
    }
}
