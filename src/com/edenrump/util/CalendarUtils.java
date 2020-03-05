package com.edenrump.util;

import com.edenrump.models.data.Table;
import com.edenrump.models.data.TableRow;
import com.edenrump.models.time.CalendarEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.edenrump.config.WindowsColumnHeaders.*;

public class CalendarUtils {
    public static CalendarEvent coerceRowToEvent(TableRow row){
        return new CalendarEvent(
                row.getEntry(SUBJECT),
                row.getEntry(START_DATE).trim() + " " + row.getEntry(START_TIME).trim(),
                row.getEntry(END_DATE).trim() + " " + row.getEntry(END_TIME).trim());
    }

    public static boolean validateTable(Table data) {
        List<String> required = new ArrayList<>(
                Arrays.asList(START_DATE, START_TIME, END_DATE, END_TIME, ORGANISER, REQUIRED_ATTENDEES, CATEGORIES));
        for (String header : data.getColumnTitles()) {
            required.remove(header);
        }
        return required.size() == 0;
    }
}
