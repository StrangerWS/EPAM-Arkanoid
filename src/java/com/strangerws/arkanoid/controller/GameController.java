package com.strangerws.arkanoid.controller;

import com.strangerws.arkanoid.model.Ball;
import com.strangerws.arkanoid.model.Brick;
import com.strangerws.arkanoid.model.Plane;
import com.strangerws.arkanoid.reader.Reader;
import com.strangerws.arkanoid.util.BrickType;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GameController implements Runnable {

    //Some constants and statics
    private static final String GAME_WON = "You Win! All bricks are destroyed!";
    private static final String GAME_LOST = "You Lose! All balls are lost!";
    private static final int PLANE_OFFSET = 5;

    //Game Properties
    private int lives;
    private int score;
    private int deletedBricks;
    private int maxDeletedBricks;
    private boolean isPlaying;
    private boolean gameOver;
    private double angleBoundMin;
    private double angleBoundMax;
    private String gameOverMessage;
    private final int maxLives;
    private final int speed;

    //Game Objects
    private List<Ball> balls;
    private List<Thread> ballThreads;
    private Map<Pair<Integer, Integer>, Brick> bricks;
    private Plane plane;


    //World Properties
    private double worldWidth;
    private double worldHeight;
    private double ballSpawnX;
    private double ballSpawnY;


    // Getters and Setters
    // -------------------------------------------------------------
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

    public int getDeletedBricks() {
        return deletedBricks;
    }

    public void setDeletedBricks(int deletedBricks) {
        this.deletedBricks = deletedBricks;
    }

    public int getMaxDeletedBricks() {
        return maxDeletedBricks;
    }

    public void setMaxDeletedBricks(int maxDeletedBricks) {
        this.maxDeletedBricks = maxDeletedBricks;
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

    public Map<Pair<Integer, Integer>, Brick> getBricks() {
        return bricks;
    }

    public void setBricks(Map<Pair<Integer, Integer>, Brick> bricks) {
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
    // -------------------------------------------------------------

    GameController(double worldWidth, double worldHeight, double angleBoundMin, double angleBoundMax, int lives, int speed) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.lives = lives;
        this.maxLives = lives;
        this.speed = speed;
        setAngleBounds(angleBoundMin, angleBoundMax);

        ballSpawnX = worldWidth / 2;
        ballSpawnY = worldHeight - 60;

        generateBricks();
        generatePlane();
        generateBalls();

        isPlaying = false;
    }

    private void setAngleBounds(double angleBoundMin, double angleBoundMax) {
        // "Fool`s security" like it called
        if (angleBoundMax >= angleBoundMin) {
            this.angleBoundMin = angleBoundMin;
            this.angleBoundMax = angleBoundMax;
        } else if (angleBoundMax < angleBoundMin) {
            this.angleBoundMin = angleBoundMax;
            this.angleBoundMax = angleBoundMin;
        }
    }

    private void generateBalls() {
        balls = new CopyOnWriteArrayList<>();
        double angle = angleBoundMin;
        for (int i = 0; i < lives; i++) {
            if (angleBoundMax != angleBoundMin)
                //Randomizing angles
                angle = ThreadLocalRandom.current().nextDouble(angleBoundMin, angleBoundMax);
            balls.add(new Ball(ballSpawnX, ballSpawnY, bricks, plane, speed, angle, worldWidth, worldHeight));
        }
        ballThreads = new CopyOnWriteArrayList<>();
        for (int i = 0; i < lives; i++) {
            ballThreads.add(new Thread(balls.get(i)));
            //When game is closed by button in upper corner
            //ball threads will be in infinite cycle
            //this option makes them die with main thread
            ballThreads.get(i).setDaemon(true);
            ballThreads.get(i).start();
        }
    }

    private void generateBricks() {
        bricks = new ConcurrentHashMap<Pair<Integer, Integer>, Brick>();
        int[][] mask;
        try {
            mask = new Reader().readBrickArray();
            for (int i = 0; i < mask.length; i++) {
                for (int j = 0; j < mask[i].length; j++) {
                    //rotate mask by -90 degrees
                    if (mask[j][i] > 0) {
                        bricks.put(new Pair<Integer, Integer>(i * Brick.BRICK_WIDTH, j * Brick.BRICK_HEIGHT), new Brick(i * Brick.BRICK_WIDTH, j * Brick.BRICK_HEIGHT, BrickType.values()[mask[j][i] - 1]));
                        if (mask[j][i] < 6) {
                            //Setting game limit by bricks count
                            maxDeletedBricks++;
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void generatePlane() {
        plane = new Plane(ballSpawnX - Plane.PLANE_WIDTH / 2, ballSpawnY + PLANE_OFFSET);
    }

    private void checkGameOver() {
        //Setting game over depending on game situation
        if (lives < 1) {
            gameOver = true;
            gameOverMessage = GAME_LOST;
        } else if (deletedBricks == maxDeletedBricks) {
            gameOver = true;
            gameOverMessage = GAME_WON;
        }
    }

    private void checkScore() {
        int score = 0;
        int deletedBricks = 0;
        for (Map.Entry<Pair<Integer, Integer>, Brick> brick : bricks.entrySet()) {
            if (!brick.getValue().isIndestructible() && brick.getValue().getBrickHealth() <= 0) {
                brick.getValue().setVisible(false);
                score += brick.getValue().getPoints() * lives / maxLives * speed;
                deletedBricks++;
                bricks.remove(brick.getKey(), brick.getValue());
            }
        }
        this.score += score;
        this.deletedBricks += deletedBricks;
    }

    @Override
    public void run() {
        do {
            if (!gameOver) {
                if (!isPlaying) {
                    //Freezing balls while game is paused
                    //TIME IS NOT PAUSED, THIS IS A FEATURE
                    for (Ball ball : balls) {
                        ball.setFrozen(true);
                    }
                } else {
                    //Counting balls in game
                    int aliveBalls = 0;
                    for (Ball ball : balls) {
                        ball.setFrozen(false);
                        if (!ball.isLost()) {
                            aliveBalls++;
                        }
                    }
                    lives = aliveBalls;
                    checkScore();
                    checkGameOver();

                }
            } else {
                //killing threads
                for (Thread ball : ballThreads) {
                    ball.interrupt();
                }
                return;
            }
            try {
                //delay depends on ball speed
                Thread.sleep((long) (balls.get(0).getSpeed() * Ball.SPEED_MULTIPLIER));
            } catch (InterruptedException e) {
                for (Thread ball : ballThreads) {
                    ball.interrupt();
                }
                return;
            }

        } while (true);
    }
}