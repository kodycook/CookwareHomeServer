package com.cookware.home.server.MediaManager;

import org.apache.log4j.Logger;


/**
 * Created by Kody on 5/09/2017.
 */
public class MediaManager {
    private final MediaManagerRunnable mediaManagerRunnable = new MediaManagerRunnable();
    private static final Logger log = Logger.getLogger(MediaManager.class);
    public static final String databaseName = "media.db";
    public static final String scheduleFileName = "C:\\Users\\maste\\IdeaProjects\\CookwareHomeServer\\config\\Schedule.csv";
    public static final String tempPath = "C:\\Users\\maste\\IdeaProjects\\CookwareHomeServer\\Media";
    public static final String finalPath = "\\\\WDMYCLOUDEX2\\Public\\Media";
//    public static String finalPath = "C:\\Users\\maste\\IdeaProjects\\CookwareHomeServer\\Media";


    public void start(){
        Thread thread = new Thread(mediaManagerRunnable);
        thread.start();
    }

    public void addNewMediaRequest(String url, int priority, String qualityString){
        mediaManagerRunnable.addNewMediaRequest(url, priority, qualityString);
    }
}