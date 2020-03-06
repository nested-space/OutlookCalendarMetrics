package com.edenrump.tests;

import com.edenrump.loaders.CSVUtils;
import com.edenrump.models.data.Table;
import com.edenrump.models.data.TableRow;
import com.edenrump.models.time.CalendarEvent;
import com.edenrump.models.time.CalendarUtils;

import java.io.File;

public class CalendarTester {

    public static void main(String[] args){
        Table loadedData = CSVUtils.loadCSV(new File("C:/Users/kvcb654/Desktop/2020-03-02-start.csv"));
        System.out.println(loadedData.getRows().size());

        for(TableRow row: loadedData.getRows()){
            CalendarEvent calendarEvent = CalendarUtils.coerceRowToEvent(row);
            System.out.println(row.getEntry("Subject")+ "-> length: " + calendarEvent.getDuration().toMinutes() + " hours");
        }
    }

}
