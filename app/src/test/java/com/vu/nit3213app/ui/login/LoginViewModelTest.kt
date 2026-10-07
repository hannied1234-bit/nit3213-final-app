package com.vu.nit3213app.ui.login

import com.vu.nit3213app.R
import com.vu.nit3213app.domain.model.DataError
import com.vu.nit3213app.domain.model.DataResult
import com.vu.nit3213app.domain.repository.AuthRepository
import com.vu.nit3213app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [LoginViewModel].
 *
 * The real AuthRepository would call the internet, so a MockK mock stands in for
 * it. Each test tells the mock what to return (coEvery), calls the ViewModel,
 * then checks the resulting state (Given, When, Then).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = LoginViewModel(authRepository)
    }

    @Test
    fun `initial state has no errors and is not loading`() {
        assertEquals(LoginUiState(), viewModel.uiState.value)
    }

    @Test
    fun `blank first name shows an error and does not call the API`() {
        // When
        viewModel.login(username = "   ", password = "s12345678")

        // Then
        val state = viewModel.uiState.value
        assertEquals(R.string.error_username_required, state.usernameError)
        assertNull(state.passwordError)
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun `blank student ID shows an error and does not call the API`() {
        viewModel.login(username = "Adam", password = "")

        val state = viewModel.uiState.value
        assertEquals(R.string.error_password_required, state.passwordError)
        assertNull(state.usernameError)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun `student ID without the leading s shows a format error`() {
        viewModel.login(username = "Adam", password = "12345678")

        assertEquals(R.string.error_password_format, viewModel.uiState.value.passwordError)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun `both fields blank shows both errors`() {
        viewModel.login(username = "", password = "")

        val state = viewModel.uiState.value
        assertEquals(R.string.error_username_required, state.usernameError)
        assertEquals(R.string.error_password_required, state.passwordError)
    }

    @Test
    fun `successful login trims the input and exposes the keypass`() {
        // Given
        coEvery { authRepository.login("Adam", "s12345678") } returns DataResult.Success("animals")

        // When
        viewModel.login(username = "  Adam ", password = " s12345678  ")

        // Then
        val state = viewModel.uiState.value
        assertEquals("animals", state.keypass)
        assertFalse(state.isLoading)
        assertNull(state.loginError)
        coVerify(exactly = 1) { authRepository.login("Adam", "s12345678") }
    }

    @Test
    fun `rejected credentials show the invalid credentials message`() {
        coEvery { authRepository.login(any(), any()) } returns
            DataResult.Error(DataError.INVALID_CREDENTIALS)

        viewModel.login(username = "Adam", password = "s12345678")

        val state = viewModel.uiState.value
        assertEquals(R.string.error_invalid_credentials, state.loginError)
        assertNull(state.keypass)
        assertFalse(state.isLoading)
    }

    @Test
    fun `network failure shows the no connection message`() {
        coEvery { authRepository.login(any(), any()) } returns
            DataResult.Error(DataError.NO_CONNECTION)

        viewModel.login(username = "Adam", password = "s12345678")

        assertEquals(R.string.error_no_connection, viewModel.uiState.value.loginError)
    }

    @Test
    fun `state shows loading while the request is running`() = runTest {
        // Given a request that does not finish until we say so
        val pendingResult = CompletableDeferred<DataResult<String>>()
        coEvery { authRepository.login(any(), any()) } coAnswers { pendingResult.await() }

        // When
        viewModel.login(username = "Adam", password = "s12345678")

        // Then it is loading
        assertTrue(viewModel.uiState.value.isLoading)

        // When the request finishes
        pendingResult.complete(DataResult.Success("animals"))
        advanceUntilIdle()

        // Then loading stops
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("animals", viewModel.uiState.value.keypass)
    }

    @Test
    fun `second tap while loading does not send a second request`() = runTest {
        val pendingResult = CompletableDeferred<DataResult<String>>()
        coEvery { authRepository.login(any(), any()) } coAnswers { pendingResult.await() }

        viewModel.login(username = "Adam", password = "s12345678")
        viewModel.login(username = "Adam", password = "s12345678")
        pendingResult.complete(DataResult.Success("animals"))
        advanceUntilIdle()

        coVerify(exactly = 1) { authRepository.login(any(), any()) }
    }

    @Test
    fun `onNavigatedToDashboard clears the keypass so navigation happens once`() {
        coEvery { authRepository.login(any(), any()) } returns DataResult.Success("animals")
        viewModel.login(username = "Adam", password = "s12345678")

        viewModel.onNavigatedToDashboard()

        assertNull(viewModel.uiState.value.keypass)
    }

    @Test
    fun `typing in a field clears that field's error`() {
        viewModel.login(username = "", password = "")

        viewModel.onUsernameChanged()
        assertNull(viewModel.uiState.value.usernameError)
        assertEquals(R.string.error_password_required, viewModel.uiState.value.passwordError)

        viewModel.onPasswordChanged()
        assertNull(viewModel.uiState.value.passwordError)
    }
}
