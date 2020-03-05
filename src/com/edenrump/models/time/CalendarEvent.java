package com.edenrump.models.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CalendarEvent {

    private static final String DATE_PATTERN = "d/M/yyyy HH:mm:ss";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private LocalDateTime start;
    private LocalDateTime end;

    private String subject;

    public String getSubject() {
        return subject;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public Duration getDuration(){
        return Duration.between(this.start, this.end).abs();
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public CalendarEvent(String subject, String start, String end) {
        this.subject = subject;
        setStart(start);
        setEnd(end);
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public LocalDateTime setStart(String dateTime){
        this.start = coerceStringToDate(dateTime.trim());
        return this.start;
    }

    public LocalDateTime setEnd(String dateTime){
        this.end= coerceStringToDate(dateTime.trim());
        return this.end;
    }

    private LocalDateTime coerceStringToDate(String dateString){
        return LocalDateTime.parse(dateString, DATE_FORMAT);
    }
}
