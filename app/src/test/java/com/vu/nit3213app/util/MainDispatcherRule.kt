// Says which folder (package) this file belongs to.
package com.vu.nit3213app.util

// Dispatchers: decides which thread coroutines run on.
import kotlinx.coroutines.Dispatchers
// ExperimentalCoroutinesApi: marks test tools that are still officially "experimental".
import kotlinx.coroutines.ExperimentalCoroutinesApi
// TestDispatcher: a fake thread for tests.
import kotlinx.coroutines.test.TestDispatcher
// UnconfinedTestDispatcher: a test dispatcher that runs coroutines straight away.
import kotlinx.coroutines.test.UnconfinedTestDispatcher
// resetMain: puts the real Main dispatcher back.
import kotlinx.coroutines.test.resetMain
// setMain: swaps Main for a test dispatcher.
import kotlinx.coroutines.test.setMain
// TestWatcher: a JUnit rule that runs code before and after each test.
import org.junit.rules.TestWatcher
// Description: information about the test being run.
import org.junit.runner.Description

/**
 * JUnit rule that swaps the Main dispatcher for a test dispatcher.
 *
 * viewModelScope launches on Dispatchers.Main, which does not exist on the JVM
 * where unit tests run. Without this rule, the first viewModelScope.launch in a
 * test would crash. Wrapping the swap in a rule means each ViewModel test class
 * only needs one line instead of repeating the set-up.
 */
// Allows the experimental test tools to be used here.
@OptIn(ExperimentalCoroutinesApi::class)
// Creates the rule class.
class MainDispatcherRule(
    // The fake thread to use. UnconfinedTestDispatcher runs coroutines straight away,
    // so a test can check the new state on the very next line.
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
// Builds on JUnit's TestWatcher, which gives us "before" and "after" hooks.
) : TestWatcher() {

    // Runs before each test.
    override fun starting(description: Description) {
        // Swaps the Main dispatcher for our fake one.
        Dispatchers.setMain(testDispatcher)
    }

    // Runs after each test, whether it passed or failed.
    override fun finished(description: Description) {
        // Puts the real Main back, so one test can't affect the next.
        Dispatchers.resetMain()
    }
}
