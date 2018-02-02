package com.strangerws.arkanoid.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Ball extends Circle implements Runnable {

    private double velocityX;
    private double velocityY;
    private double worldWidth;
    private double worldHeight;

    public Ball(double x, double y, double velocityX, double velocityY, double worldWidth, double worldHeight) {
        super(x, y, 4, Color.RED);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
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

    private void move() {
        setCenterX(getCenterX() + velocityX);
        setCenterY(getCenterY() + velocityY);
    }

    public boolean isLost() {
        if (getCenterY() + getRadius() >= worldHeight) {
            System.out.println("ball is lost");
            return true;
        }
        return false;
    }


}
