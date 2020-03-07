package com.edenrump.comms;

import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import java.util.HashMap;
import java.util.Map;


/**
 * Utility class providing methods to send content to the system clipboard
 */

public class Clipper {

    /**
     * Utility method sends object content of specified format to the system clipboard
     *
     * @param format  the format of the content
     * @param content the content
     */

    public static void pushToClipboard(DataFormat format, Object content) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        Map<DataFormat, Object> dataFormatObjectMap = new HashMap<>();
        dataFormatObjectMap.put(format, content);
        clipboard.setContent(dataFormatObjectMap);
    }

}