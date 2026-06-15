package com.worldcup26.reminder

import com.worldcup26.reminder.data.remote.KickoffParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

class KickoffParserTest {

    @Test
    fun parsesUtcMinus6() {
        // 2026-06-11 13:00 in UTC-6 == 2026-06-11 19:00 UTC.
        val instant = KickoffParser.toInstant("2026-06-11", "13:00 UTC-6")
        val expected = OffsetDateTime.of(2026, 6, 11, 19, 0, 0, 0, ZoneOffset.UTC).toInstant()
        assertEquals(expected, instant)
    }

    @Test
    fun parsesUtcMinus4DifferentOffset() {
        val instant = KickoffParser.toInstant("2026-06-18", "12:00 UTC-4")
        val expected = OffsetDateTime.of(2026, 6, 18, 16, 0, 0, 0, ZoneOffset.UTC).toInstant()
        assertEquals(expected, instant)
    }

    @Test
    fun returnsNullForGarbage() {
        assertNull(KickoffParser.toInstant("not-a-date", "??"))
    }
}
