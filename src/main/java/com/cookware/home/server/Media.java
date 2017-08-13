package com.cookware.home.server;

/**
 * Created by Kody on 13/08/2017.
 */
public class Media {
    protected  String name;
    protected  MediaType type;
    protected  DownloadState state;
    protected  String primewireUrl;

    //TODO: Add Date Release and Downloaded (To The TVShow Object and Movie objects as well
//        date dateReleased;
//        date dateDownloaded;

    public Media(String mName){
        this.name = mName;
        this.state = DownloadState.PENDING;
    }

    public void addLink(String link){
        this.primewireUrl = link;
    }

    public String getLink(){
        return this.primewireUrl;
    }
}
