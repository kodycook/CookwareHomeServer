package com.cookware.home.server.MediaManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.math.BigInteger;

import com.bitlove.fnv.FNV;
import org.apache.log4j.Logger;


/**
 * Created by Kody on 5/09/2017.
 */
public class MediaManager extends Thread{
    private static ArrayList<QueuedMedia> mediaQueue;
    private static DataBaseManager dataBaseManager;
    private FNV stringHasher;
    private Logger log;


    public MediaManager(){
        log = Logger.getLogger(this.getClass());
        stringHasher = new FNV();
        mediaQueue = new ArrayList<QueuedMedia>();
        dataBaseManager = new DataBaseManager("media.db");
    }

    @Override
    public void run()
    {
        // TODO: Add method to load up queue from database

        while(true)
        {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void add(String url, int priority, String qualityString){
        MediaInfo info = pullMediaInfoFromUrl(url);

//        String shortMediaName = generateShortMediaName(info);
        String shortMediaName = String.format("%f",Math.random());

        BigInteger mediaId = this.stringHasher.fnv1a_32(shortMediaName.getBytes());
        log.info(mediaId);
        log.info(DownloadState.PENDING.ordinal());

//        int quality = qualityStringIntoInteger(qualityString);
        int quality = -1;

        dataBaseManager.addMedia(mediaId, info.NAME, info.URL, quality, 1, priority, info.RELEASE, LocalDate.now());

        // TODO: Add item to queue
    }


    public MediaInfo pullMediaInfoFromUrl(String url){
        MediaInfo info = new MediaInfo();

        WebTool webTool = new WebTool(url);

        info.URL = url;
        info.NAME = "kody";
        info.RELEASE = LocalDate.now();

        return info;
    }

    public class QueuedMedia {
        int databaseId;
        String name;
        String url;

        public QueuedMedia(){

        }

    }

    public class MediaInfo {
        public String URL;
        public String NAME;
        public LocalDate RELEASE;
        public MediaType TYPE;
        public float EPISODE;
    }
}
