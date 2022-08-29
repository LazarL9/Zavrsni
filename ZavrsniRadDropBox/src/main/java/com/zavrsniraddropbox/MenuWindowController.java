package com.zavrsniraddropbox;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;

public class MenuWindowController {

    @FXML
    private MenuBar mainMenu;

    public void showWindowFiles()throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("FilesWindow.fxml"));
        Scene filesScene=new Scene(root);

        Stage stage=(Stage) mainMenu.getScene().getWindow();

        MainApplication.setMainStage(stage);
        MainApplication.getMainStage().setScene(filesScene);
    }

    public void showMainWindow()throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("MainWindow.fxml"));
        Scene filesScene=new Scene(root);

        Stage stage=(Stage) mainMenu.getScene().getWindow();

        MainApplication.setMainStage(stage);
        MainApplication.getMainStage().setScene(filesScene);
    }

}
