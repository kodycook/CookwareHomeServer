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
    private final CsvManager csvManager = new CsvManager();
    private final autoDownload downloading = autoDownload.AUTOMATIC;
    private final int daysInAWeek = 7;
    private final int hoursInADay = 24;
    private final String schedulerFileName;
    private boolean[][] weekSchedule = new boolean[this.hoursInADay][this.daysInAWeek];
    private boolean autoDownloadState = false;

    public Scheduler(String fileName){
        this.schedulerFileName = fileName;
        if(csvManager.createFile(this.schedulerFileName)){
            initialiseSchedule();
            saveSchedule();
        }
        else {
            loadSchedule();
        }
    }

    private void initialiseSchedule(){
        for(int hour = 0; hour < this.hoursInADay; hour++){
            for(int day = 0; day < this.daysInAWeek; day++){
                this.weekSchedule[hour][day] = true;
            }
        }
    }

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

    private boolean isCurrentlyScheduledForDownload() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        boolean currentlyDownloading;


        int hour = now.getHour();
        int day = now.getDayOfWeek().getValue() - 1;

        currentlyDownloading = weekSchedule[hour][day];

        if(currentlyDownloading & !this.autoDownloadState){
            log.info("Scheduler started downloader");
        }
        if(!currentlyDownloading & this.autoDownloadState){
            log.info("Scheduler stopped downloader");
        }
        this.autoDownloadState = currentlyDownloading;

        return weekSchedule[hour][day];
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
        csvManager.writeStringArrayToCsv(this.schedulerFileName, convertScheduleToStringArray());
    }


    private void loadSchedule(){
        String savedConfig[][] = csvManager.getStringArrayFromCsv(this.schedulerFileName);
        convertStringArrayToSchedule(savedConfig);
    }


    private String[][] convertScheduleToStringArray(){
        String [][] result = new String[this.hoursInADay + 1][this.daysInAWeek + 1];
        result[0] = new String[]{"",
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday",
                "Saturday",
                "Sunday"
        };

        for(int hour = 0; hour < this.hoursInADay; hour++){
            result[hour+1][0] = String.format("%02d:00 - %02d:59", hour, hour);
            for(int day = 0; day < this.daysInAWeek; day++){
                result[hour+1][day+1] = this.weekSchedule[hour][day] ? "x" : "";
            }
        }
        return result;
    }


    private void convertStringArrayToSchedule(String[][] savedConfig){
        for(int hour = 0; hour < this.hoursInADay; hour++){
            for(int day = 0; day < this.daysInAWeek; day++){
                this.weekSchedule[hour][day] = savedConfig[hour + 1][day + 1].equals("x");
            }
        }
    }


    public enum autoDownload{
        AUTOMATIC, MANUAL_ON, MANUAL_OFF
    }
}
