// Says which folder (package) this file belongs to.
package com.vu.nit3213app.data.remote.dto

/**
 * JSON body sent to the auth endpoint. Moshi turns it into:
 * { "username": "12345678", "password": "YourFirstName" }
 *
 * The username is the student ID with no leading "s", and the password is the
 * student's first name. The server compares the password with its capital
 * letters intact, so "Adam" and "adam" aren't the same.
 */
// Creates a data class that Moshi turns into the login JSON.
data class LoginRequest(
    // The student ID, such as "12345678".
    // Why "username": the property name becomes the JSON key, and the API expects "username".
    val username: String,
    // The first name, such as "Adam". Capital letters matter.
    val password: String
)
