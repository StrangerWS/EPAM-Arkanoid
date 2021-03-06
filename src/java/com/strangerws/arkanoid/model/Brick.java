package com.strangerws.arkanoid.model;

import com.strangerws.arkanoid.util.BrickType;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Brick extends Rectangle {

    public static final int BRICK_WIDTH = 32;
    public static final int BRICK_HEIGHT = 16;

    private int brickHealth;
    private boolean isIndestructible;
    private int points;

    public Brick(double x, double y, BrickType type) {
        super(x, y, BRICK_WIDTH, BRICK_HEIGHT);
        brickHealth = type.brickHealth;
        setStroke(Color.BLACK);
        setFill(type.color);
        isIndestructible = type.isIndestructible;
        points = type.points;
    }

    public int getBrickHealth() {
        return brickHealth;
    }

    public boolean isIndestructible() {
        return isIndestructible;
    }

    public int getPoints() {
        return points;
    }

    public void decreaseHealth() {
        if (brickHealth > 0) {
            brickHealth--;
        }
    }
}
