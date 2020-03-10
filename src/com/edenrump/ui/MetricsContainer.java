package com.edenrump.ui;

import com.edenrump.models.data.MetricBlock;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class MetricsContainer extends VBox {

    private String initialMessage;

    public MetricsContainer(String initialMessage) {
        this.initialMessage = initialMessage;

        setContainerStyle();
        resetContentToInitialMessage();
    }

    private void resetContentToInitialMessage() {
        getChildren().clear();
        getChildren().add(createStyledLabel(initialMessage));
    }

    private void setContainerStyle() {
        getStyleClass().add("content-pane");
    }

    private Label createStyledLabel(String message) {
        return createStyledLabel(message, 1);
    }

    private Label createStyledLabel(String message, double opacity) {
        Label label = new Label(message);
        label.setOpacity(opacity);
        label.getStyleClass().add("title-text");
        return label;
    }

    public void displayMetrics(List<MetricBlock> metricBlocks) {
        getChildren().clear();

        double perBlockDelay = 500;
        for (MetricBlock block : metricBlocks) {
            addMetricBlock(block, perBlockDelay * metricBlocks.indexOf(block));
        }
    }

    private void addMetricBlock(MetricBlock block, double animationDelay) {
        VBox blockContainer = new VBox();
        blockContainer.setAlignment(Pos.CENTER);
        blockContainer.setSpacing(10);

        Label metricTitleLabel = createStyledLabel(block.getTitle(), 0);
        blockContainer.getChildren().add(metricTitleLabel);

        List<Pair<Label, Label>> metricKeyValuePairLabels = new ArrayList<>();
        for (String title : block.getMetricKeys()) {
            Label key = createStyledLabel(title, 0);
            key.setPrefWidth(300);
            Label value = createStyledLabel(block.getMetricValue(title), 0);
            value.setPrefWidth(50);
            HBox container = new HBox(key, value);
            container.setAlignment(Pos.CENTER);
            blockContainer.getChildren().add(container);
            metricKeyValuePairLabels.add(new Pair<>(key, value));
        }

        getChildren().add(blockContainer);
        createFadeInAnimation(metricTitleLabel, metricKeyValuePairLabels, animationDelay).play();
    }

    private Timeline createFadeInAnimation(Label metricTitleLabel, List<Pair<Label, Label>> metricKeyValuePairLabels, double animationDelay) {
        double timelineLengthMillis = 200;
        double timelineDelayMillis = 90;
        Timeline fadeIn = new Timeline();
        fadeIn.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(animationDelay),
                        new KeyValue(metricTitleLabel.opacityProperty(), 0)),
                new KeyFrame((Duration.millis(timelineLengthMillis + animationDelay)),
                        new KeyValue(metricTitleLabel.opacityProperty(), 1))
        );
        for (Pair<Label, Label> metricKVPair : metricKeyValuePairLabels) {
            double delayForThis = animationDelay + (metricKeyValuePairLabels.indexOf(metricKVPair) + 1) * timelineDelayMillis;
            Label title = metricKVPair.getKey();
            Label value = metricKVPair.getValue();
            fadeIn.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(delayForThis),
                            new KeyValue(title.opacityProperty(), 0)),
                    new KeyFrame((Duration.millis(delayForThis + timelineLengthMillis)),
                            new KeyValue(title.opacityProperty(), 1)),
                    new KeyFrame(Duration.millis(delayForThis),
                            new KeyValue(value.opacityProperty(), 0)),
                    new KeyFrame((Duration.millis(delayForThis + timelineLengthMillis)),
                            new KeyValue(value.opacityProperty(), 1))
            );
        }
        return fadeIn;
    }


}
