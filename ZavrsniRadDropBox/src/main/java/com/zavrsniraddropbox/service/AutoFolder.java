package com.zavrsniraddropbox.service;

import com.zavrsniraddropbox.service.DropBoxService;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.TimerTask;

import static java.nio.file.StandardWatchEventKinds.*;

public class AutoFolder extends TimerTask {
    private WatchService watchService;
    private Path path;
    private WatchKey key;

    public AutoFolder(Path path) {
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.path = path;
        try {
            this.key = this.path.register(this.watchService,
                    ENTRY_CREATE,
                    ENTRY_DELETE,
                    ENTRY_MODIFY);
        } catch (IOException x) {
            x.printStackTrace();
        }
        DropBoxService.uploadFolder(String.valueOf(this.path));
    }

    public void run() {
        while (true) {
            try {
                if (!((this.key = this.watchService.take()) != null)) break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (WatchEvent<?> event : this.key.pollEvents()) {
                System.out.println(
                        "Event kind:" + event.kind()
                                + ". File affected: " + event.context() + ".");

                String file=this.path+"\\"+event.context();
                File file1=new File(file);
                String pathString=String.valueOf(this.path);
                pathString=pathString.substring(pathString.lastIndexOf('\\'));


                if(event.kind()==ENTRY_CREATE){
                    if(file1.isDirectory()){
                        pathString=pathString.replace("\\","/");
                        pathString=pathString.substring(pathString.lastIndexOf("/"));

                        DropBoxService.createFolder(pathString, String.valueOf(event.context()));
                    }else{
                        pathString=pathString.replace("\\","/");
                        DropBoxService.uploadFileAuto(file,pathString);
                    }

                }else if(event.kind()==ENTRY_MODIFY){
                    if(file1.isDirectory()){

                        pathString=pathString.replace("\\","/");
                        pathString=pathString.substring(pathString.lastIndexOf("/"));
                        DropBoxService.deleteFileAuto("/"+event.context(),pathString);
                        DropBoxService.uploadFolderAuto(file,pathString);
                    }else{
                        pathString=pathString.replace("\\","/");
                        pathString=pathString.substring(pathString.lastIndexOf("/"));
                        DropBoxService.deleteFileAuto("/"+event.context(),pathString);
                        DropBoxService.uploadFileAuto(file,pathString);
                    }
                }else{
                    pathString=pathString.replace("\\","/");
                    pathString=pathString.substring(pathString.lastIndexOf("/"));
                    DropBoxService.deleteFileAuto("/"+event.context(),pathString);
                }

            }
            this.key.reset();
        }
    }
}
