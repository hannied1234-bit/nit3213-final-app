// Says which folder (package) this file belongs to.
package com.vu.nit3213app.data.remote.dto

/**
 * JSON returned by the dashboard endpoint.
 *
 * Example response:
 * { "entities": [ { "name": "...", "description": "..." } ], "entityTotal": 7 }
 *
 * DTO stands for Data Transfer Object. Classes in this package mirror the JSON
 * exactly and live only in the data layer. The EntityMapper then converts these
 * raw maps into tidy [com.vu.nit3213app.domain.model.Entity] objects.
 */
// Creates a data class that matches the dashboard JSON. Moshi fills it in from the reply.
data class DashboardResponse(
    // The list of items. Each item is a map of field name to value, because the fields change per topic.
    // Why Any?: a value could be text, a number, true or false, a list or missing, and Any? covers them all.
    // Why "= emptyList()": if the JSON is missing this field, we get an empty list instead of a crash.
    val entities: List<Map<String, Any?>> = emptyList(),
    // The total number of items, as reported by the server. Defaults to 0 if missing.
    val entityTotal: Int = 0
)
