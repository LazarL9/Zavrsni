package com.zavrsniraddropbox.controller;

import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.v2.check.EchoResult;
import com.zavrsniraddropbox.MainApplication;
import com.zavrsniraddropbox.service.DropBoxService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;


public class MainController implements Initializable {

    @FXML
    private Label welcomeText;

    @FXML
    private MenuWindowController menuBarController;

    @FXML
    protected void onHelloButtonClick() {
        try {
            nekaj();
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    private void nekaj() throws DbxException{
        DbxClientV2 client = DropBoxService.getClient();
            try{
                client.check().user();
            }catch (InvalidAccessTokenException e){
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getClassLoader().getResource("NewWindow.fxml"));

                    Scene scene = new Scene(fxmlLoader.load());
                    Stage stage = new Stage();
                    stage.setTitle("New Accses Token");
                    stage.setScene(scene);
                    stage.show();
                    welcomeText.setText("Prijava nije uspijela, pokušajte ponovo.");
                    return;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        menuBarController.setDisableF();
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getClassLoader().getResource("FilesWindow.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene filesScene=new Scene(root);

        Stage stage=(Stage) welcomeText.getScene().getWindow();
        stage.setTitle("Datoteke");
        MainApplication.setMainStage(stage);
        MainApplication.getMainStage().setScene(filesScene);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    try {
                        nekaj();
                    } catch (DbxException e) {
                        e.printStackTrace();
                    }
                });
            }
        }, 100);
    }
}