package com.strangerws.arkanoid.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

public class Render implements Runnable {

    private Plane plane;
    private Ball ball;
    private Counter counter;
    private GraphicsContext gc;

    public Render(Ball ball, Counter counter, Plane plane, GraphicsContext gc) {
        this.ball = ball;
        this.counter = counter;
        this.plane = plane;
        this.gc = gc;
    }

    @Override
    public void run() {
        Thread ballThread = new Thread(ball);
        Thread countThread = new Thread(counter);
        ballThread.setDaemon(true);
        countThread.setDaemon(true);
        ballThread.start();
        countThread.start();


        while (true) {
            try {
                render();
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void render() {
        gc.clearRect(0, 0, 512, 512);
        gc.setFill(ball.getFill());
        gc.fillOval(ball.getCenterX() - ball.getRadius(), ball.getCenterY() + ball.getRadius(), ball.getRadius() * 2, ball.getRadius() * 2);
        gc.setFill(plane.getFill());
        gc.fillPolygon(plane.getxPoints(), plane.getyPoints(), plane.getxPoints().length);
    }
}
