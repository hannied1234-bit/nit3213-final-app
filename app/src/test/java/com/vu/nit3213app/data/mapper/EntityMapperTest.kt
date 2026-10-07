package com.vu.nit3213app.data.mapper

import com.vu.nit3213app.domain.model.EntityProperty
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [EntityMapper]. Every topic returns different fields, so these
 * tests make sure any reasonable JSON shape is turned into tidy display text.
 */
class EntityMapperTest {

    private val mapper = EntityMapper()

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

    @Test
    fun `description key is matched regardless of case`() {
        val entity = mapper.map(mapOf("name" to "Koala", "Description" to "Sleepy."))

        assertEquals("Sleepy.", entity.description)
        assertEquals(1, entity.properties.size)
    }

    @Test
    fun `missing description becomes an empty string`() {
        val entity = mapper.map(mapOf("name" to "Koala"))

        assertEquals("", entity.description)
    }

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

    @Test
    fun `labels are turned into readable sentence case`() {
        assertEquals("Artist name", mapper.formatLabel("artistName"))
        assertEquals("Date of birth", mapper.formatLabel("date_of_birth"))
        assertEquals("Property 1", mapper.formatLabel("property1"))
        assertEquals("Image URL", mapper.formatLabel("imageURL"))
        assertEquals("ISBN", mapper.formatLabel("ISBN"))
        assertEquals("Name", mapper.formatLabel("name"))
    }

    @Test
    fun `whole numbers are shown without a decimal point`() {
        assertEquals("1937", mapper.formatValue(1937.0))
        assertEquals("0", mapper.formatValue(0.0))
    }

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

    @Test
    fun `nested objects are flattened into label and value pairs`() {
        val nested = mapOf("cityName" to "Footscray", "postcode" to 3011.0)

        assertEquals("City name: Footscray, Postcode: 3011", mapper.formatValue(nested))
    }
}
