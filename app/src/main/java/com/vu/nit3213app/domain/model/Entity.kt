package com.vu.nit3213app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * One item returned by the dashboard endpoint, in the shape the UI needs.
 *
 * The fields inside each entity change depending on the topic (keypass) the API
 * gives you, so instead of hard-coding names like "artistName" we keep a list of
 * label and value pairs. This means the app works with any topic.
 *
 * @Parcelize generates the Parcelable code that lets Safe Args pass a whole
 * Entity from the Dashboard screen to the Details screen.
 */
@Parcelize
data class Entity(
    /** Every field except the description, in the order the API returned them. */
    val properties: List<EntityProperty>,
    /** The long description, shown only on the Details screen. Empty if missing. */
    val description: String
) : Parcelable {

    /** The first field's value is used as the headline for the item. */
    val title: String
        get() = properties.firstOrNull()?.value.orEmpty()

    /** The first field's label, shown as a small heading above the title. */
    val titleLabel: String
        get() = properties.firstOrNull()?.label.orEmpty()

    /** The remaining fields, shown as the summary on the Dashboard screen. */
    val summaryProperties: List<EntityProperty>
        get() = properties.drop(1)
}

/**
 * A single field of an [Entity], already formatted for display.
 * For example, the JSON pair "artistName": "Pablo Picasso" becomes
 * label = "Artist name" and value = "Pablo Picasso".
 */
@Parcelize
data class EntityProperty(
    val label: String,
    val value: String
) : Parcelable
