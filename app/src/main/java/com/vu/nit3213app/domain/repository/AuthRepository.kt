// Says which folder (package) this file belongs to.
package com.vu.nit3213app.domain.repository

// DataResult: our "worked or failed" result type.
import com.vu.nit3213app.domain.model.DataResult

/**
 * Signs the user in.
 *
 * This is an interface (an abstraction) rather than a concrete class so the
 * LoginViewModel depends only on "something that can log in". Hilt supplies the
 * real network version in the app, and the unit tests supply a MockK fake. This is
 * the Dependency Inversion Principle from Lecture 4.
 */
// Creates the interface: a promise that something can log in, without saying how.
interface AuthRepository {

    /**
     * @param username the student ID without the leading "s", for example "12345678".
     * @param password the student's first name. It's case sensitive.
     * @return the keypass on success, which the dashboard endpoint needs.
     */
    // The login function. "suspend" means it waits for the server without freezing the app.
    // Returns Success with the keypass, or Error with the kind of problem.
    suspend fun login(username: String, password: String): DataResult<String>
}
