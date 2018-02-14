package com.strangerws.arkanoid.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Plane extends Rectangle {

    public static final int PLANE_WIDTH = 50;
    public static final int PLANE_HEIGHT = 8;


    public Plane(double x, double y) {
        super(x, y, PLANE_WIDTH, PLANE_HEIGHT);
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
