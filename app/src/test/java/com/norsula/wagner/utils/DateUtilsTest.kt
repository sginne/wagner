package com.norsula.wagner.utils

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DateUtilsTest {
    @Test
    fun parsesWordPressIsoDate() {
        assertEquals(
            LocalDate.of(2026, 7, 29),
            parseComicDate("2026-07-29T19:25:44+00:00")
        )
    }

    @Test
    fun parsesPlainDate() {
        assertEquals(LocalDate.of(2026, 7, 29), parseComicDate("2026-07-29"))
    }

    @Test
    fun rejectsInvalidDate() {
        assertNull(parseComicDate("nonsense"))
    }
}
