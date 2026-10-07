package com.vu.nit3213app.domain.repository

import com.vu.nit3213app.domain.model.DataResult

/**
 * Signs the user in.
 *
 * This is an interface (an abstraction) rather than a concrete class so the
 * LoginViewModel depends only on "something that can log in". Hilt supplies the
 * real network version in the app, and the unit tests supply a MockK fake. This is
 * the Dependency Inversion Principle from Lecture 4.
 */
interface AuthRepository {

    /**
     * Sends the user's credentials to the campus auth endpoint.
     *
     * @param username the student's first name.
     * @param password the student ID, for example "s12345678".
     * @return the keypass on success, which unlocks the dashboard data.
     */
    suspend fun login(username: String, password: String): DataResult<String>
}
