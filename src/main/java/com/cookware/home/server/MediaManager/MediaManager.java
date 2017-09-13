package com.cookware.home.server.MediaManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.math.BigInteger;
import java.util.List;

import com.bitlove.fnv.FNV;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * Created by Kody on 5/09/2017.
 */
public class MediaManager {
    private final MediaManagerRunnable mediaManagerRunnable = new MediaManagerRunnable();
    private static final Logger log = Logger.getLogger(MediaManager.class);

    public void start(){
        Thread thread = new Thread(mediaManagerRunnable);
        thread.start();
    }

    public void addNewMediaRequest(String url, int priority, String qualityString){
        mediaManagerRunnable.addNewMediaRequest(url, priority, qualityString);
    }
}