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
 *
 * Without this rule, the first viewModelScope.launch in a test throws
 * "Module with the Main dispatcher is missing". On a phone, Main is backed by
 * Android's main Looper, and the JVM test runner has no Looper.
 *
 * The dispatcher is a constructor parameter with a default. A test that needs
 * finer control over when coroutines run could pass a StandardTestDispatcher
 * instead.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    // TestWatcher calls starting() before each test method runs.
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    // finished() runs after each test, pass or fail. Resetting Main here stops
    // one test's dispatcher from leaking into the next.
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
