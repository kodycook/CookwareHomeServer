package com.cookware.home.MediaManagerServer.WebApp;

/**
 * Created by Kody on 27/10/2017.
 */
public class WebAppMediaItem {
    public String name;
    public String coverImageUrl;
    public String url;

    public WebAppMediaItem(String mName, String mCoverImageUrl, String mUrl){
        this.name = mName;
        this.coverImageUrl = mCoverImageUrl;
        this.url = mUrl;
    }
}
