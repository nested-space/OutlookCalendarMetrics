package com.edenrump.comms;


import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Utility class providing functionality for launching links
 */
public class Launcher {

    /**
     * Utility method providing functionality to launch a link that's already been formatted into a URL
     *
     * @param formattedURL the url to be launched
     */
    public static void handleOpenHyperlink(String formattedURL) {
        new Application() {
            @Override
            public void start(Stage stage) throws Exception {

            }
        }.getHostServices().showDocument(formattedURL);
    }

}
