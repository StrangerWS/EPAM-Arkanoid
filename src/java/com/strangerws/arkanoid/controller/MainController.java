package com.strangerws.arkanoid.controller;

import com.strangerws.arkanoid.model.Ball;
import com.strangerws.arkanoid.model.Counter;
import com.strangerws.arkanoid.model.Plane;
import com.strangerws.arkanoid.model.Render;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
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
        Thread gameThread = new Thread(new Render(balls[lives - 1], new Counter(), gc));
        gameThread.start();

    }

    private void setPlaneControls(Plane plane) {
        final ObjectProperty<Point2D> mousePosition = new SimpleObjectProperty<>();

        plane.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mousePosition.set(new Point2D(event.getSceneX(), event.getSceneY()));
            }
        });

        plane.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double deltaX = event.getSceneX() - mousePosition.get().getX();
                plane.movePlane(plane.getLayoutX() + deltaX, windowGame.getWidth());
                mousePosition.set(new Point2D(event.getSceneX(), event.getSceneY()));
            }
        });
    }

}
