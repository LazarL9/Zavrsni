package com.zavrsniraddropbox.controller;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import com.zavrsniraddropbox.service.AutoFolder;
import com.zavrsniraddropbox.service.DropBoxService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class FilesWindowController implements Initializable {

    @FXML
    private TreeView<Metadata> treeView;

    @FXML
    private VBox vBox;

    private DbxClientV2 client= DropBoxService.getClient();
    private String currentFolder="";
    private String FolderName="New Folder";

    public void createTree(Metadata metadata, TreeItem<Metadata> parent) throws DbxException {
        if (metadata.getClass()== FolderMetadata.class) {
            TreeItem<Metadata> treeItem = new TreeItem<>(metadata);
            parent.getChildren().add(treeItem);
            ListFolderResult result = client.files().listFolder(metadata.getPathLower());
            for (Metadata f : result.getEntries()) {
                createTree(f, treeItem);
            }
        } else{
            parent.getChildren().add(new TreeItem<>(metadata));
        }
    }


    public void displayTreeView(String inputDirectoryLocation){
        // Creates the root item.
        TreeItem<Metadata> rootItem = new TreeItem<>();
        // Hides the root item of the tree view.
        treeView.setShowRoot(false);
        // Creates the cell factory.

        treeView.setCellFactory(new Callback<TreeView<Metadata>, TreeCell<Metadata>>() {
            @Override
            public TreeCell<Metadata> call(TreeView<Metadata> metadataTreeView) {
                TreeCell<Metadata> treeCell = new TreeCell<Metadata>() {

                    protected void updateItem(Metadata item, boolean empty) {
                        super.updateItem(item, empty);
                        if (isEmpty()) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            Boolean isFolder=false;
                            if(item.getClass()==FolderMetadata.class){
                                isFolder=true;
                            }
                            String path=item.getPathLower();
                            String name=item.getName();

                            ContextMenu contexMenu = new ContextMenu();

                            if(isFolder){
                                MenuItem deleteFolder = new MenuItem("Delete folder");
                                contexMenu.getItems().add(deleteFolder);
                                deleteFolder.setOnAction(new EventHandler() {
                                    public void handle(Event t) {

                                        DropBoxService.deleteFile(path);
                                        TreeItem treeItem=treeView.getSelectionModel().getSelectedItem();
                                        treeItem.getParent().getChildren().remove(treeItem);
                                    }
                                });

                                //Download folder
                                MenuItem downloadFolder = new MenuItem("Download Folder");
                                contexMenu.getItems().add(downloadFolder);
                                downloadFolder.setOnAction(new EventHandler() {
                                    public void handle(Event t) {

                                        String downloadPath=DropBoxService.getFolder();

                                        try {
                                            DropBoxService.downloadFolder(item.getPathLower(),downloadPath);
                                        } catch (DbxException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                            if(!isFolder){
                                //Delete file
                                MenuItem deleteFile = new MenuItem("Delete File");
                                contexMenu.getItems().add(deleteFile);
                                deleteFile.setOnAction(new EventHandler() {
                                    public void handle(Event t) {

                                        DropBoxService.deleteFile(path);
                                        TreeItem treeItem=treeView.getSelectionModel().getSelectedItem();
                                        treeItem.getParent().getChildren().remove(treeItem);
                                    }
                                });
                                //Download file
                                MenuItem downloadFile = new MenuItem("Download File");
                                contexMenu.getItems().add(downloadFile);
                                downloadFile.setOnAction(new EventHandler() {
                                    public void handle(Event t) {

                                        String downloadPath=DropBoxService.getFolder();
                                        DbxDownloader<FileMetadata> downloader = null;
                                        try {
                                            downloader = client.files().download(path);
                                        } catch (DbxException e) {
                                            e.printStackTrace();
                                        }
                                        if(downloadPath!=null){
                                            try {
                                                FileOutputStream out = new FileOutputStream(downloadPath+item.getPathLower());
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
                                });
                            }
                            //Move File
                            MenuItem moveFile = new MenuItem("Move file");
                            contexMenu.getItems().add(moveFile);
                            moveFile.setOnAction(new EventHandler() {
                                public void handle(Event t) {

                                    FXMLLoader fxmlLoader = new FXMLLoader();
                                    fxmlLoader.setLocation(getClass().getClassLoader().getResource("MoveWIndow.fxml"));

                                    Group group=new Group();
                                    Label mainText=new Label("Move from: ");
                                    Label fromFile=new Label();
                                    fromFile.disableProperty();
                                    fromFile.setText(path);
                                    HBox cell=new HBox(mainText,fromFile);
                                    group.getChildren().add(cell);
                                    try {
                                        group.getChildren().add(fxmlLoader.load());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    Scene scene = new Scene(group);

                                    Stage stage = new Stage();
                                    stage.setTitle("Move file");
                                    stage.setScene(scene);
                                    stage.show();
                                }
                            });
                            //Copy File
                            MenuItem copyFile = new MenuItem("Copy file");
                            contexMenu.getItems().add(copyFile);
                            copyFile.setOnAction(new EventHandler() {
                                public void handle(Event t) {

                                    FXMLLoader fxmlLoader = new FXMLLoader();
                                    fxmlLoader.setLocation(getClass().getClassLoader().getResource("CopyWindow.fxml"));

                                    Group group=new Group();
                                    Label mainText=new Label("Copy from: ");
                                    Label fromFile=new Label();
                                    fromFile.disableProperty();
                                    fromFile.setText(path);
                                    HBox cell=new HBox(mainText,fromFile);
                                    group.getChildren().add(cell);
                                    try {
                                        group.getChildren().add(fxmlLoader.load());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    Scene scene = new Scene(group);

                                    Stage stage = new Stage();
                                    stage.setTitle("Copy file");
                                    stage.setScene(scene);
                                    stage.show();
                                }
                            });


                            HBox cellBox = new HBox();

                            TextField textField = new TextField(name);
                            textField.focusedProperty().addListener((ov, oldV, newV) -> {
                                textField.setOnKeyReleased(keyEvent -> {
                                    if(keyEvent.getCode()== KeyCode.ENTER){
                                        DropBoxService.renameFile(item.getPathLower(),textField.textProperty().getValueSafe());
                                        displayTreeView(currentFolder);
                                    }
                                });
                            });
                            cellBox.getChildren().add(textField);

                            if(item.getClass()== FileMetadata.class){
                                Date datum=((FileMetadata) item).getClientModified();
                                Label date=new Label(datum.toString());
                                cellBox.getChildren().add(date);
                            }

                            // We set the cellBox as the graphic of the cell.
                            setGraphic(cellBox);
                            setContextMenu(contexMenu);
                        }
                    }
                };
                treeCell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if(treeCell.getTreeItem()!=null){
                            Metadata item=treeCell.getTreeItem().getValue();
                            if (event.getClickCount()==2&&item.getClass()==FolderMetadata.class) {
                                String backFolder=currentFolder;
                                currentFolder=item.getPathLower();
                                displayTreeView(currentFolder);
                                Button goBack;
                                if(backFolder==""){
                                    goBack=new Button("Go back to: "+"root");
                                }else{
                                    goBack=new Button("Go back to: "+backFolder);
                                }

                                EventHandler<ActionEvent> goBackEvent = new EventHandler<ActionEvent>() {
                                    public void handle(ActionEvent e)
                                    {
                                        displayTreeView(backFolder);
                                        currentFolder=backFolder;
                                        for(int i=0;i<vBox.getChildren().size();i++){
                                            if(vBox.getChildren().get(i)==e.getSource()){
                                                vBox.getChildren().remove(i,vBox.getChildren().size());
                                            }
                                        }
                                    }
                                };
                                goBack.setOnAction(goBackEvent);
                                vBox.getChildren().add(goBack);
                            }
                        }
                    }
                });

                return treeCell;
            }
        });
        // Get a list of files.
        ListFolderResult result = null;
        try {
            result = client.files().listFolder(inputDirectoryLocation);
        } catch (DbxException e) {
            e.printStackTrace();
        }

        // create tree
        for (Metadata metadata : result.getEntries()){
            try {
                createTree(metadata, rootItem);
            } catch (DbxException e) {
                e.printStackTrace();
            }
        }

        treeView.setRoot(rootItem);
    }

    @FXML
    private void uploadFile(){
        String filePath=DropBoxService.getFile();
        if(filePath==null){
            return;
        }
        DropBoxService.uploadFile(filePath);
        displayTreeView(currentFolder);
    }

    @FXML
    private void uploadFolder(){
        String folderPath=DropBoxService.getFolder();
        if(folderPath==null){
            return;
        }
        DropBoxService.uploadFolder(folderPath);
        displayTreeView(currentFolder);
    }

    @FXML
    private void createFolder(){
        DropBoxService.createFolder(currentFolder,FolderName);
    }

    @FXML
    private void refresPage(){
        displayTreeView(currentFolder);
    }

    @FXML
    private void setAutoFolder(){


        AutoFolder autoFolder=new AutoFolder(Path.of(DropBoxService.getFolder()));

        Timer t=new Timer();
        t.schedule(autoFolder,1000);

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    displayTreeView(currentFolder);
                });
            }
        }, 1000, 15000);
    }
}
