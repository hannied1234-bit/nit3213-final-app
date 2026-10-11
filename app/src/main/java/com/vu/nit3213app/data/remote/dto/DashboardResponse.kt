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
 * KotlinJsonAdapterFactory (added in NetworkModule) is what makes Moshi respect
 * these defaults. Without it, a missing "entities" key would throw instead.
 *
 * DTO stands for Data Transfer Object. Classes in this package mirror the JSON
 * exactly and live only in the data layer. The rest of the app works with the
 * domain models, so a change to the API's JSON only touches this package and
 * the mapper.
 *
 * Example response:
 * { "entities": [ { "name": "...", "description": "..." } ], "entityTotal": 7 }
 */
data class DashboardResponse(
    // Any? because a field's value could be a string, number, boolean, list,
    // object or null, and Kotlin needs one type that covers all of them.
    val entities: List<Map<String, Any?>> = emptyList(),
    val entityTotal: Int = 0
)
