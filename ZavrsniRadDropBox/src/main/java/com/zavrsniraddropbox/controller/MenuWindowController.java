package com.zavrsniraddropbox.controller;

import com.dropbox.core.oauth.DbxCredential;
import com.zavrsniraddropbox.MainApplication;
import com.zavrsniraddropbox.service.DropBoxService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MenuWindowController {

    @FXML
    private MenuBar mainMenu;

    @FXML
    private Menu datotekaMenu;

    @FXML
    private Menu odjavaMenu;

    @FXML
    private Label korisnikLabel;

    public void setKorisnikLabel(String name){
        korisnikLabel.setText(name);
    }

    public void setDisableF(){
        datotekaMenu.setDisable(false);
        odjavaMenu.setDisable(false);
    }

    public void odjaviSe(){
        DbxCredential credential=new DbxCredential("Radnom");

        final File DATABASE_FILE = new File("src/main/resources/properties.properties");

        try {
            DbxCredential.Writer.writeToFile(credential,DATABASE_FILE);
        } catch (IOException ex) {
            System.err.println("Error saving to <auth-file-out>: " + ex.getMessage());
            System.err.println("Dumping to stderr instead:");
        }

        DropBoxService.removeClient();
        try {
            showMainWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showWindowFiles()throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("FilesWindow.fxml"));
        Scene filesScene=new Scene(root);

        Stage stage=(Stage) mainMenu.getScene().getWindow();
        stage.setTitle("Datoteke");
        MainApplication.setMainStage(stage);
        MainApplication.getMainStage().setScene(filesScene);
    }

    public void showMainWindow()throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("MainWindow.fxml"));
        Scene filesScene=new Scene(root);

        Stage stage=(Stage) mainMenu.getScene().getWindow();
        stage.setTitle("Prijavi se");
        MainApplication.setMainStage(stage);
        MainApplication.getMainStage().setScene(filesScene);
    }



}
