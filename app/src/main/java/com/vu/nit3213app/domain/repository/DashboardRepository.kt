package com.vu.nit3213app.domain.repository

import com.vu.nit3213app.domain.model.DashboardData
import com.vu.nit3213app.domain.model.DataResult

/**
 * Loads the list of entities for the signed-in user's topic.
 * See [AuthRepository] for why this is an interface.
 *
 * It returns domain types only. The DashboardViewModel gets finished
 * [com.vu.nit3213app.domain.model.Entity] objects and doesn't know they started
 * as JSON maps.
 */
interface DashboardRepository {

    /**
     * @param keypass the value returned by a successful login.
     * @return the entities and their total on success, or a DataError if the
     * request failed. It never throws for network problems.
     */
    suspend fun getDashboard(keypass: String): DataResult<DashboardData>
}
