package com.vu.nit3213app.domain.repository

import com.vu.nit3213app.domain.model.DashboardData
import com.vu.nit3213app.domain.model.DataResult

/**
 * Loads the list of entities for the signed-in user's topic.
 * See [AuthRepository] for why this is an interface.
 */
interface DashboardRepository {

    /**
     * @param keypass the value returned by a successful login.
     * @return the entities and their total on success.
     */
    suspend fun getDashboard(keypass: String): DataResult<DashboardData>
}
