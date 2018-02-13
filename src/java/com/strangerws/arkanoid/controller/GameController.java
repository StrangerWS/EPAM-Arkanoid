package com.strangerws.arkanoid.controller;

import com.strangerws.arkanoid.model.Ball;
import com.strangerws.arkanoid.model.Brick;
import com.strangerws.arkanoid.model.Plane;
import com.strangerws.arkanoid.reader.Reader;
import com.strangerws.arkanoid.util.BrickType;
import com.strangerws.arkanoid.util.Counter;

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
    private Thread counterThread;

    private List<Ball> balls;
    private Thread[] ballThreads;
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

    public List<Ball> getBalls() {
        return balls;
    }

    public void setBalls(List<Ball> balls) {
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
        setAngleBounds(angleBoundMin, angleBoundMax);

        ballSpawnX = worldWidth / 2;
        ballSpawnY = worldHeight - 60;

        generateCounter();
        generateBalls(speed);
        generateBricks();
        generatePlane();

        isPlaying = false;
    }

    private void setAngleBounds(double angleBoundMin, double angleBoundMax) {
        if (angleBoundMax >= angleBoundMin) {
            this.angleBoundMin = angleBoundMin;
            this.angleBoundMax = angleBoundMax;
        } else if (angleBoundMax < angleBoundMin) {
            this.angleBoundMin = angleBoundMax;
            this.angleBoundMax = angleBoundMin;
        }
    }

    private void generateCounter() {
        Thread counterThread = new Thread(new Counter());
        counterThread.setDaemon(true);
        counterThread.start();
    }

    private void generateBalls(int speed) {
        balls = new CopyOnWriteArrayList<>();
        double angle = angleBoundMin;
        for (int i = 0; i < lives; i++) {
            if (angleBoundMax != angleBoundMin)
                angle = ThreadLocalRandom.current().nextDouble(angleBoundMin, angleBoundMax);
            balls.add(new Ball(ballSpawnX, ballSpawnY, speed, angle, worldWidth, worldHeight));
        }
        ballThreads = new Thread[lives];
        for (int i = 0; i < lives; i++) {
            ballThreads[i] = new Thread(balls.get(i));
            ballThreads[i].setDaemon(true);
            ballThreads[i].start();
        }
    }

    private void generateBricks() {
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
    }

    private void generatePlane() {
        plane = new Plane(ballSpawnX - 25, ballSpawnY + 5, 50, 8);
    }

    private void checkReflections() {
        if (maxScore == score) gameOver = true;
        gameOverMessage = "You Win! All bricks are destroyed!";
        for (Ball ball : balls) {
            ball.checkBorderReflections(plane);

            for (List<Brick> b : bricks)
                for (Brick brick : b) {
                    if (ball.checkBrickReflections(brick) && !brick.isIndestructible()) {
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

    @Override
    public void run() {
        while (!gameOver) {
            if (isPlaying) {
                for (int i = 0; i < balls.size(); i++) {
                    if (ballThreads[i].isInterrupted()) {
                        ballThreads[i].start();
                    }
                }
                checkReflections();
                try {
                    Thread.sleep((long) (balls.get(0).getSpeed() * 10));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                for (Thread ballThread : ballThreads) {
                    if (!ballThread.isInterrupted()) ballThread.interrupt();
                }
                counterThread.interrupt();
            }
        }
    }
}