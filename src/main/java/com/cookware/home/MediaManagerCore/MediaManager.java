package com.cookware.home.MediaManagerCore;

import com.cookware.home.MediaManagerCommon.DataTypes.Config;
import org.apache.log4j.Logger;


/**
 * Created by Kody on 5/09/2017.
 */
public class MediaManager {
    private static final Logger log = Logger.getLogger(MediaManager.class);
    private final MediaManagerRunnable mediaManagerRunnable;


    public MediaManager (Config config){
        mediaManagerRunnable = new MediaManagerRunnable(config);
    }


    public void start(){
        Thread thread = new Thread(mediaManagerRunnable);
        thread.start();
    }


    public String addNewMediaRequest(String url, int priority, String qualityString){
        return mediaManagerRunnable.addNewMediaRequest(url, priority, qualityString);
    }
}