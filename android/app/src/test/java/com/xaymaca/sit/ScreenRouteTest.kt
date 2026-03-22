package com.xaymaca.sit

import com.xaymaca.sit.ui.nav.Screen
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ScreenRouteTest {

    @Test
    fun `createRouteWithContact produces tickleId of -1 with contactId query param`() {
        val route = Screen.TickleEdit.createRouteWithContact(contactId = 42L)
        assertEquals("tickle_edit/-1?contactId=42", route)
    }

    @Test
    fun `createRouteWithContact works for any contactId`() {
        assertEquals("tickle_edit/-1?contactId=1", Screen.TickleEdit.createRouteWithContact(contactId = 1L))
        assertEquals("tickle_edit/-1?contactId=999", Screen.TickleEdit.createRouteWithContact(contactId = 999L))
    }

    @Test
    fun `createRoute without contactId produces plain path`() {
        assertEquals("tickle_edit/7", Screen.TickleEdit.createRoute(id = 7L))
    }

    @Test
    fun `createRoute defaults tickleId to -1 for new tickle`() {
        assertEquals("tickle_edit/-1", Screen.TickleEdit.createRoute())
    }

    @Test
    fun `ROUTE template contains both tickleId and optional contactId placeholders`() {
        assertTrue(Screen.TickleEdit.ROUTE.contains("{tickleId}"))
        assertTrue(Screen.TickleEdit.ROUTE.contains("{contactId}"))
    }

    @Test
    fun `createRouteWithContact and createRoute produce distinct routes`() {
        val withContact = Screen.TickleEdit.createRouteWithContact(contactId = 5L)
        val withoutContact = Screen.TickleEdit.createRoute()
        assertNotEquals(withContact, withoutContact)
    }

    @Test
    fun `createRouteWithContact always uses -1 as tickle placeholder regardless of contactId`() {
        val route = Screen.TickleEdit.createRouteWithContact(contactId = 100L)
        assertTrue(route.startsWith("tickle_edit/-1"))
    }
}
