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
    public BigInteger PARENTSHOWID;
    public LocalDate RELEASED;
    public float EPISODE = -1;

    public String toString() {
        if(EPISODE != -1){
            return String.format("E%dS%d: %s(%s):%d - %s",
                    (int) Math.floor(this.EPISODE),
                    (int) ((this.EPISODE-Math.floor(this.EPISODE))*100),
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
}
