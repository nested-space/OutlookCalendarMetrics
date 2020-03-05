package com.edenrump.util;

import com.edenrump.models.data.TableRow;
import com.edenrump.models.time.CalendarEvent;

import static com.edenrump.config.WindowsColumnHeaders.*;

public class CalendarUtils {
    public static CalendarEvent coerceRowToEvent(TableRow row){
        return new CalendarEvent(
                row.getEntry(SUBJECT),
                row.getEntry(START_DATE).trim() + " " + row.getEntry(START_TIME).trim(),
                row.getEntry(END_DATE).trim() + " " + row.getEntry(END_TIME).trim());
    }
}
