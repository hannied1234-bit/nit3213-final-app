package com.vu.nit3213app.domain.repository

import com.vu.nit3213app.domain.model.DataResult

/**
 * Signs the user in.
 *
 * This is an interface (an abstraction) rather than a concrete class so the
 * LoginViewModel depends only on "something that can log in". Hilt supplies the
 * real network version in the app, and the unit tests supply a MockK fake. This is
 * the Dependency Inversion Principle from Lecture 4.
 *
 * The interface sits in the domain layer and the implementation sits in the
 * data layer. The data layer depends on the domain layer, never the reverse, so
 * nothing in this package imports Retrofit, Moshi or OkHttp.
 */
interface AuthRepository {

    /**
     * Sends the user's credentials to the campus auth endpoint.
     *
     * It's a suspend function because it waits on the network. The caller
     * launches it in a coroutine and gets the result back without blocking the
     * main thread.
     *
     * @param username the student ID without the leading "s", for example "12345678".
     * @param password the student's first name. It's case sensitive.
     * @return the keypass on success, which the dashboard endpoint needs to
     * return the student's data.
     */
    suspend fun login(username: String, password: String): DataResult<String>
}
