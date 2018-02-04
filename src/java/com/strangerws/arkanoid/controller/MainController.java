package com.strangerws.arkanoid.controller;

import com.strangerws.arkanoid.model.*;
import com.strangerws.arkanoid.util.BrickType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class MainController {

    @FXML
    private Pane windowGame;

    @FXML
    private Button btnPlay;

    private GraphicsContext gc;
    private Canvas canvas;

    private Ball[] balls;
    private int lives = 3;
    private double ballSpawnX = (int) (512 / 2);
    private double ballSpawnY = (int) (512 - 60);

    private boolean isPlaying;

    @FXML
    public void startGame() {
        isPlaying = true;
        balls = new Ball[lives];
        Plane plane = new Plane(ballSpawnX - 25, ballSpawnY + 12, 50, 8);

        setPlaneControls(plane);

        canvas = new Canvas(windowGame.getWidth(), windowGame.getHeight());
        gc = canvas.getGraphicsContext2D();
        windowGame.getChildren().add(canvas);
        windowGame.getChildren().add(plane);

        for (int i = 0; i < lives; i++) {
            balls[i] = new Ball(ballSpawnX, ballSpawnY, 0, 0, windowGame.getWidth(), windowGame.getHeight());
        }

        gc.clearRect(0, 0, windowGame.getWidth(), windowGame.getHeight());
        gc.setFill(Color.YELLOWGREEN);
        try {
            Thread gameThread = new Thread(new Game(balls, new Counter(), gc, getBrickLayout(new Reader().readBrickArray())));
            gameThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setPlaneControls(Plane plane) {
        final ObjectProperty<Point2D> mousePosition = new SimpleObjectProperty<>();

        plane.setOnMousePressed(event -> mousePosition.set(new Point2D(event.getSceneX(), event.getSceneY())));
        plane.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - mousePosition.get().getX();
            plane.movePlane(plane.getLayoutX() + deltaX, windowGame.getWidth());
            mousePosition.set(new Point2D(event.getSceneX(), event.getSceneY()));
        });
    }

    private Brick[][] getBrickLayout(int[][] mask) {
        Brick[][] bricks = new Brick[mask.length][];

        for (int i = 0; i < mask.length; i++) {
            bricks[i] = new Brick[mask[i].length];
            for (int j = 0; j < mask[i].length; j++) {
                if (mask[j][i] > 0) bricks[i][j] = new Brick(i * Brick.BRICK_WIDTH, j * Brick.BRICK_HEIGHT, BrickType.values()[mask[j][i] - 1]);
            }
        }

        return bricks;
    }

}
