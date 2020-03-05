package com.edenrump.models.time;

import com.edenrump.models.data.Table;
import com.edenrump.models.data.TableRow;
import com.edenrump.util.CalendarUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Calendar {

    private List<CalendarEvent> events = new ArrayList<>();

    private Calendar(){}

    public Calendar(Table data){
        if(!CalendarUtils.validateTable(data)) return;

        for(TableRow row : data.getRows()){
            events.add(CalendarUtils.coerceRowToEvent(row));
        }
    }

    public Duration calculateTotalBookedTime(){
        Duration total = Duration.ZERO;
        for(CalendarEvent event : events){
            total = Duration.ofMillis(total.toMillis() + event.getDuration().toMillis());
        }
        return total;
    }

}
