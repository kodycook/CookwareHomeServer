package com.cookware.home.server.WebMediaServer;

/**
 * Created by Kody on 13/08/2017.
 */
public class Media {
    protected  String name;
    protected  MediaType type;
    protected  DownloadState state;
    protected  String primewireUrl;
    protected int priority;

    //TODO: Add Date Release and Downloaded (To The TVShow Object and Movie objects as well
//    date dateReleased;
//    date dateDownloaded;
//    date dateAdded;

    public Media() {

    }

    public Media(String mName){
        this.name = mName;
        this.state = DownloadState.PENDING;
        this.priority = 3;
    }

    public String getName(){
        return this.name;
    }

    public DownloadState getState(){
        return this.state;
    }

    public void setState(DownloadState mState){
        this.state = mState;
    }

    public int getPriority(){
        return this.priority;
    }

    public void addLink(String link){
        this.primewireUrl = link;
    }

    public String getLink(){
        return this.primewireUrl;
    }
}
