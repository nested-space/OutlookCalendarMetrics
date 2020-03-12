package com.edenrump.models;

import com.edenrump.config.Defaults;
import com.edenrump.loaders.CSVUtils;
import com.edenrump.models.data.MetricBlock;
import com.edenrump.models.data.Table;
import com.edenrump.models.time.Calendar;
import com.edenrump.ui.MetricsContainer;
import com.edenrump.ui.ToastContainer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.util.Duration;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

public class MetricsCalculator {

    private Point2D fileDropMouseLocation = new Point2D(250, 400);

    private MetricsContainer metricsContainer;
    private ToastContainer toastContainer;
    private IntegerProperty applicationStateObservable;

    public MetricsCalculator(MetricsContainer metricsContainer, ToastContainer toastContainer, IntegerProperty applicationState) {
        this.metricsContainer = metricsContainer;
        this.toastContainer = toastContainer;
        this.applicationStateObservable = applicationState;
    }

    public boolean loadMetricsFromDragBoard(Dragboard dragBoard, DragEvent dragEvent) {
        boolean success = false;
        if (dragBoard.getFiles().size() == 1) {
            loadMetricsFromFile(dragEvent.getX(), dragEvent.getY(), dragBoard.getFiles().get(0));
            success = true;
        } else {
            toastContainer.displayToast(Defaults.MULTIPLE_FILE_UNSUPPORTED);
            applicationStateObservable.set(Defaults.ERROR);
            success = false;
        }
        return success;
    }


    public void loadMetricsFromFile(File file) {
        loadMetricsFromFile(metricsContainer.getWidth() / 2, metricsContainer.getHeight() / 2, file);
    }

    public void loadMetricsFromFile(double fileDragDropLocationX, double fileDragDropLocationY, File file) {
        //set mouse location
        setFileDropLocation(fileDragDropLocationX, fileDragDropLocationY);

        //start validation animation
        createValidatingAnimation().play();

        //validate file
        Table loadedData = CSVUtils.loadCSV(file);
        Calendar calendar = new Calendar(loadedData);
        if (validateCalendarData(calendar)) {
            applicationStateObservable.set(Defaults.CALCULATING);
            displayMetrics(calendar);
        } else {
            toastContainer.displayToast("Calendar data not valid or does not contain any entries. Please load another file");
        }
    }


    private boolean validateCalendarData(Calendar calendar) {
        return calendar.getEvents().size() > 0;
    }

    private void displayMetrics(Calendar calendar) {
        List<String> projects = new ArrayList<>(calendar.getCategories());
        Collections.sort(projects);
        Set<String> exclude = new HashSet<>(Collections.singletonList("0. Personal"));
        Set<String> include = calendar.getCategories();
        include.removeAll(exclude);
        double totalCalendarBookedTime = ((double) calendar.calculateTotalBookedTime(include).toMinutes()) / 60;

        MetricBlock totalTime = new MetricBlock("------------------------ Total Time -------------------------");
        totalTime.addMetric("Total time booked: ", format2dp(totalCalendarBookedTime) + " h");
        double timeMeetings = ((double) calendar.calculateTimeInMeetings(include).toMinutes()) / 60;
        double defaultWorkingWeek = 36.5;
        totalTime.addMetric("Total time in meetings: ", format2dp(timeMeetings) + " h");
        totalTime.addMetric("Meeting % of 36.5h working week: ", format2dp(timeMeetings / defaultWorkingWeek * 100) + "%");

        MetricBlock projectTime = new MetricBlock("---------------- Time By Calendar Categories ----------------");
        for (String category : projects) {
            if(exclude.contains(category)) continue;
            Calendar categoryCalendar = calendar.extractCalendarByCategory(category);
            double bookedTime = ((double) categoryCalendar.calculateTotalBookedTime(include).toMinutes()) / 60;
            projectTime.addMetric(category + ": \t", format2dp(bookedTime) + " h");
        }

        metricsContainer.displayMetrics(Arrays.asList(totalTime, projectTime));
    }
    
    private void setFileDropLocation(double x, double y) {
        //TODO: validate that x and y are within content container's bounds
        fileDropMouseLocation = new Point2D(x, y);
    }

    public void pingValidationAnimation(double x, double y) {
        setFileDropLocation(x, y);
        createValidatingAnimation().play();
    }

    private Timeline createValidatingAnimation() {
        double focusPercentX = fileDropMouseLocation.getX() / 780 * 100;
        double focusPercentY = fileDropMouseLocation.getY() / 480 * 100;
        DoubleProperty timelinePosition = new SimpleDoubleProperty(0);
        timelinePosition.addListener((obs, o, n) -> {
            metricsContainer.setStyle(createRadialGradientStyleString(
                    focusPercentX, focusPercentY, n.doubleValue(), "#ffffff22", "#38ef7dcc"));
        });
        return new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(timelinePosition, 0)),
                new KeyFrame(Duration.millis(1000), new KeyValue(timelinePosition, 800))
        );
    }

    private String createRadialGradientStyleString(double focusPercentX, double focusPercentY, double radius, String color1, String color2) {
        DecimalFormat zeroDP = new DecimalFormat("##");
        return "-fx-background-color: radial-gradient(center " + zeroDP.format(focusPercentX) + "% " +
                zeroDP.format(focusPercentY) + "%, radius " +
                zeroDP.format(radius) + "%, " +
                color1 + ", " +
                color2 + ");";
    }

    private String format2dp(double value) {
        DecimalFormat df2 = new DecimalFormat("#.##");
        return df2.format(value);
    }
}
