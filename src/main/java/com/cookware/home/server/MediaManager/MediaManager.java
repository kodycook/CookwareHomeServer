package com.cookware.home.server.MediaManager;

import org.apache.log4j.Logger;


/**
 * Created by Kody on 5/09/2017.
 */
public class MediaManager {
    private final MediaManagerRunnable mediaManagerRunnable = new MediaManagerRunnable();
    private static final Logger log = Logger.getLogger(MediaManager.class);

    public void start(){
        // TODO: Sort out syncronised access to variables

        Thread thread = new Thread(mediaManagerRunnable);
        thread.start();
    }

    public void addNewMediaRequest(String url, int priority, String qualityString){
        mediaManagerRunnable.addNewMediaRequest(url, priority, qualityString);
    }
}