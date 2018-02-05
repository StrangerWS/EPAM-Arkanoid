package com.strangerws.arkanoid.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Plane extends Rectangle {

    public Plane(double x, double y, double width, double height) {
        super(x, y, width, height);
        setFill(Color.GREEN);
        setStroke(Color.BLACK);
    }

    public void movePlane(double offsetX, double windowWidth) {
        if (getX() < 0) {
            setX(1);
        } else if (getX() > windowWidth - getWidth()) {
            setX(windowWidth - getWidth() - 1);
        } else {
            setX(offsetX);
        }
    }
}
