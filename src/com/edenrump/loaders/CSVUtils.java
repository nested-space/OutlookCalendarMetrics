package com.edenrump.loaders;

import com.edenrump.models.Table;
import com.edenrump.models.TableRow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CSVUtils {

    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';

    public static Table loadCSV(File file) {
        return loadCSV(file, true);
    }

    public static Table loadCSV(File file, boolean includesHeaders) {
        String content = loadFileToString(file);
        return parseMultiLineContentToTable(content, includesHeaders);
    }

    private static Table parseMultiLineContentToTable(String content, boolean includesHeaders) {
        Table table = new Table();
        String[] lines = content.split("\n");
        int rowCaret = 0;

        int numberOfColumns = parseLine(lines[0]).size();

        List<String> headers;
        if (includesHeaders) {
            headers = parseLine(lines[0]);
            rowCaret++;
        } else {
            headers = new ArrayList<>();
            for(int i=0; i<numberOfColumns; i++){
                headers.add("Column " + (i+1));
            }
        }
        table.setColumnTitles(headers);

        for(int i=rowCaret; i<lines.length; i++){
            List<String> entries = parseLine(lines[i]);
            TableRow row = TableRow.zip(headers, entries);
            table.addRow(row);
        }

        return table;
    }

    private static String loadFileToString(File file) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            content = removeLineBreaksInsideQuotes(content, DEFAULT_QUOTE);
            return content;
        } catch (IOException e) {
            System.out.println("Could not load file: " + file.getAbsolutePath());
            return "";
        }
    }

    public static String removeLineBreaksInsideQuotes(String content, char quote) {
        char[] charContent = content.toCharArray();
        StringBuilder curatedContent = new StringBuilder();

        boolean currentCharInsideField = false;
        for (int i = 0; i < charContent.length; i++) {
            char currentChar = charContent[i];

            if (!currentCharInsideField) { //outside field
                curatedContent.append(currentChar);
                if (currentChar == quote) currentCharInsideField = true;

            } else { //inside field
                if (currentChar != '\n' && currentChar != '\r') curatedContent.append(currentChar);
                if (currentChar == quote) currentCharInsideField = false;
            }
        }
        return curatedContent.toString();
    }

    //see https://mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
    public static List<String> parseLine(String cvsLine) {
        return parseLine(cvsLine, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, char separators) {
        return parseLine(cvsLine, separators, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, char separators, char quoteMark) {

        List<String> result = new ArrayList<>();

        //if empty, return!
        if (cvsLine == null && cvsLine.isEmpty()) {
            return result;
        }

        if (quoteMark == ' ') {
            quoteMark = DEFAULT_QUOTE;
        }

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;

        char[] chars = cvsLine.toCharArray();

        for (char ch : chars) {

            if (inQuotes) {
                startCollectChar = true;
                if (ch == quoteMark) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {

                    //Fixed : allow "" in custom quote enclosed
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }

                }
            } else {
                if (ch == quoteMark) {

                    inQuotes = true;

                    //Fixed : allow "" in empty quote enclosed
                    if (chars[0] != '"' && quoteMark == '\"') {
                        curVal.append('"');
                    }

                    //double quotes in column will hit this!
                    if (startCollectChar) {
                        curVal.append('"');
                    }

                } else if (ch == separators) {

                    result.add(curVal.toString());

                    curVal = new StringBuffer();
                    startCollectChar = false;

                } else if (ch == '\r') {
                    //ignore LF characters
                    continue;
                } else if (ch == '\n') {
                    //the end, break!
                    break;
                } else {
                    curVal.append(ch);
                }
            }

        }

        result.add(curVal.toString());

        return result;
    }

}