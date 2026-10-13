// Says which folder (package) this file belongs to.
package com.vu.nit3213app.data.repository

// EntityMapper: turns raw JSON items into tidy Entity objects.
import com.vu.nit3213app.data.mapper.EntityMapper
// NitApiService: the Retrofit description of the server's endpoints.
import com.vu.nit3213app.data.remote.NitApiService
// safeApiCall: our helper that turns network errors into DataResult.Error.
import com.vu.nit3213app.data.remote.safeApiCall
// DashboardData: the list of entities plus the total.
import com.vu.nit3213app.domain.model.DashboardData
// DataResult: our "worked or failed" result type.
import com.vu.nit3213app.domain.model.DataResult
// DashboardRepository: the interface this class fills in.
import com.vu.nit3213app.domain.repository.DashboardRepository
// Inject: the label that tells Hilt it can build this class.
import javax.inject.Inject

/**
 * Network implementation of [DashboardRepository].
 *
 * It fetches the raw dashboard JSON and uses [EntityMapper] to turn each raw
 * entity into a display-ready Entity, so the ViewModel never sees raw maps.
 */
// Creates the class that really fetches the dashboard. "@Inject constructor" lets Hilt build it.
class DashboardRepositoryImpl @Inject constructor(
    // Asks Hilt for the API service, which sends the actual request.
    private val apiService: NitApiService,
    // Asks Hilt for the mapper, which tidies the raw items.
    private val entityMapper: EntityMapper
// ": DashboardRepository" means this class fills in the DashboardRepository interface.
) : DashboardRepository {

    // Fills in the interface's function. Returns the list and total, or an error.
    override suspend fun getDashboard(keypass: String): DataResult<DashboardData> =
        // Runs the request safely, using the default error rule (a 404 really means "unknown keypass").
        safeApiCall {
            // Sends the GET request with the keypass and waits for the reply.
            // Why no thread switching: Retrofit already does the network work in the background.
            val response = apiService.getDashboard(keypass)
            // Builds the result to return.
            DashboardData(
                // Runs every raw item through the mapper to get a list of tidy Entities.
                entities = response.entities.map(entityMapper::map),
                // Copies the total from the reply.
                total = response.entityTotal
            )
        }
}
