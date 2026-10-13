// Says which folder (package) this file belongs to.
package com.vu.nit3213app.domain.repository

// DashboardData: the list of entities plus the total.
import com.vu.nit3213app.domain.model.DashboardData
// DataResult: our "worked or failed" result type.
import com.vu.nit3213app.domain.model.DataResult

/**
 * Loads the list of entities for the signed-in user's topic.
 * See [AuthRepository] for why this is an interface.
 */
// Creates the interface: a promise that something can fetch the dashboard, without saying how.
interface DashboardRepository {

    /**
     * @param keypass the value returned by a successful login.
     * @return the entities and their total on success, or a DataError if the
     * request failed. It never throws for network problems.
     */
    // The fetch function. Waits for the server without freezing the app.
    // Returns Success with the list and total, or Error with the kind of problem.
    suspend fun getDashboard(keypass: String): DataResult<DashboardData>
}
