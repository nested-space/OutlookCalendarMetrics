package com.edenrump.models.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableRow {
    Map<String, String> entries = new HashMap<>();

    public void addEntry(String header, String value) {
        entries.put(header, value);
    }

    public String getEntry(String header) {
        return entries.get(header);
    }

    public void removeColumn(String header) {
        entries.remove(header);
    }

    public Set<String> getHeaders() {
        return entries.keySet();
    }

    public static TableRow zip(List<String> headers, List<String> entries) {
        TableRow row = new TableRow();
        for (int i = 0; i < headers.size(); i++) {
            row.addEntry(headers.get(i), entries.get(i));
        }
        return row;
    }
}
