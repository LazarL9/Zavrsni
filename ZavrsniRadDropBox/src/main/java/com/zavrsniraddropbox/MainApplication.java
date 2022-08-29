package com.zavrsniraddropbox;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    private static Stage mainStage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        mainStage=primaryStage;

        Parent root=FXMLLoader.load(getClass().getClassLoader().getResource("MainWindow.fxml"));
        primaryStage.setTitle("Prijavi se");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static Stage getMainStage(){
        return mainStage;
    }

    public static void setMainStage(Stage newStage){
        mainStage=newStage;
    }

    public static void main(String[] args) {
        launch();
    }
}