// Says which folder (package) this file belongs to.
package com.vu.nit3213app.data.remote

// JsonEncodingException: thrown when the server's JSON is broken.
import com.squareup.moshi.JsonEncodingException
// DataError: our list of error kinds, such as NO_CONNECTION.
import com.vu.nit3213app.domain.model.DataError
// DataResult: our "worked or failed" result type.
import com.vu.nit3213app.domain.model.DataResult
// CancellationException: thrown when a coroutine is stopped on purpose.
import kotlinx.coroutines.CancellationException
// HttpException: thrown when the server replies with an error code like 401 or 500.
import retrofit2.HttpException
// IOException: thrown for network problems, such as no internet.
import java.io.IOException
// SocketTimeoutException: thrown when the server takes too long to reply.
import java.net.SocketTimeoutException

/**
 * Runs a network call and converts any exception into a [DataResult.Error].
 *
 * Both repositories use this, so the error handling is written once (the DRY
 * principle) instead of a try and catch block being copied into every function.
 *
 * The order of the catch blocks matters. Kotlin uses the first block that
 * matches, so the specific errors have to come before the general ones.
 */
// A function any repository can call. <T> means it works for any kind of result.
suspend fun <T> safeApiCall(
    // A rule for turning an HTTP error code into a DataError. Uses defaultHttpError unless told otherwise.
    mapHttpError: (code: Int) -> DataError = ::defaultHttpError,
    // The network request to run.
    call: suspend () -> T
// Returns either Success with the data, or Error with the kind of problem.
): DataResult<T> {
    // Tries the request. If anything goes wrong, the matching catch block below runs instead.
    return try {
        // Runs the request. If it works, wraps the answer in Success.
        DataResult.Success(call())
    // Catches a coroutine being stopped on purpose, for example the user leaving the screen.
    } catch (exception: CancellationException) {
        // Throws it again so the coroutine really stops.
        // Why: if it were caught as an error, the coroutine would keep running.
        throw exception
    // Catches the server replying with an error code, such as 401 or 500.
    } catch (exception: HttpException) {
        // Reads the code and uses the repository's rule to pick a DataError.
        DataResult.Error(mapHttpError(exception.code()))
    // Catches the server taking too long.
    // Why before IOException: a timeout is a type of IOException, so it must be checked first.
    } catch (exception: SocketTimeoutException) {
        // Reports it as a timeout.
        DataResult.Error(DataError.TIMEOUT)
    // Catches broken JSON from the server. Also a type of IOException, so it's checked first.
    } catch (exception: JsonEncodingException) {
        // Reports it as an unknown problem.
        // Why: without this block, broken JSON would be blamed on the user's internet.
        DataResult.Error(DataError.UNKNOWN)
    // Catches network problems: no internet, no signal, connection dropped.
    } catch (exception: IOException) {
        // Reports it as no connection.
        DataResult.Error(DataError.NO_CONNECTION)
    // Catches anything else, such as JSON in an unexpected shape.
    } catch (exception: Exception) {
        // Reports it as unknown instead of crashing the app.
        DataResult.Error(DataError.UNKNOWN)
    }
}

/**
 * The general meaning of HTTP error codes, used unless a repository overrides it.
 */
// Takes an HTTP error code and returns a DataError.
private fun defaultHttpError(code: Int): DataError = when (code) {
    // 404 means "not found", for example an unknown keypass.
    404 -> DataError.NOT_FOUND
    // Any code from 500 to 599 means the server itself had a problem.
    in 500..599 -> DataError.SERVER
    // Any other code is reported as unknown.
    else -> DataError.UNKNOWN
}
