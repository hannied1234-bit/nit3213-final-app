package com.vu.nit3213app.data.repository

import com.vu.nit3213app.data.mapper.EntityMapper
import com.vu.nit3213app.data.remote.NitApiService
import com.vu.nit3213app.data.remote.safeApiCall
import com.vu.nit3213app.domain.model.DashboardData
import com.vu.nit3213app.domain.model.DataResult
import com.vu.nit3213app.domain.repository.DashboardRepository
import javax.inject.Inject

/**
 * Network implementation of [DashboardRepository].
 *
 * It fetches the raw dashboard JSON and uses [EntityMapper] to turn each raw
 * entity into a display-ready Entity, so the ViewModel never sees raw maps.
 *
 * The mapper is injected instead of created here with EntityMapper(). Hilt
 * can build it because of its @Inject constructor. A test can then pass in the
 * real mapper, as DashboardRepositoryImplTest does, or a mock if it wanted to
 * check the repository on its own.
 */
class DashboardRepositoryImpl @Inject constructor(
    private val apiService: NitApiService,
    private val entityMapper: EntityMapper
) : DashboardRepository {

    // The default HTTP error rule is fine here, so no mapHttpError is passed.
    // A 404 from this endpoint really does mean "unknown keypass".
    override suspend fun getDashboard(keypass: String): DataResult<DashboardData> =
        safeApiCall {
            // Retrofit's suspend functions are main-safe: OkHttp does the network
            // work on its own background threads, so no withContext call is needed.
            val response = apiService.getDashboard(keypass)
            DashboardData(
                // The mapping runs inside safeApiCall too. If an odd value made
                // the mapper throw, the user would see an error message rather
                // than a crash.
                entities = response.entities.map(entityMapper::map),
                total = response.entityTotal
            )
        }
}
