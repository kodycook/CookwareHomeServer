package com.cookware.home.server.MediaManager;

import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Kody on 30/10/2017.
 */
public class ConfigManager {
    private static final Logger log = Logger.getLogger(ConfigManager.class);
    public static String databaseName;
    public static String scheduleFileName;
    public static String tempPath;
    public static String finalPath;
//    public static String finalPath = "C:\\Users\\maste\\IdeaProjects\\CookwareHomeServer\\Media";

    public ConfigManager(String path){
        Properties properties = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(path);
            properties.load(input);

            databaseName = properties.getProperty("database");
            scheduleFileName = properties.getProperty("schedule");
            tempPath = properties.getProperty("tempMedia");
            finalPath = properties.getProperty("finalMedia");

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
