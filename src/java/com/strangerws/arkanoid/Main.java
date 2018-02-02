package com.strangerws.arkanoid;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/game.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        scene.getStylesheets().add("/css/game.css");

        primaryStage.setTitle("Arkanoid by StrangerWS");
        primaryStage.setScene(scene);
        primaryStage.resizableProperty().set(true);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
