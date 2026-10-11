package com.vu.nit3213app.ui.common

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Collects a Flow from a Fragment safely.
 *
 * repeatOnLifecycle(STARTED) starts collecting when the screen becomes visible and
 * stops when it goes into the background, so the UI is never updated while it is
 * hidden and no work is wasted. Using viewLifecycleOwner ties collection to the
 * Fragment's view, which is destroyed and recreated as the user moves between
 * screens.
 *
 * Both the Login and Dashboard screens need this, so it is written once here.
 *
 * Why viewLifecycleOwner and not the Fragment itself? A Fragment on the back
 * stack keeps living after its view is destroyed. Collecting with the
 * Fragment's own lifecycle would keep the coroutine running and touching a
 * binding that's already null, which crashes. The view's lifecycle ends with
 * the view, so collection ends at the same moment.
 *
 * Each time collection restarts, a StateFlow sends its current value first. So
 * returning to the screen redraws it from the latest state straight away.
 *
 * @param flow the stream to watch, usually a ViewModel's uiState.
 * @param action called on the main thread with every value the flow emits.
 */
fun <T> Fragment.collectWhenStarted(flow: Flow<T>, action: (T) -> Unit) {
    // launch starts the outer coroutine, which lives until the view is destroyed.
    viewLifecycleOwner.lifecycleScope.launch {
        // repeatOnLifecycle suspends here. It runs the block each time the view
        // reaches STARTED and cancels it each time the view drops below STARTED.
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { value -> action(value) }
        }
    }
}
