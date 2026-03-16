package com.xaymaca.sit

import com.xaymaca.sit.service.StringListConverter
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StringListConverterTest {

    private lateinit var converter: StringListConverter

    @Before
    fun setUp() {
        converter = StringListConverter()
    }

    @Test
    fun `round-trip empty list`() {
        val json = converter.fromList(emptyList())
        val result = converter.fromString(json)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `round-trip single element`() {
        val original = listOf("+1 555-123-4567")
        val json = converter.fromList(original)
        assertEquals(original, converter.fromString(json))
    }

    @Test
    fun `round-trip multiple elements`() {
        val original = listOf("alice@example.com", "alice@work.com", "alice@personal.org")
        val json = converter.fromList(original)
        assertEquals(original, converter.fromString(json))
    }

    @Test
    fun `round-trip preserves elements with special characters`() {
        val original = listOf("tag with spaces", "emoji🎉", "comma,inside", "quote\"inside")
        val json = converter.fromList(original)
        assertEquals(original, converter.fromString(json))
    }

    @Test
    fun `fromString with invalid json returns empty list`() {
        val result = converter.fromString("not-valid-json")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `fromString with empty string returns empty list`() {
        val result = converter.fromString("[]")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `fromList produces valid json array`() {
        val json = converter.fromList(listOf("a", "b"))
        assertTrue(json.startsWith("["))
        assertTrue(json.endsWith("]"))
    }
}
