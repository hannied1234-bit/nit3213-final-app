package com.vu.nit3213app.data.remote.dto

/**
 * JSON returned by a successful login: { "keypass": "topicName" }
 *
 * The keypass names the student's topic, and it's the only thing the dashboard
 * endpoint needs. It has no default value on purpose. A 200 reply without a
 * keypass is broken, so Moshi throws and safeApiCall reports a general error
 * rather than letting the app carry on with an empty keypass.
 */
data class LoginResponse(
    val keypass: String
)
