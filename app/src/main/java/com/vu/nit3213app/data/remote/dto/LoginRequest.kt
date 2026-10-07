package com.vu.nit3213app.data.remote.dto

/**
 * JSON body sent to the auth endpoint. Moshi turns it into:
 * { "username": "YourFirstName", "password": "sYourStudentID" }
 */
data class LoginRequest(
    val username: String,
    val password: String
)
