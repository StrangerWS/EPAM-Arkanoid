package com.strangerws.arkanoid.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Ball extends Circle implements Runnable {

    private double angle;
    private double speed;
    private double worldHeight;
    private boolean isFrozen;

    public Ball(double x, double y, double speed, double angle, double worldHeight) {
        super(x, y, 4, Color.RED);
        this.speed = speed;
        this.angle = angle * Math.PI;
        this.worldHeight = worldHeight;
        this.isFrozen = true;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    @Override
    public void run() {
        do {
            if (!isLost()) {
                move();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else return;
        } while (true);
    }

    public void move() {
        if (!isFrozen) {
            setCenterX(getCenterX() + speed * Math.cos(angle));
            setCenterY(getCenterY() + speed * Math.sin(angle));
        }
    }

    public void start() {
        setAngle(angle);
        isFrozen = false;
    }

    public boolean isLost() {
        if (getCenterY() + getRadius() >= worldHeight) {
            return true;
        }
        return false;
    }

    public void moveWithPlane(double planeX) {
        setCenterX(planeX);
    }

    public boolean isFrozen() {
        return isFrozen;
    }

}
