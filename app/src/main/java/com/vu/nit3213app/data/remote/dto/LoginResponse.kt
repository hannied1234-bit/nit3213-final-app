// Says which folder (package) this file belongs to.
package com.vu.nit3213app.data.remote.dto

/**
 * JSON returned by a successful login: { "keypass": "topicName" }
 */
// Creates a data class that Moshi fills in from the login reply.
data class LoginResponse(
    // The keypass (your topic name), which the dashboard request needs.
    // Why no default: a reply without a keypass is broken, so it should fail and show an error.
    val keypass: String
)
