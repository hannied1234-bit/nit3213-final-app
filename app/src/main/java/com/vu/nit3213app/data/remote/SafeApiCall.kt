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
 * Everything above the data layer sees a DataResult and never an exception.
 * That keeps Retrofit and OkHttp types out of the ViewModels, which only need
 * to know which kind of error happened so they can pick a message.
 *
 * The order of the catch blocks matters. Kotlin uses the first block that
 * matches, so subclasses have to come before their parent class. If IOException
 * were listed first, a timeout would be caught there and reported as "no
 * connection" instead of "timeout".
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
        // Coroutines use this exception to unwind. If it were caught and turned
        // into an Error, the coroutine would keep running and might update the
        // state of a screen that has already closed.
        throw exception
    } catch (exception: HttpException) {
        // The server replied, but with an error status such as 401 or 500.
        // The status code is handed to the repository's own rule, because the
        // same code can mean different things on different endpoints.
        DataResult.Error(mapHttpError(exception.code()))
    } catch (exception: SocketTimeoutException) {
        // Caught before IOException because it is a more specific subclass of it.
        // This is the error users are most likely to see on the first sign in,
        // while Render wakes the server up.
        DataResult.Error(DataError.TIMEOUT)
    } catch (exception: JsonEncodingException) {
        // Malformed JSON. Also a subclass of IOException, so it is caught first.
        // Without this block, broken JSON would be blamed on the user's
        // internet connection.
        DataResult.Error(DataError.UNKNOWN)
    } catch (exception: IOException) {
        // No internet, DNS failure, connection reset and so on.
        DataResult.Error(DataError.NO_CONNECTION)
    } catch (exception: Exception) {
        // Anything unexpected (such as JSON in the wrong shape) is reported to the
        // user as a general error rather than crashing the app.
        // Moshi throws JsonDataException when a field has the wrong type, for
        // example. That class isn't an IOException, so it ends up here.
        DataResult.Error(DataError.UNKNOWN)
    }
}

/**
 * The general meaning of HTTP error codes, used unless a repository overrides it.
 *
 * 404 gets its own error because the dashboard endpoint returns it for a
 * keypass it doesn't recognise. Every 5xx code means the problem is on the
 * server's side, so they all share one message.
 */
private fun defaultHttpError(code: Int): DataError = when (code) {
    404 -> DataError.NOT_FOUND
    in 500..599 -> DataError.SERVER
    else -> DataError.UNKNOWN
}
