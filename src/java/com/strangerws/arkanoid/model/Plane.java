package com.strangerws.arkanoid.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Plane extends Polygon {

    private double width;
    private double height;

    public Plane(double offsetX, double offsetY, double width, double height) {
        super(0, height, width, height, width - width / 10, 0, width / 10, 0);
        this.width = width;
        this.height = height;
        setLayoutX(offsetX);
        setLayoutY(offsetY);
        setFill(Color.GREEN);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void movePlane(double offsetX, double windowWidth) {
        if (getLayoutX() < 0) {
            setLayoutX(1);
        } else if (getLayoutX() > windowWidth - getWidth()) {
            setLayoutX(windowWidth - getWidth() - 1);
        } else {
            setLayoutX(offsetX);
        }
    }
}
