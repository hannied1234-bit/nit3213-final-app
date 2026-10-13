// Says which folder (package) this file belongs to.
package com.vu.nit3213app.domain.model

/**
 * Everything the Dashboard screen needs from the dashboard endpoint.
 *
 * This is the tidy version of DashboardResponse. The response holds raw maps
 * straight from the JSON. This class holds finished [Entity] objects, so the
 * ViewModel and the screen never deal with raw JSON.
 */
// Creates a data class that holds the dashboard's results.
data class DashboardData(
    // The tidy items to show in the list.
    val entities: List<Entity>,
    // The total number of items, as reported by the server. Shown above the list.
    val total: Int
)
