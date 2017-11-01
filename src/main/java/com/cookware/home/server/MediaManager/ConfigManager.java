package com.cookware.home.server.MediaManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Kody on 30/10/2017.
 */
public class ConfigManager {
//    private static final Logger log = Logger.getLogger(ConfigManager.class); // Cannot be used until logs are instanciated
    // TODO: Create a config object and separate it into a different class

    public ConfigManager(String path){
        Properties properties = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(path);
            properties.load(input);

            Launcher.logPropertiesPath = properties.getProperty("logProperties");
            Launcher.logsPath = properties.getProperty("logs");
            Launcher.databasePath = properties.getProperty("database");
            Launcher.scheduleFileName = properties.getProperty("schedule");
            Launcher.tempPath = properties.getProperty("tempMedia");
            Launcher.finalPath = properties.getProperty("finalMedia");

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
