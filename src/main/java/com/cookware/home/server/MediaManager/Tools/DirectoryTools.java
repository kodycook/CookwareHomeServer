package com.cookware.home.server.MediaManager.Tools;

import org.apache.log4j.Logger;

import java.io.File;

/**
 * Created by Kody on 19/09/2017.
 */
public class DirectoryTools {
    private static final Logger log = Logger.getLogger(DirectoryTools.class);

    public File[] getImmediateFilesInDirectory(String directoryPath){
        File folder = new File(directoryPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                log.debug("File " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                log.debug("Directory " + listOfFiles[i].getName());
            }
        }
        return listOfFiles;
    }


    //TODO: Finish Implementing this (getAllFilesInDirectory_Recursive) method
    public String[] getAllFilesInDirectory_Recursive(String DirectoryPath){
        String[] listOfFiles = {};

        return listOfFiles;
    }


    public boolean checkIfNetworkLocationAvailable(String path){
        File file = new File(path);
        if((file.isDirectory())&&(file.exists())){
            return true;
        }
        else {
            return false;
        }
    }


    public boolean checkIfPathExists(String directoryName){
        File directory = new File(directoryName);
        return directory.exists();
    }

    public boolean createNewDirectory(String directoryName){
        if(checkIfPathExists(directoryName)){
            return true;
        }
        try{
            log.info(String.format("Created new Directory: %s",directoryName));
            new File(directoryName).mkdir();
            return true;
        }
        catch(Exception e){
            log.error(e);
            return false;
        }
    }
}
