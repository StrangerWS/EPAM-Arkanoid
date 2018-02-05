package com.strangerws.arkanoid.controller;

import com.strangerws.arkanoid.model.Ball;
import com.strangerws.arkanoid.model.Brick;
import com.strangerws.arkanoid.model.Plane;
import javafx.animation.AnimationTimer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewController {

    // FXML Objects
    @FXML
    private Pane windowGame;
    @FXML
    private Button playBtn;
    @FXML
    private Button pauseBtn;
    @FXML
    private Label timeLabel;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label livesLabel;
    @FXML
    private TextField minAngleInput;
    @FXML
    private TextField maxAngleInput;
    @FXML
    private TextField livesInput;
    @FXML
    private TextField speedInput;

    private AnimationTimer timer;

    //
    private double angleBoundMin;
    private double angleBoundMax;
    private int lives;
    private int speed;

    //Game Objects
    private List<List<Brick>> bricks;
    private Ball ball;
    private Plane plane;

    //Game Controller
    private GameController game;

    public void initialize() {
        minAngleInput.setText("45");
        maxAngleInput.setText("135");
        livesInput.setText("3");
        speedInput.setText("5");
    }

    @FXML
    public void playBtnPressed() {
        if (playBtn.getText().toLowerCase().equals("play")) {
            readControls();
            game = new GameController(windowGame.getWidth(), windowGame.getHeight(), angleBoundMin, angleBoundMax, lives, speed);
            initializeGame();
            initCycle();
            timer.start();
            playBtn.setText("Stop");
            disableControls();
            pauseBtn.setDisable(false);
        } else {
            disposeGame();
            game.setGameOver(true);
            timer.stop();
            playBtn.setText("Play");
            pauseBtn.setDisable(true);
            enableControls();

        }
    }

    @FXML
    public void pauseBtnPressed() {
        if (pauseBtn.getText().toLowerCase().equals("pause")) {
            game.setPlaying(false);
            pauseBtn.setText("Resume");
        } else {
            game.setPlaying(true);
            pauseBtn.setText("Pause");
        }
    }

    private void readControls() {
        angleBoundMin = Double.parseDouble(minAngleInput.getText());
        angleBoundMax = Double.parseDouble(maxAngleInput.getText());
        lives = Integer.parseInt(livesInput.getText());
        speed = Integer.parseInt(speedInput.getText());
    }

    private void disableControls() {
        minAngleInput.setDisable(true);
        maxAngleInput.setDisable(true);
        livesInput.setDisable(true);
        speedInput.setDisable(true);
    }

    private void enableControls() {
        minAngleInput.setDisable(false);
        maxAngleInput.setDisable(false);
        livesInput.setDisable(false);
        speedInput.setDisable(false);
    }

    private void initializeGame() {
        ball = game.getBall();
        plane = game.getPlane();
        bricks = game.getBricks();

        setControls();
        game.setPlaying(true);

        windowGame.getChildren().add(plane);
        windowGame.getChildren().add(ball);
        for (List<Brick> b : bricks) {
            for (Brick brick : b) {
                windowGame.getChildren().addAll(brick);
            }
        }
    }

    private void disposeGame() {
        windowGame.getChildren().removeAll(windowGame.getChildren());
    }

    public void initCycle() {
        Date date = new Date();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                Calendar date2 = Calendar.getInstance();
                long seconds = date2.getTime().getTime() - date.getTime();
                SimpleDateFormat gameTime = new SimpleDateFormat("mm:ss", Locale.getDefault());
                timeLabel.setText("Time: " + gameTime.format(seconds));
                if (game.isPlaying()) {
                    scoreLabel.setText("Score: " + game.getScore());
                    livesLabel.setText("Lives: " + game.getLives());
                    if (!ball.isLost()) {
                        ball.move();
                        game.checkReflections();
                    } else {
                        game.decreaseLives();
                        game.newTurn();
                    }
                }

                if (game.isGameOver()) {
                    timer.stop();
                    disposeGame();
                    showDialogGameOver();
                    playBtn.setText("Play");
                    pauseBtn.setText("Pause");
                    pauseBtn.setDisable(true);
                }
            }
        };
    }

    private void setControls() {
        final ObjectProperty<Point2D> mousePosition = new SimpleObjectProperty<>();

        plane.setOnMousePressed(event -> mousePosition.set(new Point2D(event.getSceneX(), event.getSceneY())));
        plane.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - mousePosition.get().getX();
            plane.movePlane(plane.getX() + deltaX, windowGame.getWidth());
            if (ball.isFrozen())
                ball.moveWithPlane(plane.getX() + plane.getWidth() / 2);
            mousePosition.set(new Point2D(event.getSceneX(), event.getSceneY()));
        });

        windowGame.getScene().setOnKeyPressed(e -> {
            //Because enter and space activates focused button
            if (e.getCode() == KeyCode.W) {
                ball.start();
            }
        });

    }

    public void showDialogGameOver() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over!");
        alert.setHeaderText(game.getGameOverMessage());
        alert.setContentText("Your " + scoreLabel.getText());
        alert.show();
    }
}