package com.strangerws.arkanoid.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Ball extends Circle implements Runnable{

    private double angle;
    private double speed;
    private double worldHeight;
    private boolean isFrozen;

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }


    public boolean isFrozen() {
        return isFrozen;
    }

    public void setFrozen(boolean frozen) {
        this.isFrozen = frozen;
    }
    public Ball(double x, double y, double speed, double angle, double worldHeight) {
        super(x, y, 4, Color.RED);
        this.speed = speed;
        if (angle == 90) {
            angle++;
        }
        this.angle = Math.toRadians(angle);
        this.worldHeight = worldHeight;
        this.isFrozen = true;
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


    @Override
    public void run() {
        if(!isLost()){
            move();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
