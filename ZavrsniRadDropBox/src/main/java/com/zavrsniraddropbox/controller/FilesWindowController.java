package com.zavrsniraddropbox.controller;

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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class FilesWindowController implements Initializable {

    @FXML
    private TreeView<Metadata> directoryView;

    @FXML
    private TreeView<Metadata> treeView;

    @FXML
    private VBox vBox;

    @FXML
    private MenuWindowController menuBarController;

    @FXML
    private TextField searchField;

    @FXML
    private Label labelNaziv;

    @FXML
    private Label labelModif;

    @FXML
    private DropBoxService progressBarController;

    private DbxClientV2 client= DropBoxService.getClient();
    private String currentFolder="";
    private String FolderName="New Folder";
    private Boolean isSearch=false;
    private String searchText=null;

    private Boolean isSortedNaz =false;
    private Boolean isReversedNaz =true;

    private Boolean isSortedMod=false;
    private Boolean isReversedMod=true;

    public void createTree(Metadata metadata, TreeItem<Metadata> parent) throws DbxException {
        if (metadata.getClass()== FolderMetadata.class) {
            TreeItem<Metadata> treeItem = new TreeItem<>(metadata);
            parent.getChildren().add(treeItem);
            ListFolderResult result = client.files().listFolder(metadata.getPathLower());
            for (Metadata f : result.getEntries()) {
                //createTree(f, treeItem);
            }
        } else{
            parent.getChildren().add(new TreeItem<>(metadata));
        }
    }

    public void createTreeSide(Metadata metadata, TreeItem<Metadata> parent) throws DbxException {
        if (metadata.getClass()== FolderMetadata.class) {
            TreeItem<Metadata> treeItem = new TreeItem<>(metadata);
            parent.getChildren().add(treeItem);
            ListFolderResult result = client.files().listFolder(metadata.getPathLower());
            for (Metadata f : result.getEntries()) {
                createTreeSide(f, treeItem);
            }
        }
    }

    public void createTreeSearch(Metadata metadata, TreeItem<Metadata> parent) throws DbxException {
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
                                        refreshPage();
                                        displayTreeViewSide("");
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
                                        DropBoxService.downloadFile(item.getPathLower(),downloadPath);

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
                            Image slika = null;
                            ImageView openView;
                            if(isFolder){
                                try {
                                    slika = new Image(new FileInputStream("src/main/resources/icon/folder.jpg"));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                openView = new ImageView(slika);
                                openView.setFitWidth(15);
                                openView.setFitHeight(15);
                            }else{
                                try {
                                    slika = new Image(new FileInputStream("src/main/resources/icon/file.jpg"));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                openView = new ImageView(slika);
                                openView.setFitWidth(15);
                                openView.setFitHeight(15);
                            }
                            cellBox.getChildren().add(openView);


                            TextField textField = new TextField(name);
                            textField.focusedProperty().addListener((ov, oldV, newV) -> {
                                textField.setOnKeyReleased(keyEvent -> {
                                    if(keyEvent.getCode()== KeyCode.ENTER){
                                        DropBoxService.renameFile(item.getPathLower(),textField.textProperty().getValueSafe());
                                        refreshPage();
                                    }
                                });
                            });
                            cellBox.getChildren().add(textField);

                            if(item.getClass()== FileMetadata.class){
                                Date datum=((FileMetadata) item).getClientModified();
                                SimpleDateFormat newFormat =new SimpleDateFormat("dd-MM-yyyy hh:mm");
                                Label date=new Label(newFormat.format(datum).toString());
                                cellBox.getChildren().add(date);
                            }
                            cellBox.setAlignment(Pos.CENTER_LEFT);


                            // We set the cellBox as the graphic of the cell.
                            setGraphic(cellBox);
                            setContextMenu(contexMenu);
                        }
                    }
                };
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

        if(isSortedNaz){
            if(!isReversedNaz){
                ArrayList<TreeItem<Metadata>> lista=new ArrayList<>();
                lista.addAll(treeView.getRoot().getChildren());
                lista.sort(Comparator.comparing(t->t.getValue().getName()));
                TreeItem<Metadata> treeItem=new TreeItem<>();
                treeItem.getChildren().addAll(lista);
                treeView.setRoot(treeItem);
            }else if(isReversedNaz){
                ArrayList<TreeItem<Metadata>> lista=new ArrayList<>();
                lista.addAll(treeView.getRoot().getChildren());
                lista.sort(Comparator.comparing(t->t.getValue().getName()));
                Collections.reverse(lista);
                TreeItem<Metadata> treeItem=new TreeItem<>();
                treeItem.getChildren().addAll(lista);
                treeView.setRoot(treeItem);
            }
        }else if(isSortedMod){
            if(!isReversedMod){
                ArrayList<TreeItem<Metadata>> lista=new ArrayList<>();
                lista.addAll(treeView.getRoot().getChildren());
                Collections.sort(lista, new Comparator<TreeItem<Metadata>>() {
                    @Override
                    public int compare(TreeItem<Metadata> o1, TreeItem<Metadata> o2) {
                        if(o1.getValue().getClass()==FileMetadata.class&&o2.getValue().getClass()==FileMetadata.class){
                            FileMetadata f1= (FileMetadata) o1.getValue();
                            FileMetadata f2= (FileMetadata) o2.getValue();
                            if(f1.getClientModified().getTime()<f2.getClientModified().getTime()){
                                return -1;
                            }else if(f1.getClientModified().getTime()==f2.getClientModified().getTime()){
                                return 0;
                            }else {
                                return 1;
                            }
                        }else{
                            return -2;
                        }
                    }
                });
                TreeItem<Metadata> treeItem=new TreeItem<>();
                treeItem.getChildren().addAll(lista);
                treeView.setRoot(treeItem);
            }else if(isReversedMod){
                ArrayList<TreeItem<Metadata>> lista=new ArrayList<>();
                lista.addAll(treeView.getRoot().getChildren());
                Collections.sort(lista, new Comparator<TreeItem<Metadata>>() {
                    @Override
                    public int compare(TreeItem<Metadata> o1, TreeItem<Metadata> o2) {
                        if(o1.getValue().getClass()==FileMetadata.class&&o2.getValue().getClass()==FileMetadata.class){
                            FileMetadata f1= (FileMetadata) o1.getValue();
                            FileMetadata f2= (FileMetadata) o2.getValue();
                            if(f1.getClientModified().getTime()<f2.getClientModified().getTime()){
                                return 1;
                            }else if(f1.getClientModified().getTime()==f2.getClientModified().getTime()){
                                return 0;
                            }else {
                                return -1;
                            }
                        }else{
                            return -2;
                        }
                    }
                });
                TreeItem<Metadata> treeItem=new TreeItem<>();
                treeItem.getChildren().addAll(lista);
                treeView.setRoot(treeItem);
            }
        }
    }

    public void displayTreeViewSide(String inputDirectoryLocation){
        // Creates the root item.
        TreeItem<Metadata> rootItem = new TreeItem<>();
        // Hides the root item of the tree view.
        directoryView.setShowRoot(false);
        // Creates the cell factory.

        directoryView.setCellFactory(new Callback<TreeView<Metadata>, TreeCell<Metadata>>() {
            @Override
            public TreeCell<Metadata> call(TreeView<Metadata> metadataTreeView) {
                TreeCell<Metadata> treeCell = new TreeCell<Metadata>() {

                    protected void updateItem(Metadata item, boolean empty) {
                        super.updateItem(item, empty);
                        if (isEmpty()) {
                            setGraphic(null);
                            setText(null);
                        } else {

                            String name=item.getName();
                            Image slika = null;
                            ImageView openView;
                                try {
                                    slika = new Image(new FileInputStream("src/main/resources/icon/folder.jpg"));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                openView = new ImageView(slika);
                                openView.setFitWidth(15);
                                openView.setFitHeight(15);

                            setGraphic(openView);
                            setText(name);
                        }
                    }
                };
                treeCell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if(treeCell.getTreeItem()!=null){
                            Metadata item=treeCell.getTreeItem().getValue();
                            if (event.getClickCount()==2&&item.getClass()==FolderMetadata.class) {

                                String fullPath=item.getPathLower();
                                ArrayList<String> array=new ArrayList<>();
                                do{
                                    array.add(fullPath.substring(0,fullPath.lastIndexOf("/")));
                                    fullPath=fullPath.substring(0,fullPath.lastIndexOf("/"));
                                }while (fullPath!="");
                                Collections.reverse(array);

                                currentFolder=item.getPathLower();
                                displayTreeView(currentFolder);

                                HBox gumboviNatrag=new HBox();
                                gumboviNatrag.setAlignment(Pos.CENTER_LEFT);
                                for(int i=0;i< array.size();i++){
                                    Hyperlink goBack;
                                    String pathName;
                                    String pathFull=array.get(i);
                                    if(array.get(i)!=""){
                                        pathName=array.get(i).substring(array.get(i).lastIndexOf("/")+1);
                                    }else {
                                        pathName=array.get(i);
                                    }
                                    System.out.println(pathName);
                                    if(pathName==""){
                                        goBack=new Hyperlink("root");
                                    }else{
                                        goBack=new Hyperlink(pathName);
                                    }

                                    EventHandler<ActionEvent> goBackEvent = new EventHandler<ActionEvent>() {
                                        public void handle(ActionEvent e)
                                        {
                                            displayTreeView(pathFull);
                                            currentFolder=pathFull;
                                            for(int i=0;i<gumboviNatrag.getChildren().size();i++){
                                                if(gumboviNatrag.getChildren().get(i)==e.getSource()){
                                                    gumboviNatrag.getChildren().remove(i,gumboviNatrag.getChildren().size());
                                                }
                                            }
                                            if(gumboviNatrag.getChildren().isEmpty()){
                                                gumboviNatrag.setPadding(new Insets(4));
                                                gumboviNatrag.getChildren().add(new Label("root"));
                                            }
                                        }
                                    };
                                    goBack.setOnAction(goBackEvent);

                                    gumboviNatrag.getChildren().add(goBack);
                                    //if(i!=array.size()-1){
                                        gumboviNatrag.getChildren().add(new Label(">"));
                                    //}
                                    if(i== array.size()-1){
                                        gumboviNatrag.getChildren().add(new Label(item.getName()));
                                    }

                                }
                                if(vBox.getChildren().size()>1){
                                    vBox.getChildren().remove(1,vBox.getChildren().size());
                                }
                                vBox.getChildren().add(gumboviNatrag);
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
                createTreeSide(metadata, rootItem);
            } catch (DbxException e) {
                e.printStackTrace();
            }
        }
        directoryView.setRoot(rootItem);
    }

    public void displayTreeViewSearch(String inputDirectoryLocation){
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
                                        refreshPage();
                                        displayTreeViewSide("");
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
                                        DropBoxService.downloadFile(item.getPathLower(),downloadPath);

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
                            Image slika = null;
                            ImageView openView;
                            if(isFolder){
                                try {
                                    slika = new Image(new FileInputStream("src/main/resources/icon/folder.jpg"));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                openView = new ImageView(slika);
                                openView.setFitWidth(15);
                                openView.setFitHeight(15);
                            }else{
                                try {
                                    slika = new Image(new FileInputStream("src/main/resources/icon/file.jpg"));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                openView = new ImageView(slika);
                                openView.setFitWidth(15);
                                openView.setFitHeight(15);
                            }
                            cellBox.getChildren().add(openView);


                            TextField textField = new TextField(name);
                            textField.focusedProperty().addListener((ov, oldV, newV) -> {
                                textField.setOnKeyReleased(keyEvent -> {
                                    if(keyEvent.getCode()== KeyCode.ENTER){
                                        DropBoxService.renameFile(item.getPathLower(),textField.textProperty().getValueSafe());
                                        refreshPage();
                                    }
                                });
                            });
                            cellBox.getChildren().add(textField);

                            if(item.getClass()== FileMetadata.class){
                                Date datum=((FileMetadata) item).getClientModified();
                                SimpleDateFormat newFormat =new SimpleDateFormat("dd-MM-yyyy hh:mm");
                                Label date=new Label(newFormat.format(datum));
                                cellBox.getChildren().add(date);
                            }
                            cellBox.setAlignment(Pos.CENTER_LEFT);


                            // We set the cellBox as the graphic of the cell.
                            setGraphic(cellBox);
                            setContextMenu(contexMenu);
                        }
                    }
                };
                return treeCell;
            }
        });
        // Get a list of files.
        SearchV2Result result = null;
        try {
            result = client.files().searchV2(searchText);
        } catch (DbxException e) {
            e.printStackTrace();
        }

        // create tree
        for (SearchMatchV2 metadata : result.getMatches()){
            try {
                createTreeSearch(metadata.getMetadata().getMetadataValue(), rootItem);
            } catch (DbxException e) {
                e.printStackTrace();
            }
        }

        treeView.setRoot(rootItem);

        if(isSortedNaz){
            if(!isReversedNaz){
                ArrayList<TreeItem<Metadata>> lista=new ArrayList<>();
                lista.addAll(treeView.getRoot().getChildren());
                lista.sort(Comparator.comparing(t->t.getValue().getName()));
                TreeItem<Metadata> treeItem=new TreeItem<>();
                treeItem.getChildren().addAll(lista);
                treeView.setRoot(treeItem);
            }else if(isReversedNaz){
                ArrayList<TreeItem<Metadata>> lista=new ArrayList<>();
                lista.addAll(treeView.getRoot().getChildren());
                lista.sort(Comparator.comparing(t->t.getValue().getName()));
                Collections.reverse(lista);
                TreeItem<Metadata> treeItem=new TreeItem<>();
                treeItem.getChildren().addAll(lista);
                treeView.setRoot(treeItem);
            }
        }else if(isSortedMod) {
            if (!isReversedMod) {
                ArrayList<TreeItem<Metadata>> lista = new ArrayList<>();
                lista.addAll(treeView.getRoot().getChildren());
                Collections.sort(lista, new Comparator<TreeItem<Metadata>>() {
                    @Override
                    public int compare(TreeItem<Metadata> o1, TreeItem<Metadata> o2) {
                        if (o1.getValue().getClass() == FileMetadata.class && o2.getValue().getClass() == FileMetadata.class) {
                            FileMetadata f1 = (FileMetadata) o1.getValue();
                            FileMetadata f2 = (FileMetadata) o2.getValue();
                            if (f1.getClientModified().getTime() < f2.getClientModified().getTime()) {
                                return -1;
                            } else if (f1.getClientModified().getTime() == f2.getClientModified().getTime()) {
                                return 0;
                            } else {
                                return 1;
                            }
                        } else {
                            return -2;
                        }
                    }
                });
                TreeItem<Metadata> treeItem = new TreeItem<>();
                treeItem.getChildren().addAll(lista);
                treeView.setRoot(treeItem);
            } else if (isReversedMod) {
                ArrayList<TreeItem<Metadata>> lista = new ArrayList<>();
                lista.addAll(treeView.getRoot().getChildren());
                Collections.sort(lista, new Comparator<TreeItem<Metadata>>() {
                    @Override
                    public int compare(TreeItem<Metadata> o1, TreeItem<Metadata> o2) {
                        if (o1.getValue().getClass() == FileMetadata.class && o2.getValue().getClass() == FileMetadata.class) {
                            FileMetadata f1 = (FileMetadata) o1.getValue();
                            FileMetadata f2 = (FileMetadata) o2.getValue();
                            if (f1.getClientModified().getTime() < f2.getClientModified().getTime()) {
                                return 1;
                            } else if (f1.getClientModified().getTime() == f2.getClientModified().getTime()) {
                                return 0;
                            } else {
                                return -1;
                            }
                        } else {
                            return -2;
                        }
                    }
                });
                TreeItem<Metadata> treeItem = new TreeItem<>();
                treeItem.getChildren().addAll(lista);
                treeView.setRoot(treeItem);
            }
        }
    }

    @FXML
    private void uploadFile(){
        String filePath=DropBoxService.getFile();
        if(filePath==null){
            return;
        }
        DropBoxService.uploadFile(filePath);
        refreshPage();
    }

    @FXML
    private void uploadFolder(){
        String folderPath=DropBoxService.getFolder();
        if(folderPath==null){
            return;
        }
        progressBarController.uploadFolder(folderPath);
        refreshPage();
    }

    @FXML
    private void createFolder(){
        DropBoxService.createFolder(currentFolder,FolderName);
    }

    @FXML
    private void refresPage(){
        refreshPage();
    }

    @FXML
    private void setAutoFolder(){


        AutoFolder autoFolder=new AutoFolder(Path.of(DropBoxService.getFolder()));

        Timer t=new Timer();
        t.schedule(autoFolder,1000);
    }

    private void refreshPage(){
        if(isSearch){
            displayTreeViewSearch(searchText);
        }else {
            displayTreeView(currentFolder);
        }
        displayTreeViewSide("");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            menuBarController.setKorisnikLabel(client.users().getCurrentAccount().getName().getDisplayName());
        } catch (DbxException e) {
            e.printStackTrace();
        }
        menuBarController.setDisableF();
        displayTreeViewSide("");

        //RefreshPage
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    refreshPage();
                    displayTreeViewSide("");
                });
            }
        }, 1000, 45000);

        //Search
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.isEmpty()||newValue==""){
                isSearch=false;
                searchText="";
                refreshPage();
            }else {
                searchField.setOnKeyReleased(keyEvent -> {
                    if(keyEvent.getCode()== KeyCode.ENTER){
                        isSearch=true;
                        searchText=newValue;
                        refreshPage();
                    }
                });
            }
        });

        //SortNaz
        labelNaziv.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String naziv=labelNaziv.textProperty().getValue();
                isReversedMod=true;
                isSortedMod=false;
                labelModif.textProperty().setValue(labelModif.textProperty().getValue().substring(0,12));
                if(!isSortedNaz){
                    isSortedNaz =true;
                    refreshPage();
                }
                if(!isReversedNaz){
                    if(naziv.length()>5){
                        naziv=naziv.substring(0,5);
                        naziv=naziv+"↓";
                        labelNaziv.textProperty().setValue(naziv);
                    }else {
                        naziv=naziv+"↓";
                        labelNaziv.textProperty().setValue(naziv);
                    }
                    isReversedNaz =true;
                    refreshPage();
                }else{
                    if(naziv.length()>5){
                        naziv=naziv.substring(0,5);
                        naziv=naziv+"↑";
                        labelNaziv.textProperty().setValue(naziv);
                    }else {
                        naziv=naziv+"↑";
                        labelNaziv.textProperty().setValue(naziv);
                    }
                    isReversedNaz =false;
                    refreshPage();
                }
            }
        });
        //SortMod
        labelModif.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String modif=labelModif.textProperty().getValue();
                isReversedNaz=true;
                isSortedNaz=false;
                labelNaziv.textProperty().setValue(labelNaziv.textProperty().getValue().substring(0,5));
                if(!isSortedMod){
                    isSortedMod =true;
                    refreshPage();
                }
                if(!isReversedMod){
                    if(modif.length()>12){
                        modif=modif.substring(0,12);
                        modif=modif+"↓";
                        labelModif.textProperty().setValue(modif);
                    }else {
                        modif=modif+"↓";
                        labelModif.textProperty().setValue(modif);
                    }
                    isReversedMod =true;
                    refreshPage();
                }else{
                    if(modif.length()>12){
                        modif=modif.substring(0,12);
                        modif=modif+"↑";
                        labelModif.textProperty().setValue(modif);
                    }else {
                        modif=modif+"↑";
                        labelModif.textProperty().setValue(modif);
                    }
                    isReversedMod =false;
                    refreshPage();
                }
            }
        });
    }
}
