package com.cookware.home.server.MediaManager;

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

    public String[] getAllFilesInDirectory_Recursive(String DirectoryPath){
        String[] listOfFiles = {};
        //TODO: Finish Implementing this (getAllFilesInDirectory_Recursive) method

        return listOfFiles;
    }

    public boolean checkIfNetworkLocationAvailable(Object ojb){
        // TODO: Finish implementing this (checkIfNetworkLocationAvailable) method
        log.warn("USING AN UNFINISHED METHOD");
        return true;
    }

    public boolean checkIfdirectorityexists (String directoryName){
        File directory = new File(directoryName);
        return directory.exists();
    }

    public boolean createNewDirectory(String directoryName){
        log.debug(directoryName);
        if(checkIfdirectorityexists(directoryName)){
            return true;
        }
        try{
            new File(directoryName).mkdir();
            return true;
        }
        catch(Exception e){
            log.error(e);
            return false;
        }
    }
}
