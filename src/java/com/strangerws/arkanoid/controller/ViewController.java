package com.strangerws.arkanoid.controller;

import com.strangerws.arkanoid.model.Ball;
import com.strangerws.arkanoid.model.Brick;
import com.strangerws.arkanoid.model.Plane;
import javafx.animation.AnimationTimer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

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
    private ChoiceBox<Double> minAngleInput;
    @FXML
    private ChoiceBox<Double> maxAngleInput;
    @FXML
    private ChoiceBox<Integer> livesInput;
    @FXML
    private ChoiceBox<Integer> speedInput;

    //Canvas Rendering Objects
    private AnimationTimer timer;
    private GraphicsContext gc;
    private Canvas canvas;

    //Game Properties
    private double angleBoundMin;
    private double angleBoundMax;
    private int lives;
    private int speed;

    //Game Objects
    private List<List<Brick>> bricks;
    private List<Ball> balls;
    private Plane plane;

    //Game Controller
    private GameController game;
    private Thread gameThread;

    public void initialize() {
        for (int i = 5; i < 176; i++) {
            minAngleInput.getItems().add((double) i);
            maxAngleInput.getItems().add((double) i);
        }
        for (int i = 1; i <= 10; i++) {
            livesInput.getItems().add(i);
        }
        for (int i = 1; i <= 10; i++) {
            speedInput.getItems().add(i);
        }

        minAngleInput.setValue(45.0);
        maxAngleInput.setValue(135.0);
        livesInput.setValue(3);
        speedInput.setValue(5);
    }

    @FXML
    public void playBtnPressed() {
        if (playBtn.getText().toLowerCase().equals("play")) {
            readControls();
            game = new GameController(windowGame.getWidth(), windowGame.getHeight(), angleBoundMin, angleBoundMax, lives, speed);
            initializeGraphics();
            initializeGameScreen();
            gameThread = new Thread(game);
            game.setPlaying(true);
            timer.start();
            gameThread.start();
            playBtn.setText("Stop");
            disableControls();
            pauseBtn.setDisable(false);
        } else {
            game.setGameOver(true);
            gameThread.interrupt();
            disposeGame();
            playBtn.setText("Play");
            pauseBtn.setDisable(true);
            enableControls();
            timer.stop();
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
        angleBoundMin = minAngleInput.getValue();
        angleBoundMax = maxAngleInput.getValue();
        lives = livesInput.getValue();
        speed = speedInput.getValue();
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

    private void initializeGameScreen() {
        balls = game.getBalls();
        plane = game.getPlane();
        bricks = game.getBricks();

        setControls();
        game.setPlaying(true);

        windowGame.getChildren().add(plane);
        for (List<Brick> b : bricks) {
            for (Brick brick : b) {
                windowGame.getChildren().addAll(brick);
            }
        }
    }

    private void initializeGraphics() {
        canvas = new Canvas(windowGame.getWidth(), windowGame.getHeight());
        gc = canvas.getGraphicsContext2D();
        windowGame.getChildren().add(canvas);

        Date date = new Date();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                //Time calculating
                Calendar date2 = Calendar.getInstance();
                long seconds = date2.getTime().getTime() - date.getTime();
                SimpleDateFormat gameTime = new SimpleDateFormat("mm:ss", Locale.getDefault());
                timeLabel.setText("Time: " + gameTime.format(seconds));

                //Rendering balls on canvas (used to get rid of visual bugs)
                gc.clearRect(0, 0, windowGame.getWidth(), windowGame.getHeight());
                gc.setFill(Color.YELLOWGREEN);
                gc.fillRect(0, 0, windowGame.getWidth(), windowGame.getHeight());
                renderBalls();
                checkBricks();

                //Printing labels
                if (game.isPlaying()) {
                    scoreLabel.setText("Score: " + game.getScore());
                    livesLabel.setText("Lives: " + game.getLives());
                }

                if (game.isGameOver()) {
                    game.setGameOver(true);
                    gameThread.interrupt();
                    disposeGame();
                    playBtn.setText("Play");
                    pauseBtn.setDisable(true);
                    enableControls();
                    timer.stop();
                    showDialogGameOver();
                }
            }
        };
    }

    private void checkBricks() {
        int score = 0;
        for (List<Brick> b : bricks) {
            for (Brick brick : b) {
                if (!brick.isIndestructible() && brick.getBrickHealth() <= 0) {
                    brick.setVisible(false);
                    b.remove(brick);
                    score += brick.getPoints();
                }
            }
        }
        game.setScore(game.getScore() + score);
    }

    private void renderBalls() {
        for (Ball ball : balls) {
            gc.setFill(ball.getFill());
            gc.fillOval(ball.getHitboxX(), ball.getHitboxY(), ball.getWidth(), ball.getHeight());
            gc.setStroke(Color.BLACK);
            gc.strokeOval(ball.getHitboxX(), ball.getHitboxY(), ball.getWidth(), ball.getHeight());
        }
    }

    private void disposeGame() {
        windowGame.getChildren().removeAll(windowGame.getChildren());
    }

    private void setControls() {
        final ObjectProperty<Point2D> mousePosition = new SimpleObjectProperty<>();

        plane.setOnMousePressed(event -> mousePosition.set(new Point2D(event.getSceneX(), event.getSceneY())));

        plane.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - mousePosition.get().getX();
            if (game.isPlaying()) {
                plane.movePlane(plane.getX() + deltaX, windowGame.getWidth());
                for (Ball ball : balls) {
                    if (ball.isAiming())
                        ball.moveWithPlane(plane.getX() + plane.getWidth() / 2);
                }
                mousePosition.set(new Point2D(event.getSceneX(), event.getSceneY()));
            }
        });

        windowGame.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.W) {
                for (Ball ball : balls) {
                    if (ball.isAiming()) {
                        ball.setAiming(false);
                    } else {
                        pauseBtnPressed();
                    }
                }
            }
        });

    }

    private void showDialogGameOver() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over!");
        alert.setHeaderText(game.getGameOverMessage());
        alert.setContentText("Your " + scoreLabel.getText());
        alert.show();
    }
}
