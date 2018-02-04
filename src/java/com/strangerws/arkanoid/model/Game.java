package com.strangerws.arkanoid.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Game implements Runnable {

    private Ball[] balls;
    private Counter counter;
    private GraphicsContext gc;
    private Brick[][] bricks;

    public Game(Ball[] balls, Counter counter, GraphicsContext gc, Brick[][] bricks) {
        this.balls = balls;
        this.counter = counter;
        this.gc = gc;
        this.bricks = bricks;
    }

    @Override
    public void run() {
        Thread[] ballThreads = new Thread[balls.length];
        Thread countThread = new Thread(counter);
        countThread.setDaemon(true);
        for (int i = 0; i < ballThreads.length; i++) {
            ballThreads[i] = new Thread(balls[i]);
            ballThreads[i].setDaemon(true);
            ballThreads[i].start();
        }
        countThread.start();

        while (true) {
            try {
                render();
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void render() {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        for (int i = 0; i < balls.length; i++) {
            gc.setFill(balls[i].getFill());
            gc.fillOval(balls[i].getCenterX() - balls[i].getRadius(), balls[i].getCenterY() + balls[i].getRadius(), balls[i].getRadius() * 2, balls[i].getRadius() * 2);
        }

        for (int i = 0; i < bricks.length;  i++) {
            for (int j = 0; j < bricks[i].length; j++) {
                Brick tmp = bricks[i][j];
                if (tmp != null && tmp.getBrickHealth() > 0){
                    gc.setFill(tmp.getColor());
                    gc.setStroke(tmp.getStroke());
                    gc.fillRect(tmp.getX(), tmp.getY(), tmp.getWidth(), tmp.getHeight());
                    gc.strokeRect(tmp.getX(), tmp.getY(), tmp.getWidth(), tmp.getHeight());
                }
            }
        }
    }
}
