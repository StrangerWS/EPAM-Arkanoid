package com.strangerws.arkanoid.model;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Plane extends Polygon {

    private double[] xPoints = {0, 50, 45, 5};
    private double[] yPoints = {8, 8, 0, 0};

    public Plane(double offsetX, double offsetY) {
        super(0, 8, 50, 8, 45, 0, 5, 0);
        setLayoutX(offsetX);
        setLayoutY(offsetY);
        for (int i = 0; i < xPoints.length; i++) {
            xPoints[i] += offsetX;
            yPoints[i] += offsetY;
        }
        setFill(Color.GREEN);
        setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                setLayoutX(event.getX());
                for (int i = 0; i < xPoints.length; i++) {
                    xPoints[i] += getLayoutX();
                    yPoints[i] += getLayoutY();
                }
            }
        });
    }

    public double[] getxPoints() {
        return xPoints;
    }

    public double[] getyPoints() {
        return yPoints;
    }

}
