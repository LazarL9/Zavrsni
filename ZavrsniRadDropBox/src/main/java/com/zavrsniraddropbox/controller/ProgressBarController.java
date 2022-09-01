package com.zavrsniraddropbox.controller;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.zavrsniraddropbox.service.DropBoxService;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;



public class ProgressBarController implements Initializable{


    @FXML
    private ProgressBar progressBar;
    static DoubleProperty barUpdater=new SimpleDoubleProperty(.0);
    double postotak=.0;



    public void uploadFolder(String folderPath){
        DbxClientV2 client = DropBoxService.getClient();
        Integer maxBroj;
        List<Path> result = null;
        try (Stream<Path> walk = Files.walk(Path.of(folderPath))) {
            result = walk.collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> files= result.stream().map(Object::toString).toList();
        String rename=files.get(0).substring(files.get(0).lastIndexOf('\\'));
        String absolutPath=files.get(0).substring(0,files.get(0).length()- rename.length());

        maxBroj=files.size();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                for (int i=0;i<files.size();i++) {
                    postotak=(double) i/maxBroj;
                    File file = new File(files.get(i));
                    if (file.isDirectory()) {
                        String fileNoAbsolute = files.get(i).substring(absolutPath.length());
                        fileNoAbsolute = fileNoAbsolute.replace('\\', '/');
                        try {
                            client.files().createFolderV2(fileNoAbsolute);
                        } catch (CreateFolderErrorException x) {
                            DropBoxService.deleteFile(fileNoAbsolute);
                            uploadFolder(folderPath);
                        } catch (DbxException e) {
                            e.printStackTrace();
                        }
                    } else {
                        String fileNoAbsolute = files.get(i).substring(absolutPath.length());
                        fileNoAbsolute = fileNoAbsolute.replace('\\', '/');
                        try (InputStream in = new FileInputStream(files.get(i))) {
                            client.files().uploadBuilder(fileNoAbsolute)
                                    .uploadAndFinish(in);
                        } catch (IOException | DbxException e) {
                            e.printStackTrace();
                        }
                    }
                    Thread.sleep(10); // Pause briefly
                    // Update our progress and message properties
                    progressBar.setProgress(postotak);
                }
                return null;
            }
        };
        new Thread(task).start();
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        progressBar.progressProperty().bind(barUpdater);
    }
}
