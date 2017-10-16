package com.cookware.home.server.MediaManager;

import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Created by Kody on 12/10/2017.
 */
public class Scheduler {
    private static final Logger log = Logger.getLogger(Scheduler.class);
    private final autoDownload downloading = autoDownload.MANUAL_ON;
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
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now)); //2016/11/16 12:08:43

        int hour = now.getHour();
        int day = now.getDayOfWeek().getValue() - 1;

        return weekSchedule[day].daySchedule[hour];
    }


    public int getScheduleState(){
        if(this.downloading.equals(autoDownload.MANUAL_OFF)){
            return 0;
        }
        else if (this.downloading.equals(autoDownload.MANUAL_ON)){
            return 1;
        }
        else {
            return 2;
        }
    }

    private void saveSchedule(){
        // TODO: Finish Implementing this (saveSchedule) method

    }

    private void loadSchedule(){
        // TODO: Finish Implementing this (loadSchedule) method

    }

    private class DaySchedule {
        private final int hoursInADay = 24;
        private boolean[] daySchedule = new boolean[hoursInADay];

        public void setHour(int hour, boolean downloading){
            this.daySchedule[hour] = downloading;
        }

        public boolean getHour(int hour){
            return this.daySchedule[hour];
        }
    }


    public enum autoDownload{
        AUTOMATIC, MANUAL_ON, MANUAL_OFF
    }
}
