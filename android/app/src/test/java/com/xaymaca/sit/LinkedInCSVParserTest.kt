package com.xaymaca.sit

import com.xaymaca.sit.data.model.ImportSource
import com.xaymaca.sit.service.LinkedInCSVParser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LinkedInCSVParserTest {

    private lateinit var parser: LinkedInCSVParser
    private val gson = Gson()

    @Before
    fun setUp() {
        parser = LinkedInCSVParser()
    }

    private fun emails(contact: com.xaymaca.sit.data.model.Contact): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(contact.emails, type) ?: emptyList()
    }

    private fun csv(vararg lines: String): java.io.InputStream =
        lines.joinToString("\n").byteInputStream(Charsets.UTF_8)

    @Test
    fun `parses basic row correctly`() {
        val input = csv(
            "First Name,Last Name,Email Address,Company,Position",
            "Jane,Doe,jane@example.com,Acme,Engineer"
        )
        val result = parser.parse(input)
        assertEquals(1, result.size)
        val c = result[0]
        assertEquals("Jane", c.firstName)
        assertEquals("Doe", c.lastName)
        assertEquals(listOf("jane@example.com"), emails(c))
        assertEquals("Acme", c.company)
        assertEquals("Engineer", c.jobTitle)
        assertEquals(ImportSource.LINKEDIN.name, c.importSource)
    }

    @Test
    fun `skips rows with blank first and last name`() {
        val input = csv(
            "First Name,Last Name,Email Address,Company,Position",
            ",  ,ghost@example.com,NoName Corp,Ghost"
        )
        val result = parser.parse(input)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `handles quoted fields containing commas`() {
        val input = csv(
            "First Name,Last Name,Email Address,Company,Position",
            "\"Smith, Jr.\",Johnson,sj@example.com,\"Acme, Inc.\",\"VP, Sales\""
        )
        val result = parser.parse(input)
        assertEquals(1, result.size)
        assertEquals("Smith, Jr.", result[0].firstName)
        assertEquals("Acme, Inc.", result[0].company)
        assertEquals("VP, Sales", result[0].jobTitle)
    }

    @Test
    fun `handles escaped double quotes inside quoted fields`() {
        val input = csv(
            "First Name,Last Name,Email Address,Company,Position",
            "\"He said \"\"Hello\"\"\",Doe,h@example.com,Corp,Dev"
        )
        val result = parser.parse(input)
        assertEquals(1, result.size)
        assertEquals("He said \"Hello\"", result[0].firstName)
    }

    @Test
    fun `handles LinkedIn preamble lines before header`() {
        val input = csv(
            "Notes: This is a LinkedIn export.",
            "Exported on 2026-03-15",
            "",
            "First Name,Last Name,Email Address,Company,Position",
            "Alice,Smith,alice@example.com,FAANG,SWE"
        )
        val result = parser.parse(input)
        assertEquals(1, result.size)
        assertEquals("Alice", result[0].firstName)
    }

    @Test
    fun `returns empty list when no header row found`() {
        val input = csv(
            "This file has no recognizable headers",
            "Alice,Smith,alice@example.com"
        )
        val result = parser.parse(input)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns empty list for blank input`() {
        val result = parser.parse("".byteInputStream())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parses multiple rows`() {
        val input = csv(
            "First Name,Last Name,Email Address,Company,Position",
            "Alice,Smith,alice@example.com,Co A,Dev",
            "Bob,Jones,bob@example.com,Co B,Design",
            "Carol,White,,Co C,PM"
        )
        val result = parser.parse(input)
        assertEquals(3, result.size)
        assertEquals("Alice", result[0].firstName)
        assertEquals("Bob", result[1].firstName)
        assertEquals("Carol", result[2].firstName)
        assertTrue(emails(result[2]).isEmpty())
    }

    @Test
    fun `missing optional columns do not crash`() {
        // Only First Name and Last Name columns present
        val input = csv(
            "First Name,Last Name",
            "Alice,Smith"
        )
        val result = parser.parse(input)
        assertEquals(1, result.size)
        assertEquals("Alice", result[0].firstName)
        assertEquals("", result[0].company)
        assertEquals("", result[0].jobTitle)
        assertTrue(emails(result[0]).isEmpty())
    }

    @Test
    fun `skips blank lines between data rows`() {
        val input = csv(
            "First Name,Last Name,Email Address,Company,Position",
            "Alice,Smith,alice@example.com,Co A,Dev",
            "",
            "   ",
            "Bob,Jones,bob@example.com,Co B,Design"
        )
        val result = parser.parse(input)
        assertEquals(2, result.size)
    }
}
