package com.edenrump.controllers;

import com.edenrump.comms.Launcher;
import com.edenrump.config.Defaults;
import com.edenrump.loaders.CSVUtils;
import com.edenrump.models.data.Table;
import com.edenrump.models.time.Calendar;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainViewController implements Initializable {

    private static final int START = 1;
    private static final int LOADED = 2;
    private static final int READY = 3;
    private static final int COMPLETE = 4;
    private static final int ERROR = 5;
    private IntegerProperty applicationState = new SimpleIntegerProperty(START);

    Point2D fileDropMouseLocation = new Point2D(250, 400);

    @FXML
    private Button fileButton;

    @FXML
    private VBox dragTarget;

    @FXML
    private VBox contentPane;

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

    /**
     * Method responsible for handling active drag-over events in the dragTarget
     *
     * @param dragEvent the drag event being handled
     */
    private void handleDragOver(DragEvent dragEvent) {
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
    private void handleOnDragDropped(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            dragTarget.getStyleClass().remove(Defaults.DRAG_ALLOWED);
            if (db.getFiles().size() == 1) {
                setFileDropLocation(dragEvent.getX(), dragEvent.getY());
                createAndStartValidatingAnimation();

                //TODO: validate file pre-loading
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
    private void handleDragExit(DragEvent dragEvent) {
        dragTarget.getStyleClass().remove(Defaults.DRAG_ALLOWED);
    }

    /**
     * Method responsible for handling active drag-enter events in the dragTarget
     *
     * @param dragEvent the drag event being handled
     */
    private void handleDragEntered(DragEvent dragEvent) {
        dragTarget.getStyleClass().add(Defaults.DRAG_ALLOWED);
    }

    private void createAndStartValidatingAnimation() {
        double focusPercentX = fileDropMouseLocation.getX() / 780 * 100;
        double focusPercentY = fileDropMouseLocation.getY() / 480 * 100;
        DoubleProperty timelinePosition = new SimpleDoubleProperty(0);
        timelinePosition.addListener((obs, o, n) -> {
            contentPane.setStyle(createRadialGradientStyleString(
                    focusPercentX, focusPercentY, n.doubleValue(), "#ffffff22", "#38ef7dcc"));
        });
        Timeline loadingAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(timelinePosition, 0)),
                new KeyFrame(Duration.millis(1000), new KeyValue(timelinePosition, 800))
        );
        loadingAnimation.play();
    }

    private String createRadialGradientStyleString(double focusPercentX, double focusPercentY, double radius, String color1, String color2) {
        DecimalFormat zeroDP = new DecimalFormat("##");
        return "-fx-background-color: radial-gradient(center " + zeroDP.format(focusPercentX) + "% " +
                zeroDP.format(focusPercentY) + "%, radius " +
                zeroDP.format(radius) + "%, " +
                color1 + ", " +
                color2 + ");";
    }

    private void setFileDropLocation(double x, double y) {
        fileDropMouseLocation = new Point2D(x, y);
    }

    @FXML
    private void handleContentPaneClicked(MouseEvent event) {
        setFileDropLocation(event.getX(), event.getY());
        createAndStartValidatingAnimation();
        event.consume();
    }

    @FXML
    private void handleOpenFileButtonClicked(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Comma Separated Values", "*.csv"));
        fileChooser.setTitle("Open Calendar Export File");
        fileChooser.setInitialDirectory(new File("C:/Users/" + System.getProperty("user.name") + "/Desktop"));
        File toOpen = fileChooser.showOpenDialog(fileButton.getScene().getWindow());
        if (toOpen != null) {
            loadFile(toOpen);
        }
        event.consume();
    }

    @FXML
    private void handleExitButtonClicked(ActionEvent event) {
        exit();
        event.consume();
    }

    @FXML
    private void handleLaunchGithubWebsiteButtonClicked(ActionEvent event) {
        Launcher.handleOpenHyperlink("https://github.com/nested-space/OutlookCalendarMetrics");
        event.consume();
    }

    @FXML
    private void handleSaveImageButtonClicked(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Portable Network Graphic (PNG)", "*.png"));
        fileChooser.setTitle("Save Window as Image");
        fileChooser.setInitialDirectory(new File("C:/Users/" + System.getProperty("user.name") + "/Desktop"));
        File toSave = fileChooser.showSaveDialog(fileButton.getScene().getWindow());
        if (toSave != null) {
            saveImage(dragTarget.snapshot(new SnapshotParameters(), null), toSave);
        }
    }

    private void exit() {
        Platform.exit();
    }

    private void loadFile(File file) {
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

    private void saveImage(WritableImage snapshot, File file) {
        BufferedImage image = javafx.embed.swing.SwingFXUtils.fromFXImage(snapshot, null);
        try {
            Graphics2D gd = (Graphics2D) image.getGraphics();
            gd.translate(dragTarget.getWidth(), dragTarget.getHeight());
            ImageIO.write(image, "png", file);
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
