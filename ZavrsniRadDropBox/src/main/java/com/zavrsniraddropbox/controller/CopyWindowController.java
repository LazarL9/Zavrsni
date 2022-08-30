package com.zavrsniraddropbox.controller;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.zavrsniraddropbox.service.DropBoxService;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;

public class CopyWindowController implements Initializable {

    @FXML
    private TreeView treeView;

    private String fromPath;
    private String toPath="";

    @FXML
    private void buttonClick(){
      Group group= (Group) treeView.getParent().getParent().getParent();
      HBox cell= (HBox) group.getChildren().get(0);
      Label fromPathL= (Label) cell.getChildren().get(1);
      fromPath=fromPathL.textProperty().getValue();
          DropBoxService.copyFile(fromPath,toPath);
          Stage stage = (Stage) treeView.getScene().getWindow();
          stage.close();

    }

    @FXML
    private void cancelClick(){
        Stage stage = (Stage) treeView.getScene().getWindow();
        stage.close();
    }

    private DbxClientV2 client=DropBoxService.getClient();

    public void createTree(Metadata metadata, TreeItem<Metadata> parent) throws DbxException {
        if (metadata.getClass()== FolderMetadata.class) {
            TreeItem<Metadata> treeItem = new TreeItem<>(metadata);
            parent.getChildren().add(treeItem);
            ListFolderResult result = client.files().listFolder(metadata.getPathLower());
            for (Metadata f : result.getEntries()) {
                createTree(f, treeItem);
            }
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

                            String name=item.getName();
                            setText(name);
                        }
                    }
                };
                treeCell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if(treeCell.getTreeItem()!=null){
                            Metadata item=treeCell.getTreeItem().getValue();

                            toPath=item.getPathLower();
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        displayTreeView("");
    }
}
