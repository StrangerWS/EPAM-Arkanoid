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
import javafx.util.Pair;

import java.text.SimpleDateFormat;
import java.util.*;

public class ViewController {


    //Some static strings and constants
    private static final String PLAY = "Play";
    private static final String STOP = "Stop";
    private static final String PAUSE = "Pause";
    private static final String RESUME = "Resume";
    private static final String GAME_OVER = "Game Over!";

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
    private Map<Pair<Integer, Integer>, Brick> bricks;
    private List<Ball> balls;
    private Plane plane;

    //Game Controller
    private GameController game;
    private Thread gameThread;

    public void initialize() {
        //an easy way to control user input - using a choice boxes
        //another layer of "fool`s security"
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
    //A good way to create double-function button
    public void playBtnPressed() {
        if (playBtn.getText().equals(PLAY)) {
            readControls();
            game = new GameController(windowGame.getWidth(), windowGame.getHeight(), angleBoundMin, angleBoundMax, lives, speed);
            initializeGraphics();
            initializeGameScreen();
            gameThread = new Thread(game);
            game.setPlaying(true);
            gameThread.setDaemon(true);
            timer.start();
            gameThread.start();
            playBtn.setText(STOP);
            disableControls();
            pauseBtn.setDisable(false);
        } else {
            game.setGameOver(true);
            gameThread.interrupt();
            disposeGame();
            playBtn.setText(PLAY);
            pauseBtn.setDisable(true);
            enableControls();
            timer.stop();
        }
    }

    @FXML
    public void pauseBtnPressed() {
        if (pauseBtn.getText().equals(PAUSE)) {
            game.setPlaying(false);
            pauseBtn.setText(RESUME);
        } else {
            game.setPlaying(true);
            pauseBtn.setText(PAUSE);
        }
    }

    //Those methods doing the things in their names
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
        for (Map.Entry<Pair<Integer, Integer>, Brick> brick : bricks.entrySet()) {
            windowGame.getChildren().add(brick.getValue());
        }

    }

    private void initializeGraphics() {
        canvas = new Canvas(windowGame.getWidth(), windowGame.getHeight());
        gc = canvas.getGraphicsContext2D();
        windowGame.getChildren().add(canvas);

        Date date = new Date();
        //We are using timer to check game state and render balls on canvas
        //Also this is a good low-cost way to make a timer
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

                //Disposing game and killing game thread
                if (game.isGameOver()) {
                    game.setGameOver(true);
                    gameThread.interrupt();
                    disposeGame();
                    playBtn.setText(PLAY);
                    pauseBtn.setDisable(true);
                    enableControls();
                    timer.stop();
                    showDialogGameOver();
                }
            }
        };
    }

    //Checking health of all bricks in timer
    //because bricks is a part of UI
    private void checkBricks() {
        for (Map.Entry<Pair<Integer, Integer>, Brick> brick : bricks.entrySet()) {
            if (!brick.getValue().isIndestructible() && brick.getValue().getBrickHealth() <= 0) {
                brick.getValue().setVisible(false);
                bricks.remove(brick.getKey(), brick.getValue());
            }
        }
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

    //Using mouse as controls of a plane
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
            //Enter and space invoke focused button methods, using any other button
            //With this button you can pause the game faster than if you place the mouse pointer on UI button
            //And unpause game with pointer ready to control the plane
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

    //Showing your achievements
    private void showDialogGameOver() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(GAME_OVER);
        alert.setHeaderText(game.getGameOverMessage());
        alert.setContentText("Your " + scoreLabel.getText());
        alert.show();
    }
}
