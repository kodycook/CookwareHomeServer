package com.cookware.home.server.WebMediaServer;

import java.util.ArrayList;

/**
 * Created by Kody on 21/08/2017.
 */
public class DownloadScheduler implements Runnable {
    private ArrayList<Download> downloadQueue;
    private Download currentDownload;
    private SchedulerState state;
    private Thread t;

    public DownloadScheduler(){
    }

    public void run(){
        System.out.println("Running Scheduer");
        this.state = SchedulerState.RUNNING;
        try {
            while(true) {
                while ((downloadQueue.size() > 0) && this.state.equals(SchedulerState.RUNNING)) {
                    this.currentDownload = downloadQueue.get(0);

                    //TODO: Add code here to create the media bridge and download file

                    //            if(success){
                    downloadQueue.remove(0);
                    //            parent file change state to success
                    //          } else {
                    //          }
                }
                Thread.sleep(1000);
            }
        }catch (InterruptedException e) {
            System.out.println("Scheduler Interrupted.");
            this.state = SchedulerState.STOPPED;

        }
        System.out.println("Scheduler Exiting.");
        this.state = SchedulerState.STOPPED;
    }

    public void start () {
        System.out.println("Starting Scheduer");
        if (t == null) {
            t = new Thread(this);
            t.start ();
        }
    }

    public void addMedia(Media media){
        if(media instanceof Movie){
            if(media.getState().equals(DownloadState.PENDING)){
                addDownloadToQueue(new Download(media.getName(), media.getLink(), media.getPriority()));
            }
        }
        else if (media instanceof TvShow) {
            addTvShowEpisodes((TvShow)media);
        }
    }

    public void addTvShowEpisodes(TvShow show){
        int SeasonNumberCount = 0;
        int EpisodeNumberCount = 0;
        for(TvShow.Season season : show.getSeasons()){
            SeasonNumberCount ++;
            EpisodeNumberCount = 0;
            for(TvShow.Episode episode : season.getEpisodes()){
                EpisodeNumberCount ++;
                if(episode.getState().equals(DownloadState.PENDING)){
                    addDownloadToQueue(new Download(episode.getName(), episode.getLink(), episode.getPriority()));
                    episode.setState(DownloadState.QUEUED);
                }
            }
        }
    }

    public void addDownloadToQueue(Download download){
        this.downloadQueue.add(download);
    }

    public void downloadComplete(){
        for(Download download: this.downloadQueue) {
            if (download.equals(currentDownload)) {
                download.setState(DownloadState.FINISHED);
                break;
            }
        }

        refreshQueue();
    }

    public void refreshQueue(){
        ArrayList<Download> tempDownloadList = new ArrayList<Download>();
        for(int i = 0; i <=5; i++){
            for(Download download: this.downloadQueue) {
                if((download.getPriority() == i) && (download.getState().equals(DownloadState.QUEUED))){
                    tempDownloadList.add(download);
                }
            }
        }
        this.downloadQueue = tempDownloadList;
    }

    public String toString(){
        String result = "";

        int i = 0;
        for(Download download: this.downloadQueue) {
            i++;
            result += String.format("%d)\t%s\n", i, download.toString());
        }

        return result;
    }


}
