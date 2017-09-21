package com.cookware.home.server.MediaManager;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
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
                listOfFiles = directoryTools.getImmediateFilesInDirectory(MediaManagerRunnable.tempPath);
                for(File currentFile:listOfFiles){
                    if (currentFile.isFile()) {
                        currentFileName = currentFile.getName();
                        currentFileId = fileNameTools.generateHashFromFullFileName(currentFileName);
                        currentMediaInfo = databaseManager.getMediaItem(currentFileId);
                        if(currentMediaInfo != null){
                            if(currentMediaInfo.PATH == null){
                                log.warn("This should never be reached - media entries should have a path once downloaded");
                                currentMediaInfo.PATH = currentFileName;
                            }
                            if (currentMediaInfo.STATE.equals(DownloadState.TRANSFERRING)) {
                                transferMedia(currentMediaInfo);
                            }
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

    private boolean mediaStorageAvailable(){
        // TODO: Finish implementing this (mediaStorageAvailable) method
        log.debug("USING AN UNFINISHED METHOD");
        return true;
    }

    private void transferMedia(MediaInfo mediaInfo){
        File sourceFile = null;
        File destinationFile = null;
        String localDirectory;
        String oldFullFileName = "";
        String newFullFileName = "";
        // TODO: Finish implementing this (transferMedia) method
        boolean moveSuccess = false;
        try{
            if (mediaInfo.TYPE.equals(MediaType.MOVIE)) {
                oldFullFileName = MediaManagerRunnable.tempPath + "\\" + mediaInfo.PATH;
                sourceFile = new File(oldFullFileName);

                localDirectory = fileNameTools.getFullFileNameFromMediaInfo(mediaInfo);

                directoryTools.createNewDirectory(MediaManagerRunnable.moviePath + "\\" + localDirectory);
                mediaInfo.PATH = "\\" + localDirectory + "\\" + mediaInfo.PATH;
                newFullFileName = MediaManagerRunnable.moviePath + mediaInfo.PATH;

                destinationFile = new File(newFullFileName);
            }
            else if (mediaInfo.TYPE.equals(MediaType.EPISODE)){
                // TODO: Finish writing this side of the function
                oldFullFileName = MediaManagerRunnable.tempPath + "\\" + mediaInfo.PATH;
                sourceFile = new File(oldFullFileName);

                localDirectory = fileNameTools.getFullFileNameFromMediaInfo(databaseManager.getMediaItem(mediaInfo.PARENTSHOWID));
                directoryTools.createNewDirectory(MediaManagerRunnable.episodePath + "\\" + localDirectory);
                localDirectory += String.format("\\Season %d",mediaInfo.getSeason());
                directoryTools.createNewDirectory(MediaManagerRunnable.episodePath + "\\" + localDirectory);
                mediaInfo.PATH = "\\" + localDirectory + "\\" + mediaInfo.PATH;
                newFullFileName = MediaManagerRunnable.moviePath + mediaInfo.PATH;

                destinationFile = new File(newFullFileName);
            }
            else {
                log.error("TV shows should never have physical objects");
                return;
            }

            FileUtils.moveFile(sourceFile, destinationFile);

            log.info(String.format("Moved %s: %s -> %s",mediaInfo.NAME ,oldFullFileName,newFullFileName));

            databaseManager.updateState(mediaInfo.ID, DownloadState.FINISHED);
            databaseManager.updatePath(mediaInfo.ID, mediaInfo.PATH);

        }catch(Exception e){
            log.error(e);
        }
    }

}
