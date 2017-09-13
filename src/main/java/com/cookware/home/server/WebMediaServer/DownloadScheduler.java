package com.cookware.home.server.WebMediaServer;

import java.util.ArrayList;

/**
 * Created by Kody on 21/08/2017.
 */
public class DownloadScheduler implements Runnable {
    private ArrayList<Download> downloadQueue;
    private SchedulerState state;
    private Thread t;

    public DownloadScheduler(){
        this.downloadQueue = new ArrayList<Download>();
    }

    public void run(){
        System.out.println("Running Scheduer");
        this.state = SchedulerState.RUNNING;
            for (Download currentDownload : this.downloadQueue) {
                if (this.state.equals(SchedulerState.RUNNING)) {
                    if (currentDownload.getState().equals(DownloadState.QUEUED)) {
                        download();
                        // TODO: Get rid of the hardcoded string on the line below
                    }
                }
//                        Thread.sleep(1000);
            }
        System.out.println("Scheduler Exiting.");
        this.state = SchedulerState.STOPPED;
    }

    public void download(){
        Download currentDownload = downloadQueue.get(0);
        try {
            String downloadUrl = new WebMediaBridge().getDownloadUrl("http://www.primewire.ag", currentDownload.getUrl());
            new WebMediaDownloader().newDownload(currentDownload.getType(), downloadUrl, currentDownload.getName());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Scheduler Interrupted.");
            this.state = SchedulerState.STOPPED;
        }
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

                addDownloadToQueue(new Download(media.getName(), media.getLink(), MediaType.MOVIE, media.getPriority()));
//                addDownloadToQueue(new Download(media.getName(), "http://www.primewire.ag/tv-1386995-Game-of-Thrones/season-7-episode-7", MediaType.MOVIE, media.getPriority()));
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
                    addDownloadToQueue(new Download(episode.getName(), episode.getLink(), MediaType.TV, episode.getPriority()));
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
//            if (download.equals(currentDownload)) {
//                download.setState(DownloadState.FINISHED);
//                break;
//            }
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
