// Says which folder (package) this file belongs to.
package com.vu.nit3213app.domain.model

/**
 * The outcome of a data request: it either succeeded with some data, or failed
 * with a [DataError].
 *
 * Returning this instead of throwing exceptions means the ViewModels must handle
 * both cases (the compiler checks every branch of a "when"), and they never need
 * to know about Retrofit or OkHttp exception types.
 */
// Creates the result type. "sealed" means the only options are the two listed inside.
// Why "out T": it lets one Error type stand in for a result of any kind.
sealed interface DataResult<out T> {

    // Option 1: it worked. "data" holds the answer, such as the keypass.
    data class Success<T>(val data: T) : DataResult<T>

    // Option 2: it failed. "error" says which kind of problem it was.
    data class Error(val error: DataError) : DataResult<Nothing>
}

/**
 * The kinds of failure the app knows how to explain to the user.
 * Each one is mapped to a friendly message in the UI layer.
 */
// Creates a fixed list of error kinds.
enum class DataError {
    // The student ID or first name was rejected by the server.
    INVALID_CREDENTIALS,

    // The server couldn't find what was asked for, such as an unknown keypass.
    NOT_FOUND,

    // The server took too long to reply.
    TIMEOUT,

    // The phone couldn't reach the server at all, usually because there's no internet.
    NO_CONNECTION,

    // The server had a problem of its own (a 5xx code).
    SERVER,

    // Anything else, such as a reply we couldn't read.
    UNKNOWN
}
