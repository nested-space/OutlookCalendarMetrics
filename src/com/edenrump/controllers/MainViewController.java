package com.edenrump.controllers;

import com.edenrump.config.Defaults;
import com.edenrump.loaders.CSVUtils;
import com.edenrump.models.data.Table;
import com.edenrump.models.data.TableRow;
import com.edenrump.models.time.CalendarEvent;
import com.edenrump.util.CalendarUtils;
import javafx.beans.property.IntegerProperty;
import javafx.fxml.Initializable;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

    public VBox dragTarget;

    private static final int START = 1;
    private static final int LOADED = 2;
    private static final int READY = 3;
    private static final int COMPLETE = 4;
    private static final int ERROR = 5;
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

    public void loadFile(File file){
        Table loadedData = CSVUtils.loadCSV(file);
        System.out.println("Data loaded");

        //TODO: validate table entries with required headers

        List<CalendarEvent> calendarEventList = new ArrayList<>();
        for(TableRow row: loadedData.getRows()){
            System.out.println("Appointment: " + row.getEntry("Subject"));
            CalendarEvent calendarEvent = CalendarUtils.coerceRowToEvent(row);
            System.out.println("Appointment length: " + calendarEvent.getDuration().toHours() + " hours");
        }
        //TODO: calculate metrics
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
}
