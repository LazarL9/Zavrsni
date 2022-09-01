package com.zavrsniraddropbox.controller;

import com.dropbox.core.*;
import com.dropbox.core.oauth.DbxCredential;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class NewWindowController extends Application implements Initializable {

    @FXML
    public TextField appKey;

    @FXML
    public PasswordField appSecret;

    @FXML
    public TextField accessToken;

    @FXML
    public VBox accessTokenBox;

    @FXML
    public VBox generateTokenBox;

    @FXML
    public Hyperlink linkToken;

    @FXML
    private void onTokenGenerateClick(){
        setLink();
    }

    @FXML
    private void buttonClick(){

        setToken();
        Stage stage = (Stage) appKey.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void cancelClick(){
        Stage stage = (Stage) appKey.getScene().getWindow();
        stage.close();
    }

    private DbxAppInfo appInfo;
    private DbxRequestConfig requestConfig = new DbxRequestConfig("authorize");
    private DbxWebAuth webAuth;

    public void setLink(){

        appInfo=new DbxAppInfo(appKey.textProperty().getValueSafe(),appSecret.textProperty().getValueSafe());
        webAuth= new DbxWebAuth(requestConfig, appInfo);
        DbxWebAuth.Request webAuthRequest = DbxWebAuth.newRequestBuilder()
                .withNoRedirect()
                .build();
        String link=webAuth.authorize(webAuthRequest);
        System.out.println(link);
        linkToken.setText(link);
        linkToken.setOnAction(e -> {
            getHostServices().showDocument(link);
        });
        accessTokenBox.setVisible(true);
    }
    private DbxAuthFinish authFinish;
    private DbxAuthInfo authInfo;

    public void setToken(){
        String token=accessToken.textProperty().getValueSafe();
        token=token.trim();

        try {
            authFinish = webAuth.finishFromCode(token);
        } catch (DbxException ex) {
            System.err.println("Error in DbxWebAuth.authorize: " + ex.getMessage());
        }

        authInfo=new DbxAuthInfo(authFinish.getAccessToken(),appInfo.getHost());
        DbxCredential credential=new DbxCredential(authInfo.getAccessToken());

        final File DATABASE_FILE = new File("src/main/resources/properties.properties");

        try {
            DbxCredential.Writer.writeToFile(credential,DATABASE_FILE);
        } catch (IOException ex) {
            System.err.println("Error saving to <auth-file-out>: " + ex.getMessage());
            System.err.println("Dumping to stderr instead:");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final File INFO_FILE = new File("src/main/resources/information.properties");

        try (InputStream input = new FileInputStream(INFO_FILE)) {
            Properties info=new Properties();
            info.load(input);
            appKey.textProperty().setValue(info.getProperty("appKey").trim());
            appSecret.textProperty().setValue(info.getProperty("appSecret").trim());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {

    }
}
