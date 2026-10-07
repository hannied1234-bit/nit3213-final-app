package com.vu.nit3213app.data.remote.dto

/**
 * JSON returned by the dashboard endpoint.
 *
 * Each entity is read as a Map because its field names depend on the topic.
 * Moshi turns JSON strings into String, numbers into Double, true or false into
 * Boolean, arrays into List and nested objects into Map. The EntityMapper then
 * converts these raw maps into tidy [com.vu.nit3213app.domain.model.Entity] objects.
 *
 * The default values mean a missing field in the JSON will not crash the app.
 */
data class DashboardResponse(
    val entities: List<Map<String, Any?>> = emptyList(),
    val entityTotal: Int = 0
)
