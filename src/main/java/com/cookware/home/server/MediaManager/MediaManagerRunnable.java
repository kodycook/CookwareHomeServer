package com.cookware.home.server.MediaManager;

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
    private final FileNameTools fileNameTools = new FileNameTools();
    private final DatabaseManager databaseManager = new DatabaseManager("media.db");
    private final FileTransferrer fileTransferrer = new FileTransferrer(databaseManager);
    private final DownloadManager downloadManager = new DownloadManager(databaseManager);
    private final boolean isDownloading = false;
    private static final Logger log = Logger.getLogger(MediaManagerRunnable.class);
    // TODO: Make the Path Strings part of configuration and find a better way to share the globals with DownloadManager
    public static String tempPath = "C:\\Users\\maste\\IdeaProjects\\CookwareHomeServer\\Media";
    public static String moviePath = "C:\\Users\\maste\\IdeaProjects\\CookwareHomeServer\\Media\\Movies";
    public static String episodePath = "C:\\Users\\maste\\IdeaProjects\\CookwareHomeServer\\Media\\TV";

    public MediaManagerRunnable(){
        databaseManager.initialiseDataBase();
    }

    @Override
    public void run()
    {
        boolean downloadSuccess;
        int index = 0;
        MediaInfo currentMedia;
        MediaInfo tempMedia;
        retrieveQueuedMediaFromDatabase(mediaQueue);

        fileTransferrer.start();

        while(true)
        {
            if (!isDownloading) {
                break;
            }

            while(mediaQueue.size()>index){
                // TODO: Add a method to sort the media queue by priority

                currentMedia = mediaQueue.get(index);
                if (currentMedia.TYPE.equals(MediaType.TV)){
                    index ++;
                }
                else {
                    tempMedia = downloadManager.downloadMedia(currentMedia);
                    if(tempMedia != null){
                        currentMedia = tempMedia;
                        updateState(currentMedia, DownloadState.TRANSFERRING);
                        // TODO: Update the path in the Database
                        // TODO: Add a method to move the media into the correct place and update the path in the database
                        mediaQueue.remove(index);
                    }
                    else {
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
                log.error("MediaManager thread interrupted", e);
                System.exit(-1);
            }
        }
    }

    public void updateState(MediaInfo mediaInfo, DownloadState downloadState){
        mediaInfo.STATE = downloadState;
        databaseManager.updateState(mediaInfo.ID, downloadState);
    }


    public void retrieveQueuedMediaFromDatabase(List<MediaInfo> mediaQueue) {
        List<MediaInfo> tempMediaQueue = databaseManager.getDownloadQueue();
        if(tempMediaQueue.size()!=0) {
            log.info(String.format("Retrieved %d pending downloads from Database", tempMediaQueue.size()));
            for (MediaInfo queuedMedia : tempMediaQueue) {
                mediaQueue.add(queuedMedia);
                log.info(queuedMedia.toString());
            }
        }
    }


    public void addNewMediaRequest(String url, int priority, String qualityString){
        final WebTools webTools = new WebTools();
        final List<MediaInfo> episodes = retrieveEpisodesFromUrl(url);
        MediaInfo info = new MediaInfo();

        // TODO: Clean up the writing to the database and the handling of MediaInfo Objects

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
                episodeInfo.PARENTSHOWNAME = info.NAME;
                episodeInfo.PRIORITY = info.PRIORITY;
                episodeInfo.QUALITY = info.QUALITY;
                episodeInfo = addMediaToDataBase(episodeInfo);
            }
        }
    }


    public ArrayList<MediaInfo> retrieveEpisodesFromUrl(String url){
        WebTools webTools = new WebTools();
        ArrayList<MediaInfo> episodeInfoList = new ArrayList<>();
        final String html = webTools.getWebPageHtml(url);
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
                                    episodeInfo.URL = webTools.extractBaseURl(url) + episode.getElementsByAttribute("href").attr("href");
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

        info.ID = fileNameTools.generateHashFromMediaInfo(info);

        if(mediaInfoValid(info)){
            success = databaseManager.addMediaToDatabase(info);
        }
        else {
            log.error("Media not added - mandartory attribute not set");
            return null;
        }

        if(success){
            mediaQueue.add(info);
            for(MediaInfo queuedMedia:mediaQueue){
                log.debug(queuedMedia.toString());
            }
        }
        else {
            if(info.TYPE.equals(MediaType.EPISODE)){
                log.info(String.format("Episode already in database: %s", info.toString()));
            }
            else if(info.TYPE.equals(MediaType.MOVIE)){
                log.info(String.format("Movie already in database: %s", info.toString()));
                return null;
            }
        }
        return info;
    }


    public MediaInfo scrapeMediaInfoFromUrl(MediaInfo info){
        String name;
        final WebTools webTools = new WebTools();
        final String scrapedHtml = webTools.getWebPageHtml(info.URL);

        if (!info.TYPE.equals(MediaType.EPISODE)){
            final Document document = Jsoup.parse(scrapedHtml);
            name = getNameFromScrapedHtml(document);
            info.NAME = name.substring(0,name.length()-9);
            info.RELEASED = getReleasedDatafromScrapedHtml(document);
        }

        // TODO: Move this code to the Media Downloader
//        completeExtractedMediaHostUrl = webTools.extractBaseURl(url) + getRedirectedMediaLink(scrapeHtml);
//        log.info(String.format("Extracted media URL:%s",completeExtractedMediaHostUrl));

        return info;
    }


    public boolean mediaInfoValid(MediaInfo info){
        return true;
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

    public void rebuildDataBaseFromPath(){
        // TODO: Implement this (rebuildDataBaseFromPath) method
    }
}
