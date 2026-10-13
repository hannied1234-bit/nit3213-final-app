// Says which folder (package) this file belongs to.
package com.vu.nit3213app.domain.model

// Parcelable: lets an object be packed up and passed between screens.
import android.os.Parcelable
// Parcelize: writes the Parcelable packing code for us.
import kotlinx.parcelize.Parcelize

/**
 * One item returned by the dashboard endpoint, in the shape the UI needs.
 *
 * The fields inside each entity change depending on the topic (keypass) the API
 * gives you, so instead of hard-coding names like "artistName" we keep a list of
 * label and value pairs. This means the app works with any topic.
 */
// Writes the code that lets an Entity be passed from the Dashboard to Details.
@Parcelize
// Creates the Entity data class. "data class" gives it an equals() that compares every field.
data class Entity(
    // Every field except the description, in the order the server sent them.
    val properties: List<EntityProperty>,
    // The long description, shown only on the Details screen. Empty if missing.
    val description: String
// ": Parcelable" means it can be packed up and passed between screens.
) : Parcelable {

    // The headline: the first field's value, such as "Pablo Picasso".
    val title: String
        // Worked out when read. If there are no fields, gives "" instead of crashing.
        get() = properties.firstOrNull()?.value.orEmpty()

    // The first field's label, such as "Artist name". Shown small above the headline.
    val titleLabel: String
        // Worked out when read. Gives "" if there are no fields.
        get() = properties.firstOrNull()?.label.orEmpty()

    // Every field after the first one. Shown as the summary on the Dashboard card.
    val summaryProperties: List<EntityProperty>
        // drop(1) skips the first field, so the headline isn't shown twice.
        get() = properties.drop(1)
}

/**
 * A single field of an [Entity], already formatted for display.
 * For example, the JSON pair "artistName": "Pablo Picasso" becomes
 * label = "Artist name" and value = "Pablo Picasso".
 */
// Writes the packing code, since anything inside a Parcelable must also be Parcelable.
@Parcelize
// Creates the label and value pair class.
data class EntityProperty(
    // The readable field name, such as "Artist name".
    val label: String,
    // The value as text, such as "Pablo Picasso".
    val value: String
// Can be packed up and passed between screens.
) : Parcelable
