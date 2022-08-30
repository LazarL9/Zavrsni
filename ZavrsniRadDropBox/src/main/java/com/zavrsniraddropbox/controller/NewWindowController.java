package com.zavrsniraddropbox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.util.Properties;

public class NewWindowController {

    @FXML
    public TextArea accesTokenArea;

    @FXML
    private void buttonClick(){

        setToken();
        Stage stage = (Stage) accesTokenArea.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void cancelClick(){
        Stage stage = (Stage) accesTokenArea.getScene().getWindow();
        stage.close();
    }

    public void setToken(){
        final String DATABASE_FILE = "src/main/resources/properties.properties";

        try (OutputStream output = new FileOutputStream(DATABASE_FILE)) {

            Properties prop = new Properties();

            // set the properties value
            prop.setProperty("accsesToken", accesTokenArea.textProperty().getValueSafe());

            // save properties to project root folder
            prop.store(output, null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
