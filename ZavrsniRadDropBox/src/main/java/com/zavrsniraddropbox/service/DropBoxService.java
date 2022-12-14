package com.zavrsniraddropbox.service;

import com.dropbox.core.*;
import com.dropbox.core.json.JsonReader;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DropBoxService {

    public static DbxClientV2 client;

    @FXML
    private ProgressBar progressBar;

    private int steps=1000;
    private int curStep=0;

    public static DbxClientV2 getClient(){
        if(client==null){
            setClient();
        }
        try{
            client.check().user();
        }catch (InvalidAccessTokenException e){
            setClient();
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return client;
    }

    private static void setClient(){
        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
        client = new DbxClientV2(config, getToken());
    }

    public static void removeClient(){
        client=null;
    }


    private static DbxCredential getToken(){
        final File DATABASE_FILE = new File("src/main/resources/properties.properties");

        DbxCredential credential = null;
        try {
            credential = DbxCredential.Reader.readFromFile(DATABASE_FILE);
        } catch (JsonReader.FileLoadException ex) {
            ex.printStackTrace();
            return null;
        }
        return credential;
    }

    public static void deleteFile(String path){
        try {
            client.files().deleteV2(path);
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    public static String getFolder(){
        Stage stage = new Stage();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);

        if(selectedDirectory == null){
            return null;
        }else{
            return selectedDirectory.getAbsolutePath();
        }
    }

    public static String getFile(){
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if(selectedFile == null){
            return null;
        }else{
           return selectedFile.getAbsolutePath();
        }
    }

    public static void renameFile(String fromFilePath, String toFilePath){
        String rename=fromFilePath.substring(fromFilePath.lastIndexOf('/'));
        String renamed=fromFilePath.substring(0,fromFilePath.length()- rename.length());
        if(fromFilePath.equals(renamed+"/"+toFilePath)){
            return;
        }
        try {
            client.files().moveV2(fromFilePath,renamed+"/"+toFilePath);
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    public static void moveFile(String fromFilePath, String toFilePath){
        String rename=fromFilePath.substring(fromFilePath.lastIndexOf('/'));

        try {
            client.files().moveV2(fromFilePath,toFilePath+rename);
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(String fromFilePath, String toFilePath){
        String rename=fromFilePath.substring(fromFilePath.lastIndexOf('/'));

        try {
            client.files().copyV2(fromFilePath,toFilePath+rename);
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    public static void createFolder(String currentFolder,String folderName){
        try {
            client.files().createFolderV2(currentFolder+"/"+folderName);
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    public static void downloadFolder(String path,String downloadTo) throws DbxException {
        String newFolder=downloadTo+path;
        File file=new File(newFolder);
        file.mkdir();
        ListFolderResult result = client.files().listFolder(path);
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                if(metadata.getClass()== FolderMetadata.class){
                    downloadFolder(metadata.getPathLower(),downloadTo);
                }else{
                    try {
                        DbxDownloader<FileMetadata> downloader = client.files().download(metadata.getPathLower());

                            FileOutputStream out = new FileOutputStream(downloadTo+metadata.getPathLower());
                            downloader.download(out);
                            out.close();
                    } catch (DbxException | IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }
            if (!result.getHasMore()) {
                break;
            }

            result = client.files().listFolderContinue(result.getCursor());
        }
    }

    public static void downloadFile(String path,String downloadPath){
        DbxDownloader<FileMetadata> downloader = null;
        try {
            downloader = client.files().download(path);
        } catch (DbxException e) {
            e.printStackTrace();
        }
        if(downloadPath!=null){
            try {
                FileOutputStream out = new FileOutputStream(downloadPath+path);
                downloader.download(out);
                out.close();
            } catch (DbxException ex) {
                System.out.println(ex.getMessage());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void uploadFile(String filePath){
        String path=filePath.substring(filePath.lastIndexOf("\\"));
        path=path.substring(0,0)+"/"+path.substring(1);

        try (InputStream in = new FileInputStream(filePath)) {
            client.files().uploadBuilder(path)
                    .uploadAndFinish(in);
        } catch (IOException | DbxException e) {
            e.printStackTrace();
        }
    }

    public static void uploadFoldera(String folderPath){
        List<Path> result = null;
        try (Stream<Path> walk = Files.walk(Path.of(folderPath))) {
            result = walk.collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> files= result.stream().map(Object::toString).toList();
        String rename=files.get(0).substring(files.get(0).lastIndexOf('\\'));
        String absolutPath=files.get(0).substring(0,files.get(0).length()- rename.length());

        for (int i=0;i<files.size();i++) {

            File file = new File(files.get(i));
            if (file.isDirectory()) {
                String fileNoAbsolute = files.get(i).substring(absolutPath.length());
                fileNoAbsolute = fileNoAbsolute.replace('\\', '/');
                try {
                    client.files().createFolderV2(fileNoAbsolute);
                } catch (CreateFolderErrorException x) {
                    deleteFile(fileNoAbsolute);
                    uploadFoldera(folderPath);
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
        }
    }

    public static void uploadFolderAuto(String folderPath,String folderRoot){
        List<Path> result = null;
        try (Stream<Path> walk = Files.walk(Path.of(folderPath))) {
            result = walk.collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> files= result.stream().map(Object::toString).toList();
        String rename=files.get(0).substring(files.get(0).lastIndexOf('\\'));
        String absolutPath=files.get(0).substring(0,files.get(0).length()- rename.length());


        for (String s : files) {
            File file = new File(s);
            if (file.isDirectory()) {
                String fileNoAbsolute = s.substring(absolutPath.length());
                fileNoAbsolute = fileNoAbsolute.replace('\\', '/');
                try {
                    client.files().createFolderV2(folderRoot + fileNoAbsolute);
                } catch (DbxException e) {
                    e.printStackTrace();
                }
            } else {
                String fileNoAbsolute = s.substring(absolutPath.length());
                fileNoAbsolute = fileNoAbsolute.replace('\\', '/');
                try (InputStream in = new FileInputStream(s)) {
                    client.files().uploadBuilder(folderRoot + fileNoAbsolute)
                            .uploadAndFinish(in);
                } catch (IOException | DbxException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void uploadFileAuto(String filePath,String folderRoot){
        String path=filePath.substring(filePath.lastIndexOf("\\"));
        path=path.substring(0,0)+"/"+path.substring(1);

        try (InputStream in = new FileInputStream(filePath)) {
            client.files().uploadBuilder(folderRoot+path)
                    .uploadAndFinish(in);
        } catch (IOException | DbxException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFileAuto(String path,String folderRoot){
        try {
            client.files().deleteV2(folderRoot+path);
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    public void uploadFolder(String folderPath) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<Path> result = null;
                try (Stream<Path> walk = Files.walk(Path.of(folderPath))) {
                    result = walk.collect(Collectors.toList());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                List<String> files= result.stream().map(Object::toString).toList();
                String rename=files.get(0).substring(files.get(0).lastIndexOf('\\'));
                String absolutPath=files.get(0).substring(0,files.get(0).length()- rename.length());

                steps=files.size();
                for (int i=0;i<files.size();i++) {
                    curStep=i;
                    Thread.sleep(10);

                    updateProgress(curStep, steps);
                    File file = new File(files.get(i));
                    if (file.isDirectory()) {
                        String fileNoAbsolute = files.get(i).substring(absolutPath.length());
                        fileNoAbsolute = fileNoAbsolute.replace('\\', '/');
                        try {
                            client.files().createFolderV2(fileNoAbsolute);
                        } catch (CreateFolderErrorException x) {
                            deleteFile(fileNoAbsolute);
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
                }
                updateProgress(1, 1);
                return null;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

}
