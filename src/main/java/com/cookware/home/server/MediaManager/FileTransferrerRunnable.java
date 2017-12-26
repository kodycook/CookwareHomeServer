package com.cookware.home.server.MediaManager;

import com.cookware.home.server.MediaManager.DataTypes.Config;
import com.cookware.home.server.MediaManager.DataTypes.DownloadState;
import com.cookware.home.server.MediaManager.DataTypes.MediaInfo;
import com.cookware.home.server.MediaManager.DataTypes.MediaType;
import com.cookware.home.server.MediaManager.Managers.DatabaseManager;
import com.cookware.home.server.MediaManager.Tools.DirectoryTools;
import com.cookware.home.server.MediaManager.Tools.FileNameTools;
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
    private final Config config;
    private final DirectoryTools directoryTools = new DirectoryTools();
    private final FileNameTools fileNameTools = new FileNameTools();
    private DatabaseManager databaseManager;


    public FileTransferrerRunnable(DatabaseManager mDatabaseManager, Config mConfig){
        this.config = mConfig;
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
                listOfFiles = directoryTools.getImmediateFilesInDirectory(config.tempPath);
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
                            }
                            else if (currentMediaInfo.STATE.equals(DownloadState.IGNORED)){
                                currentFile.delete();
                            }
                            else if (currentMediaInfo.STATE.equals(DownloadState.FAILED)){
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
                System.exit(1);
            }
        }
    }

    public boolean mediaStorageAvailable(){
        return directoryTools.checkIfNetworkLocationAvailable(config.finalPath);
    }

    private void transferMedia(MediaInfo mediaInfo){
        log.info(String.format("Starting Moving: %s", mediaInfo.toString()));
        File sourceFile = null;
        File destinationFile = null;
        String localDirectory;
        String oldFullFileName = "";
        String newFullFileName = "";
        boolean moveSuccess = false;
        try{
            if (mediaInfo.TYPE.equals(MediaType.MOVIE)) {
                oldFullFileName = config.tempPath + "/" + mediaInfo.PATH;
                sourceFile = new File(oldFullFileName);

                localDirectory = "/Movies/" + fileNameTools.getFullFileNameFromMediaInfo(mediaInfo);
                directoryTools.createNewDirectory(config.finalPath + localDirectory);
                mediaInfo.PATH = localDirectory + "/" + mediaInfo.PATH;
                newFullFileName = config.finalPath + mediaInfo.PATH;

                destinationFile = new File(newFullFileName);
            }
            else if (mediaInfo.TYPE.equals(MediaType.EPISODE)){
                oldFullFileName = config.tempPath + "/" + mediaInfo.PATH;
                sourceFile = new File(oldFullFileName);

                localDirectory = "/TV Shows/" + fileNameTools.getFullFileNameFromMediaInfo(databaseManager.getMediaItemWithMatchedId(mediaInfo.PARENTSHOWID));
                directoryTools.createNewDirectory(config.finalPath + localDirectory);
                localDirectory += String.format("/Season %d",mediaInfo.getSeason());
                directoryTools.createNewDirectory(config.finalPath + localDirectory);
                mediaInfo.PATH = localDirectory + "/" + mediaInfo.PATH;
                newFullFileName = config.finalPath + mediaInfo.PATH;

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

            try {
                FileUtils.moveFile(sourceFile, destinationFile);
            }
            catch (IOException e){
                log.error("Issue moving file to Media Storage");
                return;
            }

            log.info(String.format("Finished Moving %s: %s -> %s",mediaInfo.NAME ,oldFullFileName,newFullFileName));

            databaseManager.updateState(mediaInfo.ID, DownloadState.FINISHED);
            databaseManager.updatePath(mediaInfo.ID, mediaInfo.PATH);

        }catch(Exception e){
            log.error(String.format("Issue trying to move file: %s", mediaInfo.NAME),e);
            return;
        }
    }

    private void transferUnidentifiedFile(String fileName){
        // WILL IS THE APPROACH I'VE TAKEN IN THIS METHOD POOR IMPLEMENTATION?
        String path = config.tempPath;
        directoryTools.createNewDirectory(path);
        File sourceFile = new File(path + "/" + fileName);

        path += "/Unidentified";
        File destinationFile = new File(path + "/" + fileName);

        if(destinationFile.exists()){
            log.warn(String.format("Unidentified File: %s already exists in the destination", fileName));
            destinationFile.delete();
        }

        try {
            FileUtils.moveFile(sourceFile, destinationFile);
        } catch (IOException e) {
            log.error("Unidentified file failed to move", e);
        }
    }

}
