package com.cookware.home.MediaManagerServer.WebApp;

import com.cookware.home.MediaManagerServer.Tools.WebTools;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Kody on 27/10/2017.
 */
public class WebAppScraperTornado implements WebAppScraper{
    private static final Logger log = Logger.getLogger(WebAppScraperTornado.class);
    private final WebTools webTools = new WebTools();
    private final String baseUrl = "https://www1.tornadomovies.to";


    public List<WebAppMediaItem> getMediaOptions(String searchQuery){
        return getMediaOptions(searchQuery, "movies", 0);
    }

    public List<WebAppMediaItem> getMediaOptions(String searchQuery, int page) {
        return getMediaOptions(searchQuery, "movies", 0);
    }

    public List<WebAppMediaItem> getMediaOptions(String searchQuery, String mediaType) {
        return getMediaOptions(searchQuery, mediaType, 0);
    }

    public List<WebAppMediaItem> getMediaOptions(String searchQuery, String mediaType, int page){
        String url;
        String mediaTypeString;

        if(mediaType.equals("movies")){
            mediaTypeString = "movies";
        }
        else if (mediaType.equals("tv")) {
            mediaTypeString = "series";
        }
        else {
            log.error("Wrong Media type requested");
            return null;
        }

        if((page == 0 )||(page == 1)){
            url = String.format("%s/search_all/~%s~/%s", baseUrl, searchQuery.replace(" ", "%20"), mediaTypeString);
        }
        else{
            url = String.format("%s/search_all/~%s~/%s/%d", baseUrl, searchQuery.replace(" ", "%20"), mediaTypeString, page);
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

        String html = webTools.getWebPageHtml(url);

        Document document = Jsoup.parse(html);
        Elements matchedMedia = document.getElementsByAttributeValue("class", "poster");

        if(matchedMedia.isEmpty()){
            return foundMedia;
        }

        String title;
        String imageUrl;
        String linkUrl;
        for (Element media : matchedMedia) {
            title = media.getElementsByClass("poster").attr("data-name");
            if(title.endsWith(" HD")){
                title = title.substring(0, title.length() - 3);
            }
            imageUrl = media.getElementsByClass("poster").attr("data-img");
            int queryIndex = imageUrl.lastIndexOf('?');
            imageUrl = imageUrl.substring(imageUrl.lastIndexOf("url=") + 4, queryIndex);

            linkUrl = this.baseUrl + media.getElementsByTag("a").attr("href");
            foundMedia.add(new WebAppMediaItem(title, imageUrl, linkUrl));
        }

        return foundMedia;
    }
}
