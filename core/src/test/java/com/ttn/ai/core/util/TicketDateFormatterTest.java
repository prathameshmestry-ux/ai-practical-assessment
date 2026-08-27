package com.ttn.ai.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

class TicketDateFormatterTest {

    @Test
    void formatsCalendarInAsiaKolkata() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(2026, Calendar.AUGUST, 27, 5, 30, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        assertEquals("27/08/26 11:00", TicketDateFormatter.format(calendar));
    }

    @Test
    void returnsNullForNullCalendar() {
        assertEquals(null, TicketDateFormatter.format(null));
    }
}
