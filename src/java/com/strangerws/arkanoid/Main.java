package com.strangerws.arkanoid;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static final String GAME_SCREEN_LOCATION = "/fxml/game.fxml";
    private static final String GAME_CSS_LOCATION = "/css/game.css";
    private final FXMLLoader loader = new FXMLLoader();

    @Override
    public void start(Stage primaryStage) throws Exception {
        loader.setLocation(getClass().getResource(GAME_SCREEN_LOCATION));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        scene.getStylesheets().add(GAME_CSS_LOCATION);

        primaryStage.setTitle("Arkanoid by StrangerWS");
        primaryStage.setScene(scene);
        primaryStage.resizableProperty().set(true);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
