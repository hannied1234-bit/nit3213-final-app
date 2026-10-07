package com.vu.nit3213app.data.remote.dto

/**
 * JSON returned by a successful login: { "keypass": "topicName" }
 */
data class LoginResponse(
    val keypass: String
)
