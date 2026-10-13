// Says which folder (package) this file belongs to.
package com.vu.nit3213app.data.mapper

// Entity: our tidy, ready-to-show version of one item.
import com.vu.nit3213app.domain.model.Entity
// EntityProperty: one label and value pair, such as "Artist name: Pablo Picasso".
import com.vu.nit3213app.domain.model.EntityProperty
// Inject: the label that tells Hilt it can build this class.
import javax.inject.Inject
// abs: gives the size of a number without its minus sign.
import kotlin.math.abs

/**
 * Converts one raw entity from the API (a Map of field name to value) into an
 * [Entity] that is ready to display.
 *
 * Why is this needed at all? Every student gets a different topic, and each
 * topic's JSON uses different field names. Reading each entity as a map and
 * formatting it here lets one screen design work for any topic the API might
 * return.
 */
// Creates the mapper class. "@Inject constructor()" lets Hilt build it with no extra set-up.
class EntityMapper @Inject constructor() {

    /**
     * Builds an [Entity] from one JSON object. The fields come out in the same
     * order the API sent them, and the first one becomes the headline.
     */
    // Takes one raw item (field name to value) and returns a tidy Entity.
    fun map(rawEntity: Map<String, Any?>): Entity {
        // Finds the "description" field, if there is one.
        // Why separate: the spec says the Dashboard must leave it out but Details must show it.
        val descriptionEntry = rawEntity.entries.firstOrNull { isDescriptionKey(it.key) }

        // Builds the list of every other field.
        val properties = rawEntity
            // Keeps every field except the description.
            .filterKeys { key -> !isDescriptionKey(key) }
            // Turns each remaining field into a label and value pair.
            .map { (key, value) ->
                // Makes the label readable ("artistName" to "Artist name") and the value into text.
                EntityProperty(label = formatLabel(key), value = formatValue(value))
            }

        // Builds and returns the finished Entity.
        return Entity(
            // All the fields except the description.
            properties = properties,
            // The description as text. If it was missing this gives "", and the screen shows a placeholder.
            description = formatValue(descriptionEntry?.value)
        )
    }

    /**
     * Turns a JSON field name into a readable label in sentence case, for example:
     * "artistName" becomes "Artist name", "date_of_birth" becomes "Date of birth",
     * "property1" becomes "Property 1" and "imageURL" becomes "Image URL".
     */
    // Takes a field name and returns a readable label.
    fun formatLabel(key: String): String {
        // Splits the field name into separate words.
        val words = key
            // Puts a space before each capital letter: "artistName" to "artist Name".
            .replace(CAMEL_CASE_BOUNDARY, " ")
            // Puts a space between a letter and a number: "property1" to "property 1".
            .replace(LETTER_DIGIT_BOUNDARY, " ")
            // Turns underscores into spaces: "date_of_birth" to "date of birth".
            .replace('_', ' ')
            // Turns dashes into spaces: "first-name" to "first name".
            .replace('-', ' ')
            // Cuts the text into a list of words wherever there are spaces.
            .split(WHITESPACE)
            // Throws away any empty words.
            // Why: a key like "_id" leaves an empty word at the front after splitting.
            .filter { it.isNotBlank() }

        // If no words are left (a key like "__"), gives back the original key instead of nothing.
        if (words.isEmpty()) return key

        // Fixes the capital letters on each word, then joins them back together.
        return words.mapIndexed { index, word ->
            // Picks the right capitalisation for this word.
            when {
                // Acronyms such as "URL" or "ISBN" stay in capitals.
                isAcronym(word) -> word
                // The first word gets a capital first letter: "artist" to "Artist".
                index == 0 -> word.lowercase().replaceFirstChar { it.uppercase() }
                // Every other word goes lowercase: "Name" to "name".
                else -> word.lowercase()
            }
        // Joins the words with spaces: "Artist name".
        }.joinToString(" ")
    }

    /**
     * Turns any JSON value into display text. Missing (null) values become an
     * empty string, and the UI shows a "Not provided" message in their place.
     */
    // Takes any value from the JSON and returns it as text.
    fun formatValue(value: Any?): String = when (value) {
        // Missing value: return empty text.
        null -> ""
        // A number: format it so whole numbers lose their ".0" (see below).
        is Double -> formatNumber(value)
        // A list: format each item and join them with commas, such as "red, green".
        is List<*> -> value.joinToString(", ") { formatValue(it) }
        // A nested object: show each pair as "Label: value", joined with commas.
        is Map<*, *> -> value.entries.joinToString(", ") { (key, nestedValue) ->
            // Formats the nested key and value the same way as top-level ones.
            "${formatLabel(key.toString())}: ${formatValue(nestedValue)}"
        }
        // Anything else (text, true or false) is already fine, so just turn it into text.
        else -> value.toString()
    }

    /**
     * Moshi reads every JSON number as a Double, so a year like 1990 would show as
     * "1990.0". Whole numbers are therefore shown without the decimal point.
     */
    // Takes a number and returns it as text.
    private fun formatNumber(number: Double): String {
        // Checks whether it's a whole number (no remainder when divided by 1) and not huge.
        // Why the size check: past 2^53 a Double can't hold every whole number exactly.
        val isWholeNumber = number % 1.0 == 0.0 && abs(number) < MAX_EXACT_WHOLE_NUMBER
        // Whole: convert to a Long to drop the ".0". Otherwise keep the decimals.
        return if (isWholeNumber) number.toLong().toString() else number.toString()
    }

    // Returns true if this field name is "description". ignoreCase also matches "Description".
    private fun isDescriptionKey(key: String): Boolean =
        // Compares the key with "description", ignoring capital letters.
        key.equals(DESCRIPTION_KEY, ignoreCase = true)

    // Returns true if the word looks like an acronym such as "URL".
    private fun isAcronym(word: String): Boolean =
        // Longer than one letter, has at least one letter, and has no lowercase letters.
        word.length > 1 && word.any { it.isLetter() } && word.all { it.isUpperCase() || it.isDigit() }

    // Holds fixed values shared by every call.
    // Why here: the patterns are built once for the whole app, not again for every field.
    private companion object {
        // The field name that holds the long description.
        const val DESCRIPTION_KEY = "description"

        // 2^53. Below this, a Double can store every whole number exactly.
        const val MAX_EXACT_WHOLE_NUMBER = 9_007_199_254_740_992.0

        // A pattern that finds the gap before a capital letter, like between "t" and "N" in "artistName".
        val CAMEL_CASE_BOUNDARY = Regex("(?<=[a-z0-9])(?=[A-Z])")

        // A pattern that finds the gap between a letter and a number, like "y" and "1" in "property1".
        val LETTER_DIGIT_BOUNDARY = Regex("(?<=[A-Za-z])(?=[0-9])")

        // A pattern that matches one or more spaces.
        val WHITESPACE = Regex("\\s+")
    }
}
