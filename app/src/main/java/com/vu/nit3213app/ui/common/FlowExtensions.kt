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
 * Fragment's view, which is destroyed and recreated during navigation.
 *
 * Both the Login and Dashboard screens need this, so it is written once here.
 */
fun <T> Fragment.collectWhenStarted(flow: Flow<T>, action: (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { value -> action(value) }
        }
    }
}
