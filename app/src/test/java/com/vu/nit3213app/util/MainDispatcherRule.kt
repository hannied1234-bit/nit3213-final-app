package com.vu.nit3213app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit rule that swaps the Main dispatcher for a test dispatcher.
 *
 * viewModelScope launches on Dispatchers.Main, which does not exist on the JVM
 * where unit tests run. Lecture 8 does this with Dispatchers.setMain in @Before
 * and resetMain in @After; wrapping both in a rule means each ViewModel test
 * class only needs one line instead of repeating that set-up.
 *
 * UnconfinedTestDispatcher runs coroutines straight away, so after calling a
 * ViewModel function the new state can be checked on the very next line.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
