package com.cookware.home.server.MediaManager;

import org.apache.log4j.Logger;

/**
 * Created by Kody on 19/09/2017.
 */
public class FileTransferrer {
        private final FileTransferrerRunnable fileTransferrerRunnable;
        private static final Logger log = Logger.getLogger(FileTransferrer.class);

        public FileTransferrer(DatabaseManager databaseManager){
            fileTransferrerRunnable = new FileTransferrerRunnable(databaseManager);
        }

        public void start(){
            // TODO: Sort out syncronised access to variables

            Thread thread = new Thread(fileTransferrerRunnable);
            thread.start();
        }
}
