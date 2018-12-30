package com.cookware.home.MediaManagerCore;

import com.cookware.home.MediaManagerCommon.Tools.FileNameTools;
import com.cookware.home.MediaManagerCommon.DataTypes.Config;
import com.cookware.home.MediaManagerCommon.DataTypes.DownloadState;
import com.cookware.home.MediaManagerCommon.DataTypes.MediaInfo;
import com.cookware.home.MediaManagerCommon.DataTypes.MediaType;
import com.cookware.home.MediaManagerCommon.Managers.DatabaseManager;
import com.cookware.home.MediaManagerCommon.Managers.DownloadManager;
import com.cookware.home.MediaManagerCommon.Managers.ScheduleManager;
import com.cookware.common.Tools.*;
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
    private static final Logger log = Logger.getLogger(MediaManagerRunnable.class);
    // TODO: Add in a Statistics Manager
    // TODO: Sort out syncronised access to variables
    private final DirectoryTools directoryTools = new DirectoryTools();
    private final WebTools webTools = new WebTools();
    private final FileNameTools fileNameTools = new FileNameTools();
    private final Config config;
    private final DatabaseManager databaseManager;
    private final FileTransferrer fileTransferrer;
    private final DownloadManager downloadManager;
    private final ScheduleManager scheduleManager;

    public MediaManagerRunnable(Config mConfig){
        config = mConfig;
        this.databaseManager = new DatabaseManager(config.databasePath);
        fileTransferrer = new FileTransferrer(databaseManager, config);
        downloadManager = new DownloadManager(databaseManager, config);
        scheduleManager = new ScheduleManager(config);

        databaseManager.initialise();
    }

    @Override
    public void run()
    {
        final List<MediaInfo> mediaQueue = new ArrayList<MediaInfo>();
        boolean firstLoop;
        boolean hasDownload = false;
        MediaInfo currentMedia;

        // TODO: Update TV Show object when all episodes are downloaded
        // TODO: Find a way to force download objects with a priority of 0
        // TODO: Check internet connectivity before attempting downloads
        // TODO: BUG - Seems to be an issue where scheduler starting without a VPN causes an issue which isn't logged - it just stops the process

        fileTransferrer.start();

        while(true){
            // TODO: Find a way to stop resetting firstLoop if the media queue is empty
            firstLoop = true;
            while (scheduleManager.isDownloading()){
                mediaQueue.clear();

                if(!webTools.checkInternetConnection()){
                    break;
                }

                if(!webTools.checkVpnConnection()){
                    break;
                }

                if(firstLoop){
                    resetFailedDownloadMediaItems();
                    retrieveQueuedMediaFromDatabase(mediaQueue);
                    if(mediaQueue.size()!=0) {
                        log.info(String.format("Retrieved %d pending downloads from Database", mediaQueue.size()));
                    }
                    if (!directoryTools.checkIfNetworkLocationAvailable(config.finalPath)){
                        log.warn("Media Storage not available");
                    }
                    else {
                        log.info("Media Storage detected");
                    }
                    firstLoop = false;
                }
                else {
                    retrieveQueuedMediaFromDatabase(mediaQueue);
                }

                currentMedia = getNextItemForDownload(mediaQueue);
                if (currentMedia == null){
                    break;
                }
                else {
                    hasDownload = true;
                }

                updateState(currentMedia, DownloadState.DOWNLOADING);
                downloadManager.downloadMedia(currentMedia);
            }


            if (hasDownload){
                hasDownload = false;
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                log.error("MediaManagerCore thread interrupted", e);
                System.exit(1);
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
            log.info(String.format("Half finished download reset: %s", mediaInfo.toString()));
        }

        for(MediaInfo mediaInfo: failedMedia){
            updateState(mediaInfo, DownloadState.PENDING);
            databaseManager.updatePriority(mediaInfo.ID, 5);
            log.info(String.format("Half finished download reset: %s", mediaInfo.toString()));
        }
    }

    private MediaInfo getNextItemForDownload(List<MediaInfo> mediaQueue ){

        for (int currentPriority = 0; currentPriority <= 5; currentPriority++){
            for (MediaInfo currentItem : mediaQueue){
                if ((currentItem.PRIORITY == currentPriority) && (!currentItem.TYPE.equals(MediaType.TV))) {
                    return currentItem;
                }
            }
        }
        return null;
    }


    public void retrieveQueuedMediaFromDatabase(List<MediaInfo> mediaQueue) {
        List<MediaInfo> tempMediaQueue = databaseManager.getDownloadQueue();
        if(tempMediaQueue.size()!=0) {
            for (MediaInfo queuedMedia : tempMediaQueue) {
                mediaQueue.add(queuedMedia);
            }
        }
    }


    public String addNewMediaRequest(String url, int priority, String qualityString){
        final List<MediaInfo> episodes = retrieveEpisodesFromUrl(url);
        MediaInfo info = new MediaInfo();

        if(!webTools.checkInternetConnection()){
            return "Error - Internet not connected";
        }

        if(!webTools.checkVpnConnection()){
            return "Error - VPN not connected";
        }

        info.URL = url;
        info.QUALITY = qualityStringIntoInteger(qualityString);
        info.PRIORITY = priority;

        if (episodes.isEmpty()) {
            info.TYPE = MediaType.MOVIE;
            if(!addMediaToDataBase(info)){
                return String.format("%s already in database", info.NAME);
            }
        }
        else {
            info.TYPE = MediaType.TV;
            // ASK WILL IF IT'S OK TO MODIFY AN OBJECT WHOSE POINTER IS PASSED AS A PARAMETER OR SHOULD THE OBJECT POINTER BE RETURNED INDICATING THAT THE OBJECT HAS BEEN MODIFIED
            if(!addMediaToDataBase(info)){
                return String.format("%s already in database", info.NAME);
            }

            for(MediaInfo episodeInfo: episodes){
                episodeInfo.TYPE = MediaType.EPISODE;
                episodeInfo.PARENTSHOWID = info.ID;
                episodeInfo.PARENTSHOWNAME = info.NAME;
                episodeInfo.PRIORITY = info.PRIORITY;
                episodeInfo.QUALITY = info.QUALITY;
                addMediaToDataBase(episodeInfo);
            }
        }
        log.info(String.format("Finished adding %s to Database", info.NAME));
        return String.format("Successfully added %s to Database", info.NAME);
    }


    public ArrayList<MediaInfo> retrieveEpisodesFromUrl(String url){
        WebTools webTools = new WebTools();
        ArrayList<MediaInfo> episodeInfoList = new ArrayList<>();
        final String html = webTools.getWebPageHtml(url);
        log.debug(String.format("HTML for Primewire media page (%s):\n%s",url, html));

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


    public boolean addMediaToDataBase(MediaInfo info){
        Boolean success;
        if (!info.TYPE.equals(MediaType.EPISODE)) {
            scrapeMediaInfoFromUrl(info);
        }

        info.ID = fileNameTools.generateHashFromMediaInfo(info);

//        if(info.isComplete()){
            success = databaseManager.addMediaToDatabase(info);
//        }
//        else {
//            log.error("Media not added - mandatory attribute not set");
//            return false;
//        }

        if(!success) {
            if(info.TYPE.equals(MediaType.EPISODE)){
                log.info(String.format("Episode already in database: %s", info.toString()));
                return false;
            }
            else if(info.TYPE.equals(MediaType.MOVIE)){
                log.warn(String.format("Movie already in database: %s", info.toString()));
                return false;
            }
        }
        return true;
    }


    public void scrapeMediaInfoFromUrl(MediaInfo info){
        String name;
        final WebTools webTools = new WebTools();
        final String scrapedHtml = webTools.getWebPageHtml(info.URL);

        if (!info.TYPE.equals(MediaType.EPISODE)){
            final Document document = Jsoup.parse(scrapedHtml);
            name = getNameFromScrapedHtml(document);
            info.NAME = name.substring(0,name.length()-9);
            info.RELEASED = getReleasedDatafromScrapedHtml(document);
        }

        return;
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
