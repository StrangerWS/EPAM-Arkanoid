package com.strangerws.arkanoid.model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Pair;

import java.util.Map;

public class Ball extends Circle implements Runnable {

    //Some static constants
    private static final int TOP_LEFT_X = 0;
    private static final int TOP_LEFT_Y = 0;
    private static final int OVERLAP = 4;
    private static final int RADIUS = 4;
    public static final int SPEED_MULTIPLIER = 100;

    //Ball properties
    private double angle;
    private double speed;
    private double hitboxX;
    private double hitboxY;
    private double width;
    private double height;
    private boolean isFrozen;
    private boolean isAiming;

    //Other objects of world
    //it`s not good to be honest, but i couldn`t see another way to use it with threads
    private Map<Pair<Integer, Integer>, Brick> bricks;
    private Plane plane;

    //Properties of a world
    private double worldWidth;
    private double worldHeight;

    //getter and setters, as usual
    //--------------------------------------------------------------
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

    private double getAngle() {
        return angle;
    }

    private void setAngle(double angle) {
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

    //--------------------------------------------------------------

    public Ball(double x, double y, Map<Pair<Integer, Integer>, Brick> bricks, Plane plane, double speed, double angle, double worldWidth, double worldHeight) {
        super(x, y, RADIUS, Color.RED);
        this.hitboxX = x - getRadius();
        this.hitboxY = y - getRadius();

        this.plane = plane;
        this.bricks = bricks;

        this.width = getRadius() * 2;
        this.height = getRadius() * 2;
        this.speed = speed;
        this.angle = Math.toRadians(angle);
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.isFrozen = true;
        this.isAiming = true;
    }


    private void move() {
        //moving actual ball and its canvas interpretation
        //while plane is released balls and game isn`t paused
        if (!isFrozen && !isAiming) {
            setCenterX(getCenterX() + speed * Math.cos(angle));
            setCenterY(getCenterY() + speed * Math.sin(angle));
            this.hitboxX = getCenterX() - getRadius();
            this.hitboxY = getCenterY() - getRadius();
        }
    }

    public boolean isLost() {
        //ball got through the plane, and we couldn`t catch it
        //this method is used to stop thread
        return getCenterY() + getRadius() >= worldHeight;
    }

    public void moveWithPlane(double planeX) {
        //changing x coordinate depending on plane
        setCenterX(planeX);
        hitboxX = getCenterX() - getRadius();
    }

    private void horizontalReflection() {
        setAngle(-getAngle());
    }

    private void verticalReflection() {
        double angle = (getAngle() < 0) ? Math.PI - getAngle() : -Math.PI - getAngle();
        setAngle(angle);
    }

    private void checkBorderReflections() {
        //Just reflections from borders of screen and plane
        if (getBoundsInLocal().intersects(TOP_LEFT_X, -OVERLAP, worldWidth, OVERLAP)) {
            horizontalReflection();
        }
        if (getBoundsInLocal().intersects(worldWidth, TOP_LEFT_Y, OVERLAP, worldHeight)) {
            verticalReflection();
        }
        if (getBoundsInLocal().intersects(-OVERLAP, TOP_LEFT_Y, OVERLAP, worldHeight)) {
            verticalReflection();
        }
        if (getBoundsInLocal().intersects(plane.getLayoutBounds())) {
            horizontalReflection();
        }
    }

    private void checkBrickReflection() {
        //To speed up calculations we are using ConcurrentHashMap
        //Map is better then previously used ArrayList
        //because we can get any brick by key - Pair of integers
        //and we don`t need to iterate by whole structure of bricks
        //like it was in previous version
        double nextX = getCenterX() + speed * Math.cos(angle);
        double nextY = getCenterY() + speed * Math.sin(angle);

        double nextVelocityX = nextX % Brick.BRICK_WIDTH;
        double nextVelocityY = nextY % Brick.BRICK_HEIGHT;

        //Getting coordinates of brick that we are intersecting in the next move
        double brickX = nextX - nextVelocityX;
        double brickY = nextY - nextVelocityY;

        Brick brick = bricks.get(new Pair<>((int) brickX, (int) brickY));

        //There are may be null brick, because we just get points of grid 32*16
        //So, if vector of a ball is in brick
        if (brick != null && brick.getBoundsInLocal().contains(nextX, nextY)) {
            //Then we are checking vector of a ball
            //If ball gets further by x then by y
            if (nextVelocityX > nextVelocityY){
                //Change it`s vector by x
                horizontalReflection();
            } else {
                //if ball gets further by y change vector by y
                verticalReflection();
            }

            if (!brick.isIndestructible()) {
                //Decreasing health of a brick if it can be broken
                brick.decreaseHealth();
            }
            //That method isn`t perfect, because sometimes
            //ball dives into the brick - this is visually bad
            //Also, this method cannot resolve some complex problems
            //like intersecting a border of two different bricks
        }
    }

    @Override
    public void run() {
        do {
            if (!isLost() || !isFrozen) {
                checkBorderReflections();
                checkBrickReflection();
                move();
            } else return;
            try {
                //Speed of a game depends on this sleep method
                //To make a delay at maximum (10) speed subtract 1
                Thread.sleep((long) (SPEED_MULTIPLIER - (speed - 1) * 10));
            } catch (InterruptedException e) {
                return;
            }
        }
        while (true);
    }
}
