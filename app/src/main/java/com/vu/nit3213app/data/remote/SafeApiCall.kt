package com.vu.nit3213app.data.remote

import com.squareup.moshi.JsonEncodingException
import com.vu.nit3213app.domain.model.DataError
import com.vu.nit3213app.domain.model.DataResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Runs a network call and converts any exception into a [DataResult.Error].
 *
 * Both repositories use this, so the error handling is written once (the DRY
 * principle) instead of a try and catch block being copied into every function.
 *
 * @param mapHttpError decides which [DataError] an HTTP error code means. Each
 * repository can pass its own rule; for example, a 401 from the login endpoint
 * means the credentials were wrong.
 * @param call the Retrofit request to run.
 */
suspend fun <T> safeApiCall(
    mapHttpError: (code: Int) -> DataError = ::defaultHttpError,
    call: suspend () -> T
): DataResult<T> {
    return try {
        DataResult.Success(call())
    } catch (exception: CancellationException) {
        // A cancelled coroutine (for example, the user left the screen) must be
        // allowed to stop, so this exception is passed on rather than swallowed.
        throw exception
    } catch (exception: HttpException) {
        // The server replied, but with an error status such as 401 or 500.
        DataResult.Error(mapHttpError(exception.code()))
    } catch (exception: SocketTimeoutException) {
        // Caught before IOException because it is a more specific subclass of it.
        DataResult.Error(DataError.TIMEOUT)
    } catch (exception: JsonEncodingException) {
        // Malformed JSON. Also a subclass of IOException, so it is caught first.
        DataResult.Error(DataError.UNKNOWN)
    } catch (exception: IOException) {
        // No internet, DNS failure, connection reset and so on.
        DataResult.Error(DataError.NO_CONNECTION)
    } catch (exception: Exception) {
        // Anything unexpected (such as JSON in the wrong shape) is reported to the
        // user as a general error rather than crashing the app.
        DataResult.Error(DataError.UNKNOWN)
    }
}

/** The general meaning of HTTP error codes, used unless a repository overrides it. */
private fun defaultHttpError(code: Int): DataError = when (code) {
    404 -> DataError.NOT_FOUND
    in 500..599 -> DataError.SERVER
    else -> DataError.UNKNOWN
}
