package com.cookware.home.MediaManagerServer.WebApp;

import com.cookware.home.MediaManagerServer.DataTypes.MediaType;

import java.util.List;

/**
 * Created by Kody on 12/05/2018.
 */
public interface WebAppScraper {

    public List<WebAppMediaItem> getMediaOptions(String searchQuery);

    public List<WebAppMediaItem> getMediaOptions(String searchQuery, int page);

    public List<WebAppMediaItem> getMediaOptions(String searchQuery, String mediaType);

    public List<WebAppMediaItem> getMediaOptions(String searchQuery, String mediaType, int page);
}
