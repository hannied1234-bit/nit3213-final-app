package com.vu.nit3213app.domain.model

/**
 * Everything the Dashboard screen needs from the dashboard endpoint.
 *
 * @property entities the items to show in the list.
 * @property total the "entityTotal" value reported by the API.
 */
data class DashboardData(
    val entities: List<Entity>,
    val total: Int
)
