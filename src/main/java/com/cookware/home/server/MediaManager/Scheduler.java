package com.cookware.home.server.MediaManager;

import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * Created by Kody on 12/10/2017.
 */
public class Scheduler {
    private static final Logger log = Logger.getLogger(Scheduler.class);
    private final autoDownload downloading = autoDownload.MANUAL_OFF;
    private final int daysInAWeek = 7;
    private DaySchedule[] weekSchedule = new DaySchedule[daysInAWeek];


    public boolean isDownloading() {
        if(downloading.equals(autoDownload.MANUAL_ON)) {
            return true;
        }
        else if(downloading.equals(autoDownload.MANUAL_OFF)) {
            return false;
        }
        else {
            return isCurrentlyScheduledForDownload();
        }
    }

    public boolean isCurrentlyScheduledForDownload() {
        // TODO: Finish Implementing this (isCurrentlyScheduledForDownload) method

        return false;
    }

    private void saveSchedule(){
        // TODO: Finish Implementing this (saveSchedule) method

    }

    private void loadSchedule(){
        // TODO: Finish Implementing this (loadSchedule) method

    }

    private class DaySchedule {
        private final int hoursInADay = 24;
        private boolean[] day = new boolean[hoursInADay];

        public void setHour(int hour, boolean downloading){
            this.day[hour] = downloading;
        }

        public boolean getHour(int hour){
            return this.day[hour];
        }
    }


    public enum autoDownload{
        AUTOMATIC, MANUAL_ON, MANUAL_OFF
    }
}
