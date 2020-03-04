package com.edenrump.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Table {

    private List<String> headers = new ArrayList<>();

    private Map<Integer, TableRow> entries = new HashMap<>();

    public void setColumnTitles(List<String> headers) {
        this.headers = headers;
    }

    public List<String> getColumnTitles() {
        return headers;
    }

    public void addColumn(String header) {
        this.headers.add(header);
    }

    public void removeColumn(String header) {
        this.headers.remove(header);
        for (TableRow row : entries.values()) {
            row.removeColumn(header);
        }
    }

    public boolean addRow(TableRow row) {
        if (!validateTableRowColumnHeaders(row)) return false;

        entries.put(getHighestIndex() + 1, row);
        return true;
    }

    private int getHighestIndex() {
        int highestIndex = 0;
        for (Integer index : entries.keySet()) {
            highestIndex = Math.max(index, highestIndex);
        }
        return highestIndex;
    }

    private boolean validateTableRowColumnHeaders(TableRow row) {
        List<String> rowHeaders = new ArrayList<>(row.getHeaders());
        for (String knownHeader : this.headers) {
            rowHeaders.remove(knownHeader);
        }
        return rowHeaders.size() == 0;
    }

}
