package com.cookware.home.server.MediaManager;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Created by Kody on 19/09/2017.
 */
public class FileTransferrerRunnable implements Runnable {
    private static final Logger log = Logger.getLogger(FileTransferrerRunnable.class);
    private final DirectoryTools directoryTools = new DirectoryTools();
    private final FileNameTools fileNameTools = new FileNameTools();
    private DatabaseManager databaseManager;

    public FileTransferrerRunnable(DatabaseManager mDatabaseManager){
        this.databaseManager = mDatabaseManager;
    }

    @Override
    public void run(){
        File[] listOfFiles;
        String currentFileName;
        BigInteger currentFileId;
        MediaInfo currentMediaInfo;

        while(true){
            if(mediaStorageAvailable()){
                listOfFiles = directoryTools.getImmediateFilesInDirectory(MediaManager.tempPath);
                for(File currentFile:listOfFiles){
                    if (currentFile.isFile()) {
                        currentFileName = currentFile.getName();
                        currentFileId = fileNameTools.generateHashFromFullFileName(currentFileName);
                        currentMediaInfo = databaseManager.getMediaItemWithMatchedId(currentFileId);
                        if(currentMediaInfo != null){
                            if (currentMediaInfo.STATE.equals(DownloadState.DOWNLOADING)) {
                                continue;
                            }
                            else if (currentMediaInfo.STATE.equals(DownloadState.TRANSFERRING)) {
                                if(currentMediaInfo.PATH == null){
                                    log.warn("This should never be reached - media entries should have a path once downloaded");
                                    currentMediaInfo.PATH = currentFileName;
                                }
                                transferMedia(currentMediaInfo);
                                databaseManager.updateState(currentMediaInfo.ID, DownloadState.FINISHED);
                            }
                            else if (currentMediaInfo.STATE.equals(DownloadState.IGNORED)){
                                currentFile.delete();
                            }
                        }
                        else {
                            log.error(String.format("Could not match \"%s\" media file to entry in database - moved to \"unidentified\" folder",currentFileName));
                            transferUnidentifiedFile(currentFileName);
                        }
                    }
                }
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                log.error("MediaManager thread interrupted", e);
                System.exit(-1);
            }
        }
    }

    public boolean mediaStorageAvailable(){
        return directoryTools.checkIfNetworkLocationAvailable(MediaManager.finalPath);
    }

    private void transferMedia(MediaInfo mediaInfo){
        File sourceFile = null;
        File destinationFile = null;
        String localDirectory;
        String oldFullFileName = "";
        String newFullFileName = "";
        boolean moveSuccess = false;
        try{
            if (mediaInfo.TYPE.equals(MediaType.MOVIE)) {
                oldFullFileName = MediaManager.tempPath + "\\" + mediaInfo.PATH;
                sourceFile = new File(oldFullFileName);

                localDirectory = "\\Movies\\" + fileNameTools.getFullFileNameFromMediaInfo(mediaInfo);
                directoryTools.createNewDirectory(MediaManager.finalPath + localDirectory);
                mediaInfo.PATH = localDirectory + "\\" + mediaInfo.PATH;
                newFullFileName = MediaManager.finalPath + mediaInfo.PATH;

                destinationFile = new File(newFullFileName);
            }
            else if (mediaInfo.TYPE.equals(MediaType.EPISODE)){
                oldFullFileName = MediaManager.tempPath + "\\" + mediaInfo.PATH;
                sourceFile = new File(oldFullFileName);

                localDirectory = "\\TV Shows\\" + fileNameTools.getFullFileNameFromMediaInfo(databaseManager.getMediaItemWithMatchedId(mediaInfo.PARENTSHOWID));
                directoryTools.createNewDirectory(MediaManager.finalPath + localDirectory);
                localDirectory += String.format("\\Season %d",mediaInfo.getSeason());
                directoryTools.createNewDirectory(MediaManager.finalPath + localDirectory);
                mediaInfo.PATH = localDirectory + "\\" + mediaInfo.PATH;
                newFullFileName = MediaManager.finalPath + mediaInfo.PATH;

                destinationFile = new File(newFullFileName);
            }
            else {
                log.error("TV shows should never have physical objects");
                return;
            }

            if(destinationFile.exists()){
                log.warn(String.format("File: %s already exists in the destination", mediaInfo.NAME));
                destinationFile.delete();
            }

            FileUtils.moveFile(sourceFile, destinationFile);

            log.info(String.format("Moved %s: %s -> %s",mediaInfo.NAME ,oldFullFileName,newFullFileName));

            databaseManager.updateState(mediaInfo.ID, DownloadState.FINISHED);
            databaseManager.updatePath(mediaInfo.ID, mediaInfo.PATH);

        }catch(Exception e){
            log.error(e);
        }
    }

    private void transferUnidentifiedFile(String fileName){
        // WILL IS THE APPROACH I'VE TAKEN IN THIS METHOD POOR IMPLEMENTATION?
        String path = MediaManager.tempPath;
        directoryTools.createNewDirectory(path);
        File sourceFile = new File(path + "\\" + fileName);

        path += "\\Unidentified";
        File destinationFile = new File(path + "\\" + fileName);

        try {
            FileUtils.moveFile(sourceFile, destinationFile);
        } catch (IOException e) {
            log.error("Unidentified file failed to move", e);
        }
    }

}
