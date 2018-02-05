package com.strangerws.arkanoid.controller;

import com.strangerws.arkanoid.model.Ball;
import com.strangerws.arkanoid.model.Brick;
import com.strangerws.arkanoid.model.Counter;
import com.strangerws.arkanoid.model.Plane;
import com.strangerws.arkanoid.reader.Reader;
import com.strangerws.arkanoid.util.BrickType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainController {

    @FXML
    private Pane windowGame;

    @FXML
    private Button btnPlay;

    private Ball ball;
    private double ballSpawnX;
    private double ballSpawnY;
    private Plane plane;

    @FXML
    public void startGame() {
        ballSpawnX = windowGame.getWidth() / 2;
        ballSpawnY = windowGame.getHeight() - 60;

        //moving plane from ball spawn at half of plane width and ball radius
        plane = new Plane(ballSpawnX - 25, ballSpawnY + 5, 50, 8);
        setPlaneControls();

        windowGame.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.W) {
                ball.start();
            }
        });

        windowGame.getChildren().add(plane);
        ball = new Ball(ballSpawnX, ballSpawnY, 5, Math.random(), windowGame.getHeight());
        windowGame.getChildren().add(ball);
        try {
            GameController gc = new GameController(ball, new Counter(), getBrickLayout(new Reader().readBrickArray()), plane, windowGame);
            gc.setPlaying(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setPlaneControls() {
        final ObjectProperty<Point2D> mousePosition = new SimpleObjectProperty<>();

        plane.setOnMousePressed(event -> mousePosition.set(new Point2D(event.getSceneX(), event.getSceneY())));
        plane.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - mousePosition.get().getX();
            plane.movePlane(plane.getX() + deltaX, windowGame.getWidth());
            if (ball.isFrozen())
                ball.moveWithPlane(plane.getX() + plane.getWidth() / 2);
            mousePosition.set(new Point2D(event.getSceneX(), event.getSceneY()));
        });
    }
    private List<List<Brick>> getBrickLayout(int[][] mask) {
        List<List<Brick>> bricks = new ArrayList<>(mask.length);

        for (int i = 0; i < mask.length; i++) {
            bricks.add(new ArrayList<>(mask[i].length));
            for (int j = 0; j < mask[i].length; j++) {
                if (mask[j][i] > 0)
                    bricks.get(i).add(new Brick(i * Brick.BRICK_WIDTH, j * Brick.BRICK_HEIGHT, BrickType.values()[mask[j][i] - 1]));
                if (bricks.get(i).get(j) != null) windowGame.getChildren().add(bricks.get(i).get(j));
            }
        }

        return bricks;
    }

}
