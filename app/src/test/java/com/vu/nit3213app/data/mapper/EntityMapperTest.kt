package com.vu.nit3213app.data.mapper

import com.vu.nit3213app.domain.model.EntityProperty
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [EntityMapper]. Every topic returns different fields, so these
 * tests make sure any reasonable JSON shape is turned into tidy display text.
 *
 * The mapper is plain Kotlin with no dependencies, so the real class is used
 * directly. There's nothing to mock, and no coroutine or dispatcher set-up is
 * needed.
 *
 * Numbers in these tests are written as Doubles (1937.0, 3011.0) on purpose.
 * That's what Moshi produces from JSON, so the tests feed the mapper the same
 * types it gets in the real app.
 */
class EntityMapperTest {

    private val mapper = EntityMapper()

    // The spec requires the description to appear only on the Details screen,
    // so it must not end up in the properties list.
    @Test
    fun `description is separated from the other fields`() {
        val entity = mapper.map(
            mapOf(
                "name" to "Koala",
                "description" to "A tree-dwelling marsupial."
            )
        )

        assertEquals("A tree-dwelling marsupial.", entity.description)
        assertEquals(listOf(EntityProperty(label = "Name", value = "Koala")), entity.properties)
    }

    // Guards against a topic that capitalises its keys differently.
    @Test
    fun `description key is matched regardless of case`() {
        val entity = mapper.map(mapOf("name" to "Koala", "Description" to "Sleepy."))

        assertEquals("Sleepy.", entity.description)
        assertEquals(1, entity.properties.size)
    }

    // An empty string, not null. The UI relies on that to show its
    // "not provided" placeholder.
    @Test
    fun `missing description becomes an empty string`() {
        val entity = mapper.map(mapOf("name" to "Koala"))

        assertEquals("", entity.description)
    }

    // linkedMapOf has a guaranteed order, which this test depends on. It also
    // checks the title helpers, since they're built on the first field.
    @Test
    fun `fields keep the order the API returned them in`() {
        val entity = mapper.map(
            linkedMapOf("property1" to "first", "property2" to "second", "property3" to "third")
        )

        assertEquals(listOf("first", "second", "third"), entity.properties.map { it.value })
        assertEquals("first", entity.title)
        assertEquals("Property 1", entity.titleLabel)
        assertEquals(listOf("second", "third"), entity.summaryProperties.map { it.value })
    }

    // One assertion per naming style the API might use. "ISBN" checks that a
    // key which is already an acronym isn't lowercased.
    @Test
    fun `labels are turned into readable sentence case`() {
        assertEquals("Artist name", mapper.formatLabel("artistName"))
        assertEquals("Date of birth", mapper.formatLabel("date_of_birth"))
        assertEquals("Property 1", mapper.formatLabel("property1"))
        assertEquals("Image URL", mapper.formatLabel("imageURL"))
        assertEquals("ISBN", mapper.formatLabel("ISBN"))
        assertEquals("Name", mapper.formatLabel("name"))
    }

    // Without the special case in formatNumber, a year would show as "1937.0".
    @Test
    fun `whole numbers are shown without a decimal point`() {
        assertEquals("1937", mapper.formatValue(1937.0))
        assertEquals("0", mapper.formatValue(0.0))
    }

    // The opposite check, to make sure real decimals aren't rounded away.
    @Test
    fun `decimal numbers keep their decimal places`() {
        assertEquals("4.5", mapper.formatValue(4.5))
    }

    @Test
    fun `null becomes an empty string`() {
        assertEquals("", mapper.formatValue(null))
    }

    @Test
    fun `booleans and strings are shown as text`() {
        assertEquals("true", mapper.formatValue(true))
        assertEquals("Melbourne", mapper.formatValue("Melbourne"))
    }

    @Test
    fun `lists are joined with commas`() {
        assertEquals("red, green, blue", mapper.formatValue(listOf("red", "green", "blue")))
    }

    // Checks the recursion. The nested key gets a readable label and the
    // nested number still loses its ".0".
    @Test
    fun `nested objects are flattened into label and value pairs`() {
        val nested = mapOf("cityName" to "Footscray", "postcode" to 3011.0)

        assertEquals("City name: Footscray, Postcode: 3011", mapper.formatValue(nested))
    }
}
