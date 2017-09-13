package com.cookware.home.server.MediaManager;

import com.bitlove.fnv.FNV;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Kody on 5/09/2017.
 */
public class MediaManagerRunnable implements Runnable{
    private final List<MediaInfo> mediaQueue = new ArrayList<>();
    private final DownloadManager downloadManager = new DownloadManager();
    private final DataBaseManager dataBaseManager;
    private final FNV stringHasher;
    private static final Logger log = Logger.getLogger(MediaManagerRunnable.class);


    public MediaManagerRunnable(){
        stringHasher = new FNV();
        dataBaseManager = new DataBaseManager("media.db");
        dataBaseManager.initialiseDataBase();
    }


    @Override
    public void run()
    {
        boolean downloadSuccess;
        int index = 0;
        MediaInfo currentMedia;
        retrieveQueuedMediaFromDatabase(mediaQueue);

        while(true)
        {
            while(mediaQueue.size()>index){
                // TODO: Add a method to sort the media queue by priority

                currentMedia = mediaQueue.get(index);
                if (currentMedia.TYPE.equals(MediaType.TV)){
                    index ++;
                }
                else {
                    downloadSuccess = downloadManager.downloadMedia(currentMedia);
                    if (downloadSuccess) {
                        updateState(currentMedia, DownloadState.TRANSFERRING);
                        // TODO: Add a method to move the media into the correct place
                        updateState(currentMedia, DownloadState.FINISHED);
                        mediaQueue.remove(index);
                    } else {
                        log.error(String.format("Media failed to download: %s", currentMedia.toString()));
                        updateState(currentMedia, DownloadState.FAILED);
                        index++;
                    }
                }
            }

            log.info("No more media to download");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateState(MediaInfo mediaInfo, DownloadState downloadState){
        mediaInfo.STATE = downloadState;
        dataBaseManager.updateState(mediaInfo.ID, downloadState);
    }

    public void retrieveQueuedMediaFromDatabase(List<MediaInfo> mediaQueue) {
        List<MediaInfo> tempMediaQueue = dataBaseManager.getDownloadQueue();
        if(tempMediaQueue.size()!=0) {
            log.info(String.format("Retrieved %d pending downloads from Database", tempMediaQueue.size()));
            for (MediaInfo queuedMedia : tempMediaQueue) {
                mediaQueue.add(queuedMedia);
            }
        }
    }


    public void addNewMediaRequest(String url, int priority, String qualityString){
        final WebTool webTool = new WebTool();
        final List<MediaInfo> episodes = retrieveEpisodesFromUrl(url);
        MediaInfo info = new MediaInfo();

        info.URL = url;
        info.QUALITY = qualityStringIntoInteger(qualityString);
        info.PRIORITY = priority;


        if (episodes.isEmpty()) {
            info.TYPE = MediaType.MOVIE;
            info = addMediaToDataBase(info);
        }
        else {
            info.TYPE = MediaType.TV;
            info = addMediaToDataBase(info);

            for(MediaInfo episodeInfo: episodes){
                episodeInfo.TYPE = MediaType.EPISODE;
                episodeInfo.PARENTSHOWID = info.ID;
                episodeInfo.PRIORITY = info.PRIORITY;
                episodeInfo.QUALITY = info.QUALITY;
                episodeInfo = addMediaToDataBase(episodeInfo);
            }
        }
    }


    public ArrayList<MediaInfo> retrieveEpisodesFromUrl(String url){
        WebTool webTool = new WebTool();
        ArrayList<MediaInfo> episodeInfoList = new ArrayList<>();
        final String html = webTool.getWebPageHtml(url);
        final Document document = Jsoup.parse(html);
        final Elements tvSeasons = document.getElementsByClass("tv_container");
        MediaInfo episodeInfo;
        int seasonNumber, episodeNumber;
        String episodeText, episodeReleaseDateAsString;
        String[] episodeAttributes;

        if(tvSeasons.isEmpty()){ //This means that this media is a Movie
            return episodeInfoList;
        }
        else {
            for(Element element : tvSeasons){
                Elements seasons = element.getElementsByClass("show_season");
                for(Element season : seasons){
                    seasonNumber = Integer.parseInt(season.getElementsByAttribute("data-id").attr("data-id"));
                    if(seasonNumber != 0){
                        Elements episodes = season.getElementsByClass("tv_episode_item");
                        for(Element episode : episodes){
                            episodeText = episode.text();
                            episodeAttributes = episodeText.split(" - ");
                            episodeNumber = Integer.parseInt(episodeAttributes[0].substring(1));
                            if(episodeAttributes.length != 3){
                                log.warn(String.format("Skipped S%dE%d at %s due to error parsing details",
                                        seasonNumber,
                                        episodeNumber,
                                        url));
                            }
                            else {
                                episodeReleaseDateAsString = episodeAttributes[2].substring(0,10);
                                if(episodeNumber != 0) {
                                    episodeInfo = new MediaInfo();
                                    episodeInfo.NAME = episodeAttributes[1];
                                    episodeInfo.URL = webTool.extractBaseURl(url) + episode.getElementsByAttribute("href").attr("href");
                                    episodeInfo.EPISODE = seasonNumber + ((float) episodeNumber)/100;
                                    try {
                                        episodeInfo.RELEASED = LocalDate.parse(episodeReleaseDateAsString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                    }
                                    catch (Exception e){
                                        log.error(e);
                                    }
                                    episodeInfoList.add(episodeInfo);
                                }
                            }
                        }
                    }
                }
            }
        }
        return episodeInfoList;
    }


    public MediaInfo addMediaToDataBase(MediaInfo info){
        Boolean success;
        if (!info.TYPE.equals(MediaType.EPISODE)) {
            info = scrapeMediaInfoFromUrl(info);
        }

        String shortMediaName = generateShortMediaName(info);
        info.ID = this.stringHasher.fnv1a_32(shortMediaName.getBytes());

        if(mediaInfoValid(info)){
            success = dataBaseManager.addMediaToDatabase(info);
        }
        else {
            log.error("Media not added");
            return info;
        }

        if(success){
            mediaQueue.add(info);
//            for(QueuedMedia queuedMedia:mediaQueue){
//                System.out.println(queuedMedia.toString());
//            }
        }
        return info;
    }

    public MediaInfo scrapeMediaInfoFromUrl(MediaInfo info){
        String name;
        final WebTool webTool = new WebTool();
        final String scrapedHtml = webTool.getWebPageHtml(info.URL);

        if (!info.TYPE.equals(MediaType.EPISODE)){
            final Document document = Jsoup.parse(scrapedHtml);
            name = getNameFromScrapedHtml(document);
            info.NAME = name.substring(0,name.length()-9);
            info.RELEASED = getReleasedDatafromScrapedHtml(document);
        }

        // TODO: Move this code to the Media Downloader
//        completeExtractedMediaHostUrl = webTool.extractBaseURl(url) + getRedirectedMediaLink(scrapeHtml);
//        log.info(String.format("Extracted media URL:%s",completeExtractedMediaHostUrl));

        return info;
    }


    public boolean mediaInfoValid(MediaInfo info){
        return true;
    }


    private String generateShortMediaName(MediaInfo info){
        if((info.NAME != "")&&(info.RELEASED != null)) {
            String result = info.NAME.replaceAll("\\s", "");
            result += "(" + info.RELEASED.getYear() + ")";

            return result;
        }
        else{
            return "";
        }
    }


    public int qualityStringIntoInteger(String qualityString){
        if(qualityString.equals("MAX")){
            return -1;
        }
        else if(qualityString.equals("MIN")){
            return 0;
        }
        else {
            try {
                return Integer.parseInt(qualityString.replaceAll("p",""));
            }
            catch (Exception e){
                log.error("Could not parse quality attribute as integer");
                return Integer.MAX_VALUE;
            }
        }
    }


    public String getNameFromScrapedHtml(Document document){
        String name;
        Elements matchedLinks = document.getElementsByClass("index_container");
        name = matchedLinks.get(0).getElementsByAttribute("title").get(0).text();

        return name;
    }


    public LocalDate getReleasedDatafromScrapedHtml(Document document){
        String dateAsString;
        LocalDate date;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        Elements matchedLinks = document.getElementsByClass("movie_info");
        dateAsString = matchedLinks.get(0).getElementsByTag("tr").get(2).getElementsByTag("td").get(1).text();

        try {
            date = LocalDate.parse(dateAsString, formatter);
            return date;
        }
        catch (Exception e){
            log.error(String.format("Scraped release date could not be parsed"));
            return null;
        }
    }


    public String getRedirectedMediaLink(String html){
        final String mediaWebsite = "thevideo.me";
        final Document document = Jsoup.parse(html);

        Elements matchedLinks = document.getElementsByTag("table");

        if(matchedLinks.isEmpty()){
            log.error("No media entries found on website, please try again!");
            return "";
        }

        int i = 1;
        String site;
        String urlExtension = "";
        for (Element matchedLink : matchedLinks) {
            if(matchedLink.hasAttr("class")) {
                site = "";
                try{
                    site = matchedLink.getElementsByClass("version_host").tagName("script").html().split("'")[1];
                }catch(Exception e){
                }
                if(site.equals(mediaWebsite)){
                    urlExtension = matchedLink.getElementsByAttribute("href").attr("href");
                    break;
                }
                i++;
            }
        }
        return urlExtension;
    }
}
