package com.strangerws.arkanoid.controller;

import com.strangerws.arkanoid.model.Ball;
import com.strangerws.arkanoid.model.Brick;
import com.strangerws.arkanoid.model.Plane;
import com.strangerws.arkanoid.reader.Reader;
import com.strangerws.arkanoid.util.BrickType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GameController implements Runnable {


    private int lives;
    private int score;
    private int maxScore;
    private boolean isPlaying;
    private boolean gameOver;

    private Ball[] balls;
    private List<List<Brick>> bricks;
    private Plane plane;

    private double angleBoundMin;
    private double angleBoundMax;
    private double worldWidth;
    private double worldHeight;
    private double ballSpawnX;
    private double ballSpawnY;

    private String gameOverMessage;


    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public String getGameOverMessage() {
        return gameOverMessage;
    }

    public void setGameOverMessage(String gameOverMessage) {
        this.gameOverMessage = gameOverMessage;
    }

    public Ball[] getBalls() {
        return balls;
    }

    public void setBalls(Ball[] balls) {
        this.balls = balls;
    }

    public List<List<Brick>> getBricks() {
        return bricks;
    }

    public void setBricks(List<List<Brick>> bricks) {
        this.bricks = bricks;
    }

    public Plane getPlane() {
        return plane;
    }

    public void setPlane(Plane plane) {
        this.plane = plane;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public double getWorldWidth() {
        return worldWidth;
    }

    public void setWorldWidth(double worldWidth) {
        this.worldWidth = worldWidth;
    }

    public double getWorldHeight() {
        return worldHeight;
    }

    public void setWorldHeight(double worldHeight) {
        this.worldHeight = worldHeight;
    }

    GameController(double worldWidth, double worldHeight, double angleBoundMin, double angleBoundMax, int lives, int speed) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.lives = lives;
        if (angleBoundMax >= angleBoundMin) {
            this.angleBoundMin = angleBoundMin;
            this.angleBoundMax = angleBoundMax;
        } else if (angleBoundMax < angleBoundMin) {
            this.angleBoundMin = angleBoundMax;
            this.angleBoundMax = angleBoundMin;
        }
        ballSpawnX = worldWidth / 2;
        ballSpawnY = worldHeight - 60;

        double angle = angleBoundMin;
        if (angleBoundMax != angleBoundMin)
            angle = ThreadLocalRandom.current().nextDouble(angleBoundMin, angleBoundMax);
        for (int i = 0; i < lives; i++) {
            balls[i] = new Ball(ballSpawnX, ballSpawnY, speed, angle, worldHeight);
        }
        bricks = new CopyOnWriteArrayList<>();
        int[][] mask;
        try {
            mask = new Reader().readBrickArray();
            for (int i = 0; i < mask.length; i++) {
                bricks.add(new CopyOnWriteArrayList<>());
                for (int j = 0; j < mask[i].length; j++) {
                    if (mask[j][i] > 0) {
                        bricks.get(i).add(new Brick(i * Brick.BRICK_WIDTH, j * Brick.BRICK_HEIGHT, BrickType.values()[mask[j][i] - 1]));
                        maxScore += BrickType.values()[mask[j][i] - 1].points;
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        plane = new Plane(ballSpawnX - 25, ballSpawnY + 5, 50, 8);

        isPlaying = false;
    }

    void newTurn() {
        decreaseLives();
        for (int i = 0; i < lives; i++) {
            balls[i].setCenterX(ballSpawnX);
            balls[i].setCenterY(ballSpawnY);
            plane.setX(ballSpawnX - 25);
            plane.setY(ballSpawnY + 5);
            balls[i].setFrozen(true);
            if (angleBoundMax != angleBoundMin) {
                balls[i].setAngle(ThreadLocalRandom.current().nextDouble(angleBoundMin, angleBoundMax));
            } else {
                balls[i].setAngle(angleBoundMin);
            }
        }
    }

    synchronized void checkReflections() {
        if (maxScore == score) gameOver = true;
        gameOverMessage = "You Win! All bricks are destroyed!";
        for (int i = 0; i < lives; i++) {

            if (balls[i].getBoundsInLocal().intersects(0, -2, worldWidth, 2)) {
                horizontalReflection(balls[i]);
            }
            if (balls[i].getBoundsInLocal().intersects(worldWidth, 0, 2, worldHeight)) {
                verticalReflection(balls[i]);
            }
            if (balls[i].getBoundsInLocal().intersects(-2, 0, 2, worldHeight)) {
                verticalReflection(balls[i]);
            }
            if (balls[i].getBoundsInLocal().intersects(plane.getLayoutBounds())) {
                horizontalReflection(balls[i]);
            }

            for (List<Brick> b : bricks)
                for (Brick brick : b) {

                    if ((intersectUpDown(balls[i], brick) || intersectLeftRight(balls[i], brick)) && !brick.isIndestructible()) {
                        brick.decreaseHealth();
                    }
                    if (!brick.isIndestructible() && brick.getBrickHealth() <= 0) {
                        brick.setVisible(false);
                        b.remove(brick);
                        score += brick.getPoints();
                    }
                }
        }
    }

    private boolean intersectUpDown(Ball ball, Brick brick) {
        if (ball.getBoundsInLocal().intersects(brick.getX(), brick.getY() + brick.getHeight() - 2, brick.getWidth(), 2)) {
            horizontalReflection(ball);
            return true;
        }
        if (ball.getBoundsInLocal().intersects(brick.getX(), brick.getY(), brick.getWidth(), 2)) {
            horizontalReflection(ball);
            return true;
        }
        return false;
    }

    private boolean intersectLeftRight(Ball ball, Brick brick) {
        if (ball.getBoundsInLocal().intersects(brick.getX(), brick.getY(), 2, brick.getHeight())) {
            verticalReflection(ball);
            return true;
        }
        if (ball.getBoundsInLocal().intersects(brick.getX() + brick.getWidth() - 2, brick.getY(), 2, brick.getHeight())) {
            verticalReflection(ball);
            return true;
        }
        return false;
    }

    private void horizontalReflection(Ball ball) {
        ball.setAngle(-ball.getAngle());
    }

    private void verticalReflection(Ball ball) {
        double angle = (ball.getAngle() < 0) ? Math.PI - ball.getAngle() : -Math.PI - ball.getAngle();
        ball.setAngle(angle);
    }

    public void decreaseLives() {
        if (lives > 1) {
            lives--;
        } else {
            gameOverMessage = "You Lose! All balls are lost!";
            gameOver = true;
        }
    }

    @Override
    public void run() {
        Thread[] ballThreads = new Thread[lives];
        for (int i = 0; i < lives; i++) {

        }
        if (isPlaying) {
            checkReflections();
        }
    }
}
