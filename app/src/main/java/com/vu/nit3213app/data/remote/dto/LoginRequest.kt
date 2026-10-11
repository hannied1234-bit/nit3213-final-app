package com.vu.nit3213app.data.remote.dto

/**
 * JSON body sent to the auth endpoint. Moshi turns it into:
 * { "username": "12345678", "password": "YourFirstName" }
 *
 * The username is the student ID with no leading "s", and the password is the
 * student's first name. The server compares the password with its capital
 * letters intact, so "Adam" and "adam" aren't the same.
 *
 * The property names have to match the JSON keys the API expects, because Moshi
 * uses them as the keys by default. The UI labels these fields "Student ID" and
 * "First name", but the API calls them username and password, so the API's
 * names are kept here. Renaming a property would need a @Json(name = ...)
 * annotation to keep the request working.
 */
data class LoginRequest(
    val username: String,
    val password: String
)
