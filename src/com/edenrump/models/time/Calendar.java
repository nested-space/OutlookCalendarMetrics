package com.edenrump.models.time;

import com.edenrump.models.data.Table;
import com.edenrump.models.data.TableRow;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        return calculateTotalBookedTime(getCategories());
    }

    public Duration calculateTotalBookedTime(Set<String> includedCategories){
        return calculateTotalTime(includedCategories, true, true);
    }

    public Duration calculateTimeInMeetings(){
        return calculateTimeInMeetings(getCategories());
    }

    public Duration calculateTimeInMeetings(Set<String> includedCategories){
        return calculateTotalTime(includedCategories, true, false);
    }

    public Duration calculateTimeInAppointments(){
        return calculateTimeInAppointments(getCategories());
    }

    public Duration calculateTimeInAppointments(Set<String> includedCategories){
        return calculateTotalTime(includedCategories, false, true);
    }

    private Duration calculateTotalTime(Set<String> includedCategories, boolean includeMeetings, boolean includeAppointments){
        Duration total = Duration.ZERO;
        for(CalendarEvent event : events){
            boolean include = false;
            if(includedCategories.contains(event.getCategory())) include = true;
            if(includeMeetings && !event.isMeeting()) include = false;
            if(includeAppointments && !event.isAppointment()) include = false;
            if(include)  total = Duration.ofMillis(total.toMillis() + event.getDuration().toMillis());
        }
        return total;
    }

    public Set<String> getCategories(){
        Set<String> categories = new HashSet<>();
        for(CalendarEvent event: events){
            categories.add(event.getCategory());
        }
        return categories;
    }
}
