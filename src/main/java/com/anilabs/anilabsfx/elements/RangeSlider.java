package com.anilabs.anilabsfx.elements;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class RangeSlider extends Pane {
    private final Rectangle track = new Rectangle();
    private final Rectangle selectedRange = new Rectangle();

    private final Circle lowThumb = new Circle(8);
    private final Circle highThumb = new Circle(8);
    private double trackWidth;

    private Integer TRACK_HEIGHT = 5;
    private Integer min = 1000;
    private Integer max = 3000;

    private final IntegerProperty lowValue = new SimpleIntegerProperty();
    private final IntegerProperty highValue = new SimpleIntegerProperty();

    public RangeSlider() {
        Color purple = Color.valueOf("#9b59b6");

        track.setHeight(TRACK_HEIGHT);
        track.setFill(Color.GRAY);
        track.setArcWidth(TRACK_HEIGHT);
        track.setArcHeight(TRACK_HEIGHT);

        selectedRange.setHeight(TRACK_HEIGHT);
        selectedRange.setFill(purple);
        selectedRange.setArcWidth(TRACK_HEIGHT);
        selectedRange.setArcHeight(TRACK_HEIGHT);

        lowThumb.setStroke(Color.WHITE);
        lowThumb.setStrokeWidth(1);
        lowThumb.setEffect(new DropShadow(2, Color.GRAY));
        lowThumb.setFill(purple);

        highThumb.setStroke(Color.WHITE);
        highThumb.setStrokeWidth(1);
        highThumb.setEffect(new DropShadow(2, Color.GRAY));
        highThumb.setFill(purple);

        getChildren().addAll(track, selectedRange, lowThumb, highThumb);

        lowValue.set(min);
        highValue.set(max);

        layoutBoundsProperty().addListener((obs, old, bounds) -> {
            trackWidth = bounds.getWidth();
            double centerY = bounds.getHeight() / 2.0;

            track.setWidth(trackWidth);
            track.setY(centerY - TRACK_HEIGHT / 2.0);
            selectedRange.setY(centerY - TRACK_HEIGHT / 2.0);

            lowThumb.setCenterY(centerY);
            highThumb.setCenterY(centerY);

            updatePositions(lowValue.get(), highValue.get());
        });



        setupDragging(lowThumb, true);
        setupDragging(highThumb, false);
    }

    public void setValues(Integer minValue, Integer maxValue) {
        min = minValue;
        max = maxValue;
        lowValue.set(min);
        highValue.set(max);
        updatePositions(lowValue.get(), highValue.get());
    }

    public Integer getMaxValue() {
        return max;
    }

    public IntegerProperty lowValueProperty() {
        return lowValue;

    }

    public IntegerProperty highValueProperty() {
        return highValue;
    }

    private void setupDragging(Circle thumb, boolean isLow) {
        thumb.setOnMouseDragged(e -> {
            double x = clamp(e.getX(), 0, trackWidth);
            int value = (int) (min + (x / trackWidth) * (max - min));
            if (isLow) {
                if (value >= getHighValue()) return;
                updatePositions(value, getHighValue());
            } else {
                if (value <= getLowValue()) return;
                updatePositions(getLowValue(), value);
            }
        });
    }

    private void updatePositions(int lowVal, int highVal) {
        double lowX = ((lowVal - min) * trackWidth) / (max - min);
        double highX = ((highVal - min) * trackWidth) / (max - min);

        lowThumb.setCenterX(lowX);
        highThumb.setCenterX(highX);

        selectedRange.setX(lowX);
        selectedRange.setWidth(highX - lowX);

        lowValue.set(lowVal);
        highValue.set(highVal);
    }

    public Integer getLowValue() {
        double ratio = lowThumb.getCenterX() / trackWidth;
        return min + (int) (ratio * (max - min));
    }

    public Integer getHighValue() {
        double ratio = highThumb.getCenterX() / trackWidth;
        return min + (int) (ratio * (max - min));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}

