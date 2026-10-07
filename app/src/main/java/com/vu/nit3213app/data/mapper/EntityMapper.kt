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
 */
class EntityMapper @Inject constructor() {

    fun map(rawEntity: Map<String, Any?>): Entity {
        // The description is kept separate because the spec says the Dashboard
        // summary must leave it out, while the Details screen must show it.
        val descriptionEntry = rawEntity.entries.firstOrNull { isDescriptionKey(it.key) }

        val properties = rawEntity
            .filterKeys { key -> !isDescriptionKey(key) }
            .map { (key, value) ->
                EntityProperty(label = formatLabel(key), value = formatValue(value))
            }

        return Entity(
            properties = properties,
            description = formatValue(descriptionEntry?.value)
        )
    }

    /**
     * Turns a JSON field name into a readable label in sentence case, for example:
     * "artistName" becomes "Artist name", "date_of_birth" becomes "Date of birth",
     * "property1" becomes "Property 1" and "imageURL" becomes "Image URL".
     */
    fun formatLabel(key: String): String {
        val words = key
            .replace(CAMEL_CASE_BOUNDARY, " ")
            .replace(LETTER_DIGIT_BOUNDARY, " ")
            .replace('_', ' ')
            .replace('-', ' ')
            .split(WHITESPACE)
            .filter { it.isNotBlank() }

        if (words.isEmpty()) return key

        return words.mapIndexed { index, word ->
            when {
                // Keep acronyms such as "ISBN" or "URL" in capitals.
                isAcronym(word) -> word
                index == 0 -> word.lowercase().replaceFirstChar { it.uppercase() }
                else -> word.lowercase()
            }
        }.joinToString(" ")
    }

    /**
     * Turns any JSON value into display text. Missing (null) values become an
     * empty string, and the UI shows a "Not provided" message in their place.
     */
    fun formatValue(value: Any?): String = when (value) {
        null -> ""
        is Double -> formatNumber(value)
        is List<*> -> value.joinToString(", ") { formatValue(it) }
        is Map<*, *> -> value.entries.joinToString(", ") { (key, nestedValue) ->
            "${formatLabel(key.toString())}: ${formatValue(nestedValue)}"
        }
        else -> value.toString()
    }

    /**
     * Moshi reads every JSON number as a Double, so a year like 1990 would show as
     * "1990.0". Whole numbers are therefore shown without the decimal point.
     */
    private fun formatNumber(number: Double): String {
        val isWholeNumber = number % 1.0 == 0.0 && abs(number) < MAX_EXACT_WHOLE_NUMBER
        return if (isWholeNumber) number.toLong().toString() else number.toString()
    }

    private fun isDescriptionKey(key: String): Boolean =
        key.equals(DESCRIPTION_KEY, ignoreCase = true)

    private fun isAcronym(word: String): Boolean =
        word.length > 1 && word.any { it.isLetter() } && word.all { it.isUpperCase() || it.isDigit() }

    private companion object {
        const val DESCRIPTION_KEY = "description"

        // Doubles can represent every whole number below 2^53 exactly.
        const val MAX_EXACT_WHOLE_NUMBER = 9_007_199_254_740_992.0

        // Matches the gap in "artistName" between "t" and "N".
        val CAMEL_CASE_BOUNDARY = Regex("(?<=[a-z0-9])(?=[A-Z])")

        // Matches the gap in "property1" between "y" and "1".
        val LETTER_DIGIT_BOUNDARY = Regex("(?<=[A-Za-z])(?=[0-9])")

        val WHITESPACE = Regex("\\s+")
    }
}
