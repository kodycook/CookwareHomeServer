package com.cookware.home.server.MediaManager;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.attribute.standard.Media;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by Kody on 5/09/2017.
 */
public class MediaManagerRunnable implements Runnable{
    // TODO: Add in a Statistics Manager
    private final List<MediaInfo> mediaQueue = Collections.synchronizedList(new ArrayList<MediaInfo>());
    private final FileNameTools fileNameTools = new FileNameTools();
    private final DatabaseManager databaseManager = new DatabaseManager("media.db");
    private final FileTransferrer fileTransferrer = new FileTransferrer(databaseManager);
    private final DownloadManager downloadManager = new DownloadManager(databaseManager);
    private final boolean isDownloading = true;
    private static final Logger log = Logger.getLogger(MediaManagerRunnable.class);

    public MediaManagerRunnable(){
        databaseManager.initialise();
    }

    @Override
    public void run()
    {
        int index = 0;
        MediaInfo currentMedia;
        MediaInfo tempMedia;
        resetFailedDownloadMediaItems();
        retrieveQueuedMediaFromDatabase(mediaQueue);


        fileTransferrer.start();


        while(true)
        {
            // TODO: Write the Scheduler
            if (!isDownloading) {
                break;
            }


            while(mediaQueue.size()>index){


                sortMediaQueue();

                currentMedia = mediaQueue.get(index);
                if (currentMedia.TYPE.equals(MediaType.TV)){
                    index ++;
                }
                else {
                    updateState(currentMedia, DownloadState.DOWNLOADING);
                    tempMedia = downloadManager.downloadMedia(currentMedia);
                    if(tempMedia != null){
                        currentMedia = tempMedia;
                        updateState(currentMedia, DownloadState.TRANSFERRING);
                        mediaQueue.remove(index);
                        // TODO: Send a message to plex server to refresh
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


    private void resetFailedDownloadMediaItems(){
        List<MediaInfo> halfDownloadedMedia = databaseManager.getMediaItemsWithState(DownloadState.DOWNLOADING);
        List<MediaInfo> failedMedia = databaseManager.getMediaItemsWithState(DownloadState.FAILED);

        for(MediaInfo mediaInfo: halfDownloadedMedia){
            updateState(mediaInfo, DownloadState.PENDING);
            databaseManager.updatePriority(mediaInfo.ID, 5);
            log.info(String.format("Half finished download reset: %s", mediaInfo.toString()));
        }

        for(MediaInfo mediaInfo: failedMedia){
            updateState(mediaInfo, DownloadState.PENDING);
            databaseManager.updatePriority(mediaInfo.ID, 5);
            log.info(String.format("Half finished download reset: %s", mediaInfo.toString()));
        }
    }

    private void sortMediaQueue(){ // ASK WILL HOW HE UNIT TEST'S PRIVATE CLASSES
        // TODO: Add Iterator to remove elements out of tempmedia queue as it sorts
        List<MediaInfo>  tempMediaQueue = new ArrayList<MediaInfo>();
        for (MediaInfo currentItem : mediaQueue){
            tempMediaQueue.add(currentItem);
        }

        mediaQueue.clear();
        for (int currentPriority = 0; currentPriority <= 5; currentPriority++){
            for (MediaInfo currentItem : tempMediaQueue){
                if (currentItem.PRIORITY == currentPriority){
                    mediaQueue.add(currentItem);
                }
            }
        }
    }


    public void retrieveQueuedMediaFromDatabase(List<MediaInfo> mediaQueue) {
        List<MediaInfo> tempMediaQueue = databaseManager.getDownloadQueue();
        if(tempMediaQueue.size()!=0) {
            log.info(String.format("Retrieved %d pending downloads from Database", tempMediaQueue.size()));
            for (MediaInfo queuedMedia : tempMediaQueue) {
                mediaQueue.add(queuedMedia);
                log.debug(queuedMedia.toString());
            }
        }
    }


    public void addNewMediaRequest(String url, int priority, String qualityString){
        final WebTools webTools = new WebTools();
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
                episodeInfo.PARENTSHOWNAME = info.NAME;
                episodeInfo.PRIORITY = info.PRIORITY;
                episodeInfo.QUALITY = info.QUALITY;
                episodeInfo = addMediaToDataBase(episodeInfo);
            }
        }
        log.info(String.format("Finished adding %s to Data Base", info.NAME));
    }


    public ArrayList<MediaInfo> retrieveEpisodesFromUrl(String url){
        WebTools webTools = new WebTools();
        ArrayList<MediaInfo> episodeInfoList = new ArrayList<>();
        final String html = webTools.getWebPageHtml(url);
        log.debug(String.format("HTML for Tv Show:\n%s",html));

        final Document document = Jsoup.parse(html);
        final Elements tvSeasons = document.getElementsByClass("tv_container");
        MediaInfo episodeInfo;
        int seasonNumber, episodeNumber;
        String episodeText, episodeReleaseDateAsString, episodeName;
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
                            if(episodeAttributes.length < 3){
                                log.warn(String.format("Skipped S%dE%d (%s) at %s due to error parsing details",
                                        seasonNumber,
                                        episodeNumber,
                                        episodeAttributes[1],
                                        url));
                                continue;
                            }
                            if((episodeNumber == 0) || (episodeNumber > 99)){
                                log.warn(String.format("Skipped S%dE%d (%s) due exceeding episode limits [1-99]",
                                        seasonNumber,
                                        episodeNumber,
                                        episodeAttributes[1],
                                        url));
                                continue;
                            }

                            episodeReleaseDateAsString = episodeAttributes[episodeAttributes.length-1].substring(0,10);

                            episodeName = episodeAttributes[1];
                            for (int i = 2; i <= episodeAttributes.length-2; i++){ // Episode contains " - " in the title
                                episodeName += " - " + episodeAttributes[i];
                            }

                            episodeInfo = new MediaInfo();
                            episodeInfo.NAME = episodeName;
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
            log.error("Media not added - mandatory attribute not set");
            return null;
        }

        if(success){
            mediaQueue.add(info);
            log.debug(info.toString());
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
