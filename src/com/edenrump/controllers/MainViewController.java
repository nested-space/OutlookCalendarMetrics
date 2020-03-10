package com.edenrump.controllers;

import com.edenrump.comms.Launcher;
import com.edenrump.config.Defaults;
import com.edenrump.models.MetricsCalculator;
import com.edenrump.ui.MetricsContainer;
import com.edenrump.ui.ToastContainer;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainViewController implements Initializable {

    private IntegerProperty applicationState = new SimpleIntegerProperty(Defaults.START);

    @FXML
    private StackPane contentContainer;

    @FXML
    private Button fileButton;

    @FXML
    private VBox dragTarget;

    private MetricsCalculator metricsCalculator;

    private MetricsContainer metricsContainer;

    private ToastContainer toastContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addMetricsContainer();
        addToastContainer();
        setMetricsCalculator(metricsContainer, toastContainer);
        enableDragboardFileLoad();
    }

    private void setMetricsCalculator(MetricsContainer metricsContainer, ToastContainer toastContainer) {
        metricsCalculator = new MetricsCalculator(metricsContainer, toastContainer, applicationState);
    }

    private void addMetricsContainer() {
        metricsContainer = new MetricsContainer("Drop Calendar Export CSV to Load Metrics...");
        contentContainer.getChildren().add(metricsContainer);
    }

    private void addToastContainer() {
        toastContainer = new ToastContainer();
        contentContainer.getChildren().add(toastContainer);
    }

    private void enableDragboardFileLoad() {
        dragTarget.setOnDragDropped(this::handleOnDragDropped);
        dragTarget.setOnDragEntered(this::handleDragEntered);
        dragTarget.setOnDragExited(this::handleDragExit);
        dragTarget.setOnDragOver(this::handleDragOver);
    }

    private void handleDragOver(DragEvent dragEvent) {
        if (dragEvent.getGestureSource() != dragTarget
                && dragEvent.getDragboard().hasFiles()) {
            dragEvent.acceptTransferModes(TransferMode.COPY);
        }
        dragEvent.consume();
    }

    private void handleOnDragDropped(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        dragTarget.getStyleClass().remove(Defaults.DRAG_ALLOWED);
        boolean success = metricsCalculator.loadMetricsFromDragBoard(db, dragEvent);
        dragEvent.setDropCompleted(success);
        dragEvent.consume();
    }

    private void handleDragExit(DragEvent dragEvent) {
        dragTarget.getStyleClass().remove(Defaults.DRAG_ALLOWED);
    }

    private void handleDragEntered(DragEvent dragEvent) {
        dragTarget.getStyleClass().add(Defaults.DRAG_ALLOWED);
    }

    @FXML
    private void handleContentPaneClicked(MouseEvent event) {
        metricsCalculator.pingValidationAnimation(event.getX(), event.getY());
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
            metricsCalculator.loadMetricsFromFile(toOpen);
        }
        event.consume();
    }

    @FXML
    private void handleExitButtonClicked(ActionEvent event) {
        Platform.exit();
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
        event.consume();
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
