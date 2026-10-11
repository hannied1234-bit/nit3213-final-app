package com.vu.nit3213app.data.remote.dto

/**
 * JSON body sent to the auth endpoint. Moshi turns it into:
 * { "username": "YourFirstName", "password": "sYourStudentID" }
 *
 * The property names have to match the JSON keys the API expects, because Moshi
 * uses them as the keys by default. The UI calls these fields "first name" and
 * "student ID", but the API calls them username and password, so the API's
 * names are kept here. Renaming a property would need a @Json(name = ...)
 * annotation to keep the request working.
 */
data class LoginRequest(
    val username: String,
    val password: String
)
