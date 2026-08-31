package com.ttn.ai.core.util;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

/**
 * Formats ticket timestamps for UI display in IST.
 */
public final class TicketDateFormatter {

    public static final String PATTERN = "dd/MM/yy HH:mm";
    public static final String TIMEZONE_ID = "Asia/Kolkata";

    private static final ZoneId ZONE = ZoneId.of(TIMEZONE_ID);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(PATTERN).withZone(ZONE);

    private TicketDateFormatter() {
    }

    public static String format(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        return FORMATTER.format(calendar.toInstant());
    }
}
