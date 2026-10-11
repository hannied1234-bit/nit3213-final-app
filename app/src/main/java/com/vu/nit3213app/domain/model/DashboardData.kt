package com.vu.nit3213app.domain.model

/**
 * Everything the Dashboard screen needs from the dashboard endpoint.
 *
 * This is the domain version of DashboardResponse. The response holds raw maps
 * straight from the JSON. This class holds finished [Entity] objects, so the
 * ViewModel and the UI never deal with raw JSON.
 *
 * @property entities the items to show in the list.
 * @property total the "entityTotal" value reported by the API. It's shown as
 * the count above the list. The API's own number is used instead of
 * entities.size so the screen reports exactly what the server says.
 */
data class DashboardData(
    val entities: List<Entity>,
    val total: Int
)
