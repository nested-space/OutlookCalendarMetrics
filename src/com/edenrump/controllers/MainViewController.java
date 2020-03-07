package com.edenrump.controllers;

import com.edenrump.comms.Launcher;
import com.edenrump.config.Defaults;
import com.edenrump.loaders.CSVUtils;
import com.edenrump.models.data.Table;
import com.edenrump.models.time.Calendar;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

public class MainViewController implements Initializable {

    double initialWindowX;
    double initialWindowY;
    double initialMouseX;
    double initialMouseY;
    double deltaX;
    double deltaY;
    public VBox dragTarget;

    private static final int START = 1;
    private static final int LOADED = 2;
    private static final int READY = 3;
    private static final int COMPLETE = 4;
    private static final int ERROR = 5;
    public Button fileButton;
    private IntegerProperty applicationState;

    /**
     * Method to enable the ability for the user to load a file (usually when the program is in a state
     * of converting or saving the model)
     */
    private void enableFileLoad() {
        dragTarget.setOnDragDropped(this::handleOnDragDropped);
        dragTarget.setOnDragEntered(this::handleDragEntered);
        dragTarget.setOnDragExited(this::handleDragExit);
        dragTarget.setOnDragOver(this::handleDragOver);
    }


    public void loadFile(File file) {
        Table loadedData = CSVUtils.loadCSV(file);

        //TODO: validate table entries with required headers
        Calendar calendar = new Calendar(loadedData);


        Set<String> projects = new HashSet<>(Arrays.asList());
        Set<String> include = calendar.getCategories();
        include.removeAll(Collections.singletonList("0. Personal"));

        double totalCalendarBookedTime = ((double) calendar.calculateTotalBookedTime(include).toMinutes()) / 60;
        double dedicatedProjectTime = ((double) calendar.calculateTotalBookedTime(projects).toMinutes()) / 60;
        System.out.println("\n-------------- Total Time -------------------");
        System.out.println("Total time booked: " + totalCalendarBookedTime);
        System.out.println("Dedicated project time: " + dedicatedProjectTime);

        DecimalFormat df2 = new DecimalFormat("#.##");
        Map<String, Calendar> calendarsByCategory = new HashMap<>();
        System.out.println("\n------------ Normalised Project Time ------------");
        for (String category : projects) {
            Calendar categoryCalendar = calendar.extractCalendarByCategory(category);
            double bookedTime = ((double) categoryCalendar.calculateTotalBookedTime(include).toMinutes()) / 60;
            System.out.println(category + ": \t" + df2.format(bookedTime * totalCalendarBookedTime / dedicatedProjectTime) + " h");
        }

        double timeMeetings = ((double) calendar.calculateTimeInMeetings(include).toMinutes()) / 60;
        System.out.println("\n------------------- Meetings  -------------------");
        System.out.println("Percent time in meetings: " + df2.format(timeMeetings / totalCalendarBookedTime * 100));

    }

    private void updateDisplayWithMetrics(Table loadedData) {
        System.out.println("Total booked time = ");
    }

    private void updateDisplayWithErrorMessage(String errorMessage) {

    }

    /**
     * Method responsible for handling active drag-over events in the dragTarget
     *
     * @param dragEvent the drag event being handled
     */
    public void handleDragOver(DragEvent dragEvent) {
        if (dragEvent.getGestureSource() != dragTarget
                && dragEvent.getDragboard().hasFiles()) {
            /* allow for both copying and moving, whatever user chooses */
            dragEvent.acceptTransferModes(TransferMode.COPY);
        }
        dragEvent.consume();
    }

    /**
     * Method responsible for handling drag-drop events in the dragTarget
     *
     * @param dragEvent the drag event being handled
     */
    public void handleOnDragDropped(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            dragTarget.getStyleClass().remove(Defaults.DRAG_ALLOWED);
            if (db.getFiles().size() == 1) {
                loadFile(db.getFiles().get(0));
                success = true;
            } else {
                String errorMessage = Defaults.MULTIPLE_FILE_UNSUPPORTED;
                applicationState.set(ERROR);
                success = false;
            }
        }
        /* let the source know whether the string was successfully
         * transferred and used */
        dragEvent.setDropCompleted(success);

        dragEvent.consume();
    }

    /**
     * Method responsible for handling active drag-exit events in the dragTarget
     *
     * @param dragEvent the drag event being handled
     */
    public void handleDragExit(DragEvent dragEvent) {
        dragTarget.getStyleClass().remove(Defaults.DRAG_ALLOWED);
    }

    /**
     * Method responsible for handling active drag-enter events in the dragTarget
     *
     * @param dragEvent the drag event being handled
     */
    public void handleDragEntered(DragEvent dragEvent) {
        dragTarget.getStyleClass().add(Defaults.DRAG_ALLOWED);
    }

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  <tt>null</tt> if the location is not known.
     * @param resources The resources used to localize the root object, or <tt>null</tt> if
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        enableFileLoad();
    }

    public void exit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void openFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Comma Separated Values", "*.csv"));
        fileChooser.setTitle("Open Calendar Export File");
        fileChooser.setInitialDirectory(new File("C:/Users/" + System.getProperty("user.name") + "/Desktop"));
        File toOpen = fileChooser.showOpenDialog(fileButton.getScene().getWindow());
        if (toOpen != null) {
            loadFile(toOpen);
        }
        actionEvent.consume();
    }

    public void launchGithubWebsite(ActionEvent actionEvent) {
        Launcher.handleOpenHyperlink("https://github.com/nested-space/OutlookCalendarMetrics");
    }

    private Window getWindow() {
        return dragTarget.getScene().getWindow();
    }
}
