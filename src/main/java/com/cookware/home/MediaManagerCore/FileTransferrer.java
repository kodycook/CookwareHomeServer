package com.cookware.home.MediaManagerCore;

import com.cookware.home.MediaManagerCommon.DataTypes.Config;
import com.cookware.home.MediaManagerCommon.Managers.DatabaseManager;
import org.apache.log4j.Logger;

/**
 * Created by Kody on 19/09/2017.
 */
public class FileTransferrer {
        private final FileTransferrerRunnable fileTransferrerRunnable;
        private static final Logger log = Logger.getLogger(FileTransferrer.class);

        public FileTransferrer(DatabaseManager databaseManager, Config config){
            fileTransferrerRunnable = new FileTransferrerRunnable(databaseManager, config);
        }

        public void start(){
            Thread thread = new Thread(fileTransferrerRunnable);
            thread.start();
        }
}
