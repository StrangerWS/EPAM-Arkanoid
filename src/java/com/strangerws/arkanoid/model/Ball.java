package com.strangerws.arkanoid.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;

public class Ball extends Circle implements Runnable {

    private double angle;
    private double speed;
    private double worldWidth;
    private double worldHeight;
    private boolean isFrozen;
    private boolean isAiming;

    private List<List<Brick>> bricks;
    private Plane plane;

    private double hitboxX;
    private double hitboxY;

    private double width;
    private double height;

    public double getSpeed() {
        return speed;
    }

    public double getHitboxX() {
        return hitboxX;
    }

    public double getHitboxY() {
        return hitboxY;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

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

    public boolean isAiming() {
        return isAiming;
    }

    public void setAiming(boolean aiming) {
        isAiming = aiming;
    }

    public Ball(double x, double y, List<List<Brick>> bricks, Plane plane, double speed, double angle, double worldWidth, double worldHeight) {
        super(x, y, 4, Color.RED);
        this.hitboxX = x - getRadius();
        this.hitboxY = y - getRadius();

        this.plane = plane;
        this.bricks = bricks;

        this.width = getRadius() * 2;
        this.height = getRadius() * 2;
        this.speed = speed;
        if (angle == 90) {
            angle++;
        }
        this.angle = Math.toRadians(angle);
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.isFrozen = true;
        this.isAiming = true;
    }


    public void move() {
        if (!isFrozen && !isAiming) {
            setCenterX(getCenterX() + speed * Math.cos(angle));
            setCenterY(getCenterY() + speed * Math.sin(angle));
            this.hitboxX = getCenterX() - getRadius();
            this.hitboxY = getCenterY() - getRadius();
        }
    }

    public boolean isLost() {
        return getCenterY() + getRadius() >= worldHeight;
    }

    public void moveWithPlane(double planeX) {
        setCenterX(planeX);
        hitboxX = getCenterX() - getRadius();
    }

    private boolean intersectUpDown(Brick brick) {
        if (getBoundsInLocal().intersects(brick.getX(), brick.getY() + brick.getHeight() - 2, brick.getWidth(), 2)) {
            horizontalReflection();
            System.out.println(Thread.currentThread().getName() + " reflected from " + brick.getX() + " " + brick.getY());
            return true;
        }
        if (getBoundsInLocal().intersects(brick.getX(), brick.getY(), brick.getWidth(), 2)) {
            horizontalReflection();
            System.out.println(Thread.currentThread().getName() + " reflected from " + brick.getX() + " " + brick.getY());
            return true;
        }
        return false;
    }

    private boolean intersectLeftRight(Brick brick) {
        if (getBoundsInLocal().intersects(brick.getX(), brick.getY(), 2, brick.getHeight())) {
            System.out.println(Thread.currentThread().getName() + " reflected from " + brick.getX() + " " + brick.getY());
            verticalReflection();
            return true;
        }
        if (getBoundsInLocal().intersects(brick.getX() + brick.getWidth() - 2, brick.getY(), 2, brick.getHeight())) {
            verticalReflection();
            System.out.println(Thread.currentThread().getName() + " reflected from " + brick.getX() + " " + brick.getY());
            return true;
        }
        return false;
    }

    private void horizontalReflection() {
        setAngle(-getAngle());
        System.out.println(angle);
    }

    private void verticalReflection() {
        double angle = (getAngle() < 0) ? Math.PI - getAngle() : -Math.PI - getAngle();
        System.out.println(angle);
        setAngle(angle);
    }

    private void checkBorderReflections() {
        if (getBoundsInLocal().intersects(0, -2, worldWidth, 2)) {
            System.out.println(Thread.currentThread().getName() + " reflected from ceiling");
            horizontalReflection();
        }
        if (getBoundsInLocal().intersects(worldWidth, 0, 2, worldHeight)) {
            System.out.println(Thread.currentThread().getName() + " reflected from right wall");
            verticalReflection();
        }
        if (getBoundsInLocal().intersects(-2, 0, 2, worldHeight)) {
            System.out.println(Thread.currentThread().getName() + " reflected from left wall");
            verticalReflection();
        }
        if (getBoundsInLocal().intersects(plane.getLayoutBounds())) {
            System.out.println(Thread.currentThread().getName() + " reflected from plane");
            horizontalReflection();
        }
    }

    private void checkBrickReflections() {
        for (List<Brick> b : bricks) {
            for (Brick brick : b) {
                // this code must be in controller, but it is here because ball needs to reflect itself in its own thread
                if (intersectLeftRight(brick) || intersectUpDown(brick) && !brick.isIndestructible()) {
                    brick.decreaseHealth();
                }
            }
        }
    }

    @Override
    public void run() {
        do {
            if (!isLost() || !isFrozen) {
                move();
                checkBorderReflections();
                checkBrickReflections();
            } else return;
            try {
                Thread.sleep((long) speed * 10);
            } catch (InterruptedException e) {
                return;
            }
        }
        while (true);
    }
}
