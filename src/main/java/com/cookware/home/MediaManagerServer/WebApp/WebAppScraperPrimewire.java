package com.cookware.home.MediaManagerServer.WebApp;

import com.cookware.home.MediaManagerServer.Tools.WebTools;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

/**
 * Created by Kody on 27/10/2017.
 */
public class WebAppScraperPrimewire implements WebAppScraper{
    private static final Logger log = Logger.getLogger(WebAppScraperPrimewire.class);
    private final WebTools webTools = new WebTools();
    private final String baseUrl = "http://www.primewire.ag";


    public List<WebAppMediaItem> getMediaOptions(String searchQuery){
        return getMediaOptions(searchQuery, 0);
    }

    public List<WebAppMediaItem> getMediaOptions(String searchQuery, String mediaType){
        return getMediaOptions(searchQuery, 0);
    }

    public List<WebAppMediaItem> getMediaOptions(String searchQuery, String mediaType, int page){
        return getMediaOptions(searchQuery, page);
    }

    public List<WebAppMediaItem> getMediaOptions(String searchQuery, int page){
        String url;
        if((page == 0 )||(page == 1)){
            url = baseUrl + "/index.php?search_keywords=" + searchQuery.replace(' ', '+');
        }
        else{
            url = baseUrl + "/index.php?search_keywords=" + searchQuery.replace(' ', '+') + "&page=" + page;
        }
        return getMediaOptionsFromUrl(url);
    }

    private List<WebAppMediaItem> getMediaOptionsFromUrl(String url){
        String result = "";
        Scanner consoleScanner = new Scanner(System.in);
        final List<WebAppMediaItem> foundMedia = new ArrayList<>();

        if(!webTools.checkInternetConnection()){
            log.error("Web App server could not complete request - internet not connected");
            return null;
        }

        if(!webTools.checkVpnConnection()){
            log.error("Web App server could not complete request - VPN not connected");
            return null;
        }

        URLConnection connection = null;
        InputStream response = null;
        try {
            connection = new URL(url).openConnection();
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            response = connection.getInputStream();
        } catch (IOException e) {
            log.error("Url is not in a compatible format");
            return null;
        }

        if(connection.getHeaderField(1).equals("nginx")){
            log.error("Cannot access primewire - check VPN connection");
            return null;
        } else {
        }

        String html = "";
        try (Scanner scanner = new Scanner(response)) {
            html = scanner.useDelimiter("\\A").next();
        }

        Document document = Jsoup.parse(html);
        Elements matchedMedia = document.getElementsByClass("index_item");
        if(matchedMedia.isEmpty()){
            log.error("No movies found - check search entry");
            return foundMedia;
        }

        String title;
        String imageUrl;
        String linkUrl;
        int tagIndex;
        for (Element media : matchedMedia) {
            title = media.getElementsByAttribute("href").attr("title").substring(6);
            imageUrl = media.getElementsByAttribute("src").attr("src");
            tagIndex = imageUrl.lastIndexOf("/");
            imageUrl = imageUrl.substring(tagIndex+1);
            linkUrl = this.baseUrl + media.getElementsByAttribute("title").attr("href");
            foundMedia.add(new WebAppMediaItem(title, imageUrl, linkUrl));
        }

        return foundMedia;
    }
}
