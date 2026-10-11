package com.vu.nit3213app.data.mapper

import com.vu.nit3213app.domain.model.Entity
import com.vu.nit3213app.domain.model.EntityProperty
import javax.inject.Inject
import kotlin.math.abs

/**
 * Converts one raw entity from the API (a Map of field name to value) into an
 * [Entity] that is ready to display.
 *
 * It has a single job (the Single Responsibility Principle), contains no Android
 * code, and is therefore easy to unit test. The @Inject constructor tells Hilt
 * how to create it, so the repository can simply ask for one.
 *
 * Why is this needed at all? Every student gets a different topic, and each
 * topic's JSON uses different field names. A typed data class per topic would
 * mean guessing every possible shape in advance. Reading each entity as a map
 * and formatting it here lets one screen design work for any topic the API
 * might return.
 */
class EntityMapper @Inject constructor() {

    /**
     * Builds an [Entity] from one JSON object.
     *
     * Moshi reads JSON objects into a LinkedHashMap, which keeps keys in the
     * order they appeared in the response. The properties list therefore comes
     * out in the same order the API sent them, and the first field becomes the
     * headline on both screens.
     */
    fun map(rawEntity: Map<String, Any?>): Entity {
        // The description is kept separate because the spec says the Dashboard
        // summary must leave it out, while the Details screen must show it.
        val descriptionEntry = rawEntity.entries.firstOrNull { isDescriptionKey(it.key) }

        // filterKeys keeps the original map order, so removing the description
        // doesn't shuffle the remaining fields.
        val properties = rawEntity
            .filterKeys { key -> !isDescriptionKey(key) }
            .map { (key, value) ->
                EntityProperty(label = formatLabel(key), value = formatValue(value))
            }

        return Entity(
            properties = properties,
            // A missing description gives null here, and formatValue turns null
            // into "". The UI checks for blank text and shows a placeholder.
            description = formatValue(descriptionEntry?.value)
        )
    }

    /**
     * Turns a JSON field name into a readable label in sentence case, for example:
     * "artistName" becomes "Artist name", "date_of_birth" becomes "Date of birth",
     * "property1" becomes "Property 1" and "imageURL" becomes "Image URL".
     *
     * The approach is to split the key into words first, then fix the
     * capitalisation of each word. APIs mix camelCase, snake_case and kebab-case,
     * so each style gets its own split step and they all end up as spaces.
     */
    fun formatLabel(key: String): String {
        val words = key
            // "artistName" -> "artist Name"
            .replace(CAMEL_CASE_BOUNDARY, " ")
            // "property1" -> "property 1"
            .replace(LETTER_DIGIT_BOUNDARY, " ")
            // "date_of_birth" -> "date of birth"
            .replace('_', ' ')
            // "first-name" -> "first name"
            .replace('-', ' ')
            .split(WHITESPACE)
            // A key like "_id" leaves an empty string at the front after the
            // split. Dropping blanks stops that becoming a stray leading space.
            .filter { it.isNotBlank() }

        // A key made only of separators (say "__") has no words left. Returning
        // the raw key is safer than showing an empty label.
        if (words.isEmpty()) return key

        return words.mapIndexed { index, word ->
            when {
                // Keep acronyms such as "ISBN" or "URL" in capitals.
                isAcronym(word) -> word
                // Sentence case: only the first word gets a capital letter.
                index == 0 -> word.lowercase().replaceFirstChar { it.uppercase() }
                else -> word.lowercase()
            }
        }.joinToString(" ")
    }

    /**
     * Turns any JSON value into display text. Missing (null) values become an
     * empty string, and the UI shows a "Not provided" message in their place.
     *
     * Lists and nested objects call this function again on each item, so a
     * value nested several levels deep still comes out as readable text.
     */
    fun formatValue(value: Any?): String = when (value) {
        null -> ""
        is Double -> formatNumber(value)
        // ["red", "green"] -> "red, green"
        is List<*> -> value.joinToString(", ") { formatValue(it) }
        // {"cityName": "Footscray"} -> "City name: Footscray". The nested keys
        // get the same label treatment as top-level ones, so they read the same.
        is Map<*, *> -> value.entries.joinToString(", ") { (key, nestedValue) ->
            "${formatLabel(key.toString())}: ${formatValue(nestedValue)}"
        }
        // Strings and Booleans are already fine as they are.
        else -> value.toString()
    }

    /**
     * Moshi reads every JSON number as a Double, so a year like 1990 would show as
     * "1990.0". Whole numbers are therefore shown without the decimal point.
     */
    private fun formatNumber(number: Double): String {
        // number % 1.0 == 0.0 means there's no fractional part. The size check
        // matters too. Past 2^53 a Double can't hold every whole number exactly,
        // so converting it to a Long could print digits that weren't in the JSON.
        val isWholeNumber = number % 1.0 == 0.0 && abs(number) < MAX_EXACT_WHOLE_NUMBER
        return if (isWholeNumber) number.toLong().toString() else number.toString()
    }

    // ignoreCase covers APIs that send "Description" or "DESCRIPTION".
    private fun isDescriptionKey(key: String): Boolean =
        key.equals(DESCRIPTION_KEY, ignoreCase = true)

    // An acronym here is two or more characters, at least one letter, and no
    // lowercase letters. The length check stops a single capital like "A" being
    // treated as one. Digits are allowed so "MP3" stays as it is.
    private fun isAcronym(word: String): Boolean =
        word.length > 1 && word.any { it.isLetter() } && word.all { it.isUpperCase() || it.isDigit() }

    // Private companion so the Regex objects are compiled once for the whole
    // app. Building a Regex inside formatLabel would recompile it for every
    // field of every entity.
    private companion object {
        const val DESCRIPTION_KEY = "description"

        // Doubles can represent every whole number below 2^53 exactly.
        const val MAX_EXACT_WHOLE_NUMBER = 9_007_199_254_740_992.0

        // Matches the gap in "artistName" between "t" and "N".
        // (?<=...) and (?=...) are lookarounds. They match a position between
        // two characters without consuming either, so replace() inserts a space
        // and keeps both letters.
        val CAMEL_CASE_BOUNDARY = Regex("(?<=[a-z0-9])(?=[A-Z])")

        // Matches the gap in "property1" between "y" and "1".
        val LETTER_DIGIT_BOUNDARY = Regex("(?<=[A-Za-z])(?=[0-9])")

        // One or more spaces, so a double space left by the steps above
        // doesn't produce an empty word.
        val WHITESPACE = Regex("\\s+")
    }
}
