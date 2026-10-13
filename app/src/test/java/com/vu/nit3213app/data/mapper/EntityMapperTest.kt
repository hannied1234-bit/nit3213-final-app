// Says which folder (package) this file belongs to.
package com.vu.nit3213app.data.mapper

// EntityProperty: one label and value pair, used to build expected results.
import com.vu.nit3213app.domain.model.EntityProperty
// assertEquals: fails the test if two values aren't equal.
import org.junit.Assert.assertEquals
// Test: marks a function as a test for JUnit to run.
import org.junit.Test

/**
 * Unit tests for [EntityMapper]. Every topic returns different fields, so these
 * tests make sure any reasonable JSON shape is turned into tidy display text.
 *
 * Numbers are written as Doubles (1937.0) on purpose, because that's what Moshi
 * produces from JSON in the real app.
 */
// Creates the test class.
class EntityMapperTest {

    // The real mapper. There's nothing to mock, because it has no dependencies.
    private val mapper = EntityMapper()

    // Marks this function as a test.
    @Test
    // Checks that the description is kept apart from the other fields.
    fun `description is separated from the other fields`() {
        // Runs the mapper on a small raw item.
        val entity = mapper.map(
            // A raw item with a name and a description.
            mapOf(
                // A normal field.
                "name" to "Koala",
                // The description field.
                "description" to "A tree-dwelling marsupial."
            )
        )

        // Checks the description came out separately.
        assertEquals("A tree-dwelling marsupial.", entity.description)
        // Checks the fields list holds only the name, with a readable label.
        assertEquals(listOf(EntityProperty(label = "Name", value = "Koala")), entity.properties)
    }

    // Marks this function as a test.
    @Test
    // Checks that "Description" with a capital D is still treated as the description.
    fun `description key is matched regardless of case`() {
        // Runs the mapper on an item with a capitalised description key.
        val entity = mapper.map(mapOf("name" to "Koala", "Description" to "Sleepy."))

        // Checks it was picked up as the description.
        assertEquals("Sleepy.", entity.description)
        // Checks only one field (the name) is left.
        assertEquals(1, entity.properties.size)
    }

    // Marks this function as a test.
    @Test
    // Checks that a missing description becomes empty text, not null.
    fun `missing description becomes an empty string`() {
        // Runs the mapper on an item with no description.
        val entity = mapper.map(mapOf("name" to "Koala"))

        // Checks the description is "". The screen then shows its placeholder.
        assertEquals("", entity.description)
    }

    // Marks this function as a test.
    @Test
    // Checks the fields stay in the order the server sent them.
    fun `fields keep the order the API returned them in`() {
        // Runs the mapper on three fields. linkedMapOf keeps them in this exact order.
        val entity = mapper.map(
            // Three numbered fields.
            linkedMapOf("property1" to "first", "property2" to "second", "property3" to "third")
        )

        // Checks the values come out in order.
        assertEquals(listOf("first", "second", "third"), entity.properties.map { it.value })
        // Checks the headline is the first field's value.
        assertEquals("first", entity.title)
        // Checks the headline label is the first field's readable name.
        assertEquals("Property 1", entity.titleLabel)
        // Checks the summary skips the first field.
        assertEquals(listOf("second", "third"), entity.summaryProperties.map { it.value })
    }

    // Marks this function as a test.
    @Test
    // Checks field names become readable labels in each naming style.
    fun `labels are turned into readable sentence case`() {
        // camelCase.
        assertEquals("Artist name", mapper.formatLabel("artistName"))
        // snake_case.
        assertEquals("Date of birth", mapper.formatLabel("date_of_birth"))
        // A word followed by a number.
        assertEquals("Property 1", mapper.formatLabel("property1"))
        // An acronym at the end stays in capitals.
        assertEquals("Image URL", mapper.formatLabel("imageURL"))
        // A key that's all one acronym stays in capitals.
        assertEquals("ISBN", mapper.formatLabel("ISBN"))
        // A simple word gets a capital first letter.
        assertEquals("Name", mapper.formatLabel("name"))
    }

    // Marks this function as a test.
    @Test
    // Checks whole numbers lose their ".0".
    fun `whole numbers are shown without a decimal point`() {
        // A year.
        assertEquals("1937", mapper.formatValue(1937.0))
        // Zero.
        assertEquals("0", mapper.formatValue(0.0))
    }

    // Marks this function as a test.
    @Test
    // Checks real decimals aren't rounded away.
    fun `decimal numbers keep their decimal places`() {
        // A rating with a half.
        assertEquals("4.5", mapper.formatValue(4.5))
    }

    // Marks this function as a test.
    @Test
    // Checks a missing value becomes empty text.
    fun `null becomes an empty string`() {
        // null in, "" out.
        assertEquals("", mapper.formatValue(null))
    }

    // Marks this function as a test.
    @Test
    // Checks true or false and text are shown as they are.
    fun `booleans and strings are shown as text`() {
        // A boolean.
        assertEquals("true", mapper.formatValue(true))
        // A string.
        assertEquals("Melbourne", mapper.formatValue("Melbourne"))
    }

    // Marks this function as a test.
    @Test
    // Checks a list becomes comma-separated text.
    fun `lists are joined with commas`() {
        // Three colours in, one line of text out.
        assertEquals("red, green, blue", mapper.formatValue(listOf("red", "green", "blue")))
    }

    // Marks this function as a test.
    @Test
    // Checks a nested object becomes "Label: value" pairs.
    fun `nested objects are flattened into label and value pairs`() {
        // A nested object with a camelCase key and a number.
        val nested = mapOf("cityName" to "Footscray", "postcode" to 3011.0)

        // Checks the nested key gets a readable label and the number loses its ".0".
        assertEquals("City name: Footscray, Postcode: 3011", mapper.formatValue(nested))
    }
}
