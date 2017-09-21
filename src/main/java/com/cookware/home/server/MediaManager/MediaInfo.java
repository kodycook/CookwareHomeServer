package com.cookware.home.server.MediaManager;

import java.math.BigInteger;
import java.time.LocalDate;

/**
 * Created by Kody on 12/09/2017.
 */
public class MediaInfo {
    public BigInteger ID;
    public String URL;
    public String NAME;
    public MediaType TYPE;
    public DownloadState STATE;
    public int PRIORITY;
    public int QUALITY;
    public LocalDate RELEASED;
    public String PATH;
    public BigInteger PARENTSHOWID;
    public String PARENTSHOWNAME;
    public float EPISODE = -1;

    public String toString() {
        if(EPISODE != -1){
            return String.format("E%dS%d: %s(%s):%d - %s",
                    getSeason(),
                    getEpisode(),
                    this.NAME,
                    this.RELEASED.toString(),
                    this.ID,
                    this.URL);
        }
        else{
            return String.format("%s(%s):%d - %s",
                    this.NAME,
                    this.RELEASED.toString(),
                    this.ID,
                    this.URL);
        }
    }

    public int getSeason(){
        if (this.EPISODE != -1){
            return (int) Math.floor(this.EPISODE);
        }
        else {
            return -1;
        }
    }

    public int getEpisode(){
        if (this.EPISODE != -1){
            return (int) ((this.EPISODE-Math.floor(this.EPISODE))*100);
        }
            else {
            return -1;
        }
    }
}
