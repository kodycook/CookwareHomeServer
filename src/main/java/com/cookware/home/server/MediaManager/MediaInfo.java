package com.cookware.home.server.MediaManager;

import org.apache.log4j.Logger;

import javax.print.attribute.standard.Media;
import java.math.BigInteger;
import java.time.LocalDate;

/**
 * Created by Kody on 12/09/2017.
 */
public class MediaInfo {
    private static final Logger log = Logger.getLogger(MediaInfo.class);
    // TODO: Create a flag that switches the toString method between a short and long format
    private final boolean printLong = false;
    public BigInteger ID;
    public String URL;
    public String NAME;
    public MediaType TYPE;
    public DownloadState STATE;
    public int PRIORITY;
    public int QUALITY;
    public LocalDate RELEASED;
    public LocalDate ADDED;
    public String PATH;
    public BigInteger PARENTSHOWID;
    public String PARENTSHOWNAME;
    public float EPISODE = -1;

    public String toString() {
        if(printLong == true){
            return toStringLong();
        }
        else{
            return toStringShort();
        }
    }


    private String toStringLong(){
        if(this.TYPE.equals(MediaType.EPISODE)){
            return toStringLongEpisode();
        }
        else{
            return toStringLongMovieOrTv();
        }
    }

    private String toStringLongEpisode(){
        return String.format("TV Show: %s (%d)\n" +
                        "Episode S%dE%d - %s (%d)\n" +
                        "Released: %s\n" +
                        "Downloaded: %s\n" +
                        "State: %s\n" +
                        "Quality: %s\n" +
                        "Priority: %s\n" +
                        "URL: %s\n" +
                        "Path: %s",
                this.PARENTSHOWNAME,
                this.PARENTSHOWID,
                getSeason(),
                getEpisode(),
                this.NAME,
                this.ID,
                this.RELEASED.toString(),
                this.ADDED,
                this.STATE.toString(),
                getQualityString(),
                getPriorityString(),
                this.URL,
                this.PATH);
    }

    private String toStringLongMovieOrTv(){
        return String.format("Media: %s (%d)\n" +
                        "Released: %s\n" +
                        "Downloaded: %s\n" +
                        "State: %s\n" +
                        "Quality: %s\n" +
                        "Priority: %s\n" +
                        "URL: %s\n" +
                        "Path: %s",
                this.NAME,
                this.ID,
                this.RELEASED.toString(),
                this.ADDED,
                this.STATE.toString(),
                getQualityString(),
                getPriorityString(),
                this.URL,
                this.PATH);
    }


    private String toStringShort(){
        if(this.TYPE.equals(MediaType.EPISODE)){
            return toStringShortEpisode();
        }
        else{
            return toStringShortMovieOrTv();
        }
    }

    private String toStringShortEpisode(){
        return String.format("[%d] %s - S%dE%d: %s (%s)",
                this.ID,
                this.PARENTSHOWNAME,
                getSeason(),
                getEpisode(),
                this.NAME,
                this.RELEASED.toString());
    }

    private String toStringShortMovieOrTv(){
        return String.format("[%d] %s (%s)",
                this.ID,
                this.NAME,
                this.RELEASED.toString());
    }


    public String getQualityString(){
        if (this.QUALITY == -1){
            return "MAX";
        }
        else if (this.QUALITY == 0){
            return "MIN";
        }
        else {
            return String.format("%dp", this.QUALITY);
        }
    }


    public String getPriorityString() {
        String priorityString;
        switch (this.PRIORITY) {
            case 0:
                priorityString = "IMMEDIATE";
                break;

            case 1:
                priorityString = "VERY HIGH";
                break;

            case 2:
                priorityString = "HIGH";
                break;

            case 3:
                priorityString = "MEDIUM";
                break;

            case 4:
                priorityString = "LOW";
                break;

            case 5:
                priorityString = "VERY LOW";
                break;

            default:
                log.error(String.format("Priority for %s not recognised", this.NAME));
                priorityString = "N/A";
                break;
        }
        return priorityString;
    }


    public int getSeason(){
        if (this.EPISODE != -1){
            return (int) Math.floor(this.EPISODE);
        }
        else {
            log.error(String.format("Failed to get Season Number for %s", this.NAME));
            return -1;
        }
    }


    public int getEpisode(){
        if (this.EPISODE != -1){
            return (int) ((this.EPISODE-Math.floor(this.EPISODE))*100);
        }
            else {
            log.error(String.format("Failed to get Episode Number for %s", this.NAME));
            return -1;
        }
    }
}
