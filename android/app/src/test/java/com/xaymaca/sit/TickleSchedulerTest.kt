package com.xaymaca.sit

import com.xaymaca.sit.data.model.TickleFrequency
import com.xaymaca.sit.service.TickleScheduler
import org.junit.Test
import java.util.Calendar
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TickleSchedulerTest {

    private fun calAt(year: Int, month: Int, day: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month - 1, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun calFrom(millis: Long): Calendar =
        Calendar.getInstance().apply { timeInMillis = millis }

    @Test
    fun `DAILY adds exactly one day`() {
        val from = calAt(2026, 3, 15)
        val result = calFrom(TickleScheduler.nextDueDate(from, TickleFrequency.DAILY.name))
        assertEquals(2026, result.get(Calendar.YEAR))
        assertEquals(3, result.get(Calendar.MONTH) + 1)
        assertEquals(16, result.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `WEEKLY adds exactly seven days`() {
        val from = calAt(2026, 3, 15)
        val result = calFrom(TickleScheduler.nextDueDate(from, TickleFrequency.WEEKLY.name))
        assertEquals(2026, result.get(Calendar.YEAR))
        assertEquals(3, result.get(Calendar.MONTH) + 1)
        assertEquals(22, result.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `BIWEEKLY adds exactly fourteen days`() {
        val from = calAt(2026, 3, 15)
        val result = calFrom(TickleScheduler.nextDueDate(from, TickleFrequency.BIWEEKLY.name))
        assertEquals(2026, result.get(Calendar.YEAR))
        assertEquals(3, result.get(Calendar.MONTH) + 1)
        assertEquals(29, result.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `MONTHLY adds one calendar month`() {
        val from = calAt(2026, 1, 31)
        val result = calFrom(TickleScheduler.nextDueDate(from, TickleFrequency.MONTHLY.name))
        // Calendar rolls Jan 31 + 1 month → Feb 28 (non-leap 2026)
        assertEquals(2026, result.get(Calendar.YEAR))
        assertEquals(2, result.get(Calendar.MONTH) + 1)
        assertEquals(28, result.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `BIMONTHLY adds two calendar months`() {
        val from = calAt(2026, 3, 15)
        val result = calFrom(TickleScheduler.nextDueDate(from, TickleFrequency.BIMONTHLY.name))
        assertEquals(2026, result.get(Calendar.YEAR))
        assertEquals(5, result.get(Calendar.MONTH) + 1)
        assertEquals(15, result.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `QUARTERLY adds three calendar months`() {
        val from = calAt(2026, 3, 15)
        val result = calFrom(TickleScheduler.nextDueDate(from, TickleFrequency.QUARTERLY.name))
        assertEquals(2026, result.get(Calendar.YEAR))
        assertEquals(6, result.get(Calendar.MONTH) + 1)
        assertEquals(15, result.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `CUSTOM uses provided interval days`() {
        val from = calAt(2026, 3, 15)
        val result = calFrom(TickleScheduler.nextDueDate(from, TickleFrequency.CUSTOM.name, customDays = 10))
        assertEquals(2026, result.get(Calendar.YEAR))
        assertEquals(3, result.get(Calendar.MONTH) + 1)
        assertEquals(25, result.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `CUSTOM defaults to 30 days when customDays is null`() {
        val from = calAt(2026, 3, 1)
        val result = calFrom(TickleScheduler.nextDueDate(from, TickleFrequency.CUSTOM.name, customDays = null))
        assertEquals(2026, result.get(Calendar.YEAR))
        assertEquals(3, result.get(Calendar.MONTH) + 1)
        assertEquals(31, result.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `unknown frequency defaults to one month`() {
        val from = calAt(2026, 3, 15)
        val result = calFrom(TickleScheduler.nextDueDate(from, "BOGUS"))
        assertEquals(2026, result.get(Calendar.YEAR))
        assertEquals(4, result.get(Calendar.MONTH) + 1)
        assertEquals(15, result.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `result is always strictly after the input`() {
        val from = System.currentTimeMillis()
        for (freq in TickleFrequency.entries) {
            val next = TickleScheduler.nextDueDate(from, freq.name, customDays = 1)
            assertTrue(next > from, "Expected next > from for frequency ${freq.name}")
        }
    }

    @Test
    fun `QUARTERLY crosses year boundary correctly`() {
        val from = calAt(2025, 11, 15)
        val result = calFrom(TickleScheduler.nextDueDate(from, TickleFrequency.QUARTERLY.name))
        assertEquals(2026, result.get(Calendar.YEAR))
        assertEquals(2, result.get(Calendar.MONTH) + 1)
        assertEquals(15, result.get(Calendar.DAY_OF_MONTH))
    }
}
