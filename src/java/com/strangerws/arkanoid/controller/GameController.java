package com.strangerws.arkanoid.controller;

import com.strangerws.arkanoid.model.Ball;
import com.strangerws.arkanoid.model.Brick;
import com.strangerws.arkanoid.model.Counter;
import com.strangerws.arkanoid.model.Plane;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;

public class GameController {

    private Ball ball;
    private int lives = 3;
    private Counter counter;
    private Brick[][] bricks;
    private Plane plane;
    private Pane pane;
    private boolean isPlaying;

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public GameController(Ball ball, Counter counter, Brick[][] bricks, Plane plane, Pane pane) {
        this.ball = ball;
        this.counter = counter;
        this.bricks = bricks;
        this.plane = plane;
        this.pane = pane;
        animateBall();
        isPlaying = false;
    }

    private void animateBall() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isPlaying) {
                    if (!ball.isLost()) {
                        ball.move();
                        checkReflections();
                    }
                }
            }
        }.start();
    }


    private void checkReflections() {

        if (ball.intersects(0, -2, pane.getWidth(), 2)) {
            horizontalReflection(ball);
        }
        if (ball.intersects(pane.getWidth(), 0, 2, pane.getHeight())) {
            verticalReflection(ball);
        }
        if (ball.intersects(-2, 0, 2, pane.getHeight())) {
            verticalReflection(ball);
        }
        if (ball.intersects(plane.getLayoutBounds())) {
            horizontalReflection(ball);
        }

        for (Brick[] b : bricks)
            for (Brick brick : b) {
                if (brick != null) {
                    if (ball.intersects(brick.getX(), brick.getY() + brick.getHeight(), brick.getWidth(), 1)) {
                        horizontalReflection(ball);
                        brick.decreaseHealth();
                    } else if (ball.intersects(brick.getX(), brick.getY(), 1, brick.getHeight()) || ball.intersects(brick.getX() + brick.getWidth(), brick.getY(), 1, brick.getHeight())) {
                        verticalReflection(ball);
                        brick.decreaseHealth();
                    }
                    if (brick.getBrickHealth() <= 0) {
                        pane.getChildren().remove(brick);
                    }
                }
            }
    }


    private void horizontalReflection(Ball ball) {
        ball.setAngle(-ball.getAngle());
    }

    private void verticalReflection(Ball ball) {
        double angle = (ball.getAngle() < 0) ? Math.PI - ball.getAngle() : -Math.PI - ball.getAngle();
        ball.setAngle(angle);
    }

}
