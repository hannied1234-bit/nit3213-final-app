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
 *
 * Passing the whole object means the Details screen doesn't need to fetch
 * anything, because it already has every field. The API has no endpoint for
 * a single entity anyway, so passing an ID wouldn't work.
 *
 * Being a data class gives Entity an equals() that compares every field.
 * EntityAdapter's DiffUtil callback depends on that, since the API sends no
 * unique ID.
 */
@Parcelize
data class Entity(
    /** Every field except the description, in the order the API returned them. */
    val properties: List<EntityProperty>,
    /** The long description, shown only on the Details screen. Empty if missing. */
    val description: String
) : Parcelable {

    // The three properties below are computed with get() instead of stored.
    // @Parcelize only writes constructor properties into the Parcel, so these
    // add nothing to the data passed between screens. They're recalculated
    // from properties whenever they're read.

    /** The first field's value is used as the headline for the item. */
    val title: String
        get() = properties.firstOrNull()?.value.orEmpty()

    /** The first field's label, shown as a small heading above the title. */
    val titleLabel: String
        get() = properties.firstOrNull()?.label.orEmpty()

    /**
     * The remaining fields, shown as the summary on the Dashboard screen.
     * drop(1) skips the headline field so it isn't printed twice on the card.
     */
    val summaryProperties: List<EntityProperty>
        get() = properties.drop(1)
}

/**
 * A single field of an [Entity], already formatted for display.
 * For example, the JSON pair "artistName": "Pablo Picasso" becomes
 * label = "Artist name" and value = "Pablo Picasso".
 *
 * It's Parcelable as well, since a Parcelable class can only contain types
 * that can also be written to a Parcel.
 */
@Parcelize
data class EntityProperty(
    val label: String,
    val value: String
) : Parcelable
