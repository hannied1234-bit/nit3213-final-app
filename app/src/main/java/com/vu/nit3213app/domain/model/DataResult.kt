package com.vu.nit3213app.domain.model

/**
 * The outcome of a data request: it either succeeded with some data, or failed
 * with a [DataError].
 *
 * Returning this instead of throwing exceptions means the ViewModels must handle
 * both cases (the compiler checks every branch of a "when"), and they never need
 * to know about Retrofit or OkHttp exception types.
 *
 * "out T" makes the type covariant. That's what lets Error, which is a
 * DataResult<Nothing>, be returned where a DataResult<String> or a
 * DataResult<DashboardData> is expected. Nothing is a subtype of every type,
 * so a single Error class works for every kind of request.
 */
sealed interface DataResult<out T> {

    /** The request worked. [data] holds whatever it produced. */
    data class Success<T>(val data: T) : DataResult<T>

    /** The request failed. [error] says which kind of failure it was. */
    data class Error(val error: DataError) : DataResult<Nothing>
}

/**
 * The kinds of failure the app knows how to explain to the user.
 * Each one is mapped to a friendly message in the UI layer.
 *
 * The enum describes what went wrong, but holds no message text. Text belongs
 * to the UI, where it can come from strings.xml and be translated. Keeping it
 * out of here means the domain layer has no Android dependency at all.
 */
enum class DataError {
    /** The first name or student ID was rejected by the auth endpoint. */
    INVALID_CREDENTIALS,

    /** The server could not find the requested resource, for example an unknown keypass. */
    NOT_FOUND,

    /** The server took too long to reply. */
    TIMEOUT,

    /** The device could not reach the server at all, usually because it is offline. */
    NO_CONNECTION,

    /** The server replied with a 5xx error. */
    SERVER,

    /** Anything else, such as a response we could not read. */
    UNKNOWN
}
