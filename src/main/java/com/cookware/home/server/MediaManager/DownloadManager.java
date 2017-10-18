package com.cookware.home.server.MediaManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.SocketException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by Kody on 13/09/2017.
 */
public class DownloadManager {
    // TODO: Add returns to identify failed downloads
    // TODO: Add logs to announce the progress of downloads
    // TODO: Add timeout method on downloads
    // TODO: Implement interfaces to create different versions of the web scrapers
    private static final Logger log = Logger.getLogger(DownloadManager.class);
    private final WebTools webTools = new WebTools();
    private final FileNameTools fileNameTools = new FileNameTools();
    private final DatabaseManager databaseManager;
    private String fileType;

    public DownloadManager(DatabaseManager mDatabaseManager){
        this.databaseManager = mDatabaseManager;
    }

    public MediaInfo downloadMedia(MediaInfo mediaInfo){
        log.info(String.format("Starting download: %s", mediaInfo.toString()));
        boolean downloadSuccess;
        final DownloadLink embeddedMediaUrlAndQuality = bridgeToVideoMe(mediaInfo);
        if(embeddedMediaUrlAndQuality == null){
            log.error(String.format("This media item will need to be downloaded manually and has been set to the \"IGNORED\" state in the Database"));
            mediaInfo.STATE = DownloadState.IGNORED;
            databaseManager.updateState(mediaInfo.ID, mediaInfo.STATE);
            return null;
        }
        mediaInfo.QUALITY = embeddedMediaUrlAndQuality.quality;
        mediaInfo.PATH = fileNameTools.getFullFileNameFromMediaInfo(mediaInfo);

        mediaInfo.PATH += fileType;

        databaseManager.updatePath(mediaInfo.ID, (mediaInfo.PATH));
        databaseManager.updateQuality(mediaInfo.ID, mediaInfo.QUALITY);
        downloadSuccess = newDownload(embeddedMediaUrlAndQuality.url, mediaInfo.PATH, mediaInfo.TYPE);
        if(!downloadSuccess){
            log.error(String.format("Issue downloading: %s",mediaInfo.toString()));
            mediaInfo.STATE = DownloadState.FAILED;
            databaseManager.updateState(mediaInfo.ID, mediaInfo.STATE);
            return null;
        }


        mediaInfo.STATE = DownloadState.TRANSFERRING;
        databaseManager.updateState(mediaInfo.ID, mediaInfo.STATE);
        databaseManager.updateDownloadDate(mediaInfo.ID, LocalDate.now());
        log.info(String.format("Finished downloading: %s",mediaInfo.toString()));
        return mediaInfo;
    }

    private DownloadLink bridgeToVideoMe(MediaInfo mediaInfo){
        final String html = webTools.getWebPageHtml(mediaInfo.URL);
        if(html.equals("")){
            System.exit(1);
        }
        final String urlExtension = findVideoMeLinkInHtml(html);
        if (urlExtension == null){
            log.error(String.format("No links found at %s", mediaInfo.URL));
            return null;
        }
        else if (urlExtension.equals("")){
            log.error(String.format("No video.me link found at %s", mediaInfo.URL));
            return null;
        }
        final String videoMeUrl = webTools.extractBaseURl(mediaInfo.URL) + urlExtension;
        String redirectedUrl = webTools.getRedirectedUrl(videoMeUrl);
        if(redirectedUrl.equals("")) {
            log.error(String.format("Could not obtain redirect URL for %s", videoMeUrl));
            return null;
        }
        List<DownloadLink> mediaDownloadLinks = extractAllMediaUrls(redirectedUrl);
        if(mediaDownloadLinks == null){
            return null;
        }
        else if(mediaDownloadLinks.size() == 0){
            log.error(String.format("Could not find media URLS at %s", redirectedUrl));
            return null;
        }

        return selectBestLinkByQuality(mediaDownloadLinks, mediaInfo.QUALITY);
    }


    public String findVideoMeLinkInHtml(String html){
        Document document = Jsoup.parse(html);
        Elements matchedLinks = document.getElementsByTag("table");
        if(matchedLinks.isEmpty()){
            log.debug("No entries found, please try again!");

            return null;
        }

        int i = 1;
        String site;
        String url = "";
        for (Element matchedLink : matchedLinks) {
            if(matchedLink.hasAttr("class")) {
                site = "";
                if(matchedLink.getElementsByClass("version_host").tagName("script").html().split("'").length > 1) {
                    try {
                        site = matchedLink.getElementsByClass("version_host").tagName("script").html().split("'")[1];
                    } catch (Exception e) {
                        log.error(e);
                    }
                    if(site.equals("thevideo.me")){
                        url = matchedLink.getElementsByAttribute("href").attr("href");
                        break;
                    }
                    i++;
                }
            }
        }
        return url;
    }


    public List<DownloadLink> extractAllMediaUrls(String url){
        // TODO: Clean up this function
        Scanner scan;
        String logicalLine;
        String firstPage = webTools.getWebPageHtml(url);
        Document document = Jsoup.parse(firstPage);
        if(document.getElementsByAttributeValue("name", "hash").size() == 0){
            log.error(String.format("Error retrieving hash code from %s",url));
            return null;
        }
        String hash = document.getElementsByAttributeValue("name", "hash").get(0).attr("value");

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("_vhash", "i1102394cE"));
        params.add(new BasicNameValuePair("gfk", "i22abd2449"));
        params.add(new BasicNameValuePair("hash", hash));
        params.add(new BasicNameValuePair("inhu", "foff"));

        String secondPage = webTools.getWebPageHtml(url, WebTools.HttpRequestType.POST, params);

        int startOfUrlCodeInWebPage = secondPage.indexOf("lets_play_a_game='");

        scan = new Scanner(secondPage.substring(startOfUrlCodeInWebPage+"lets_play_a_game='".length()));
        scan.useDelimiter(Pattern.compile("'"));
        logicalLine = scan.next();

        String thirdPage = webTools.getWebPageHtml("https://thevideo.me/vsign/player/"+logicalLine);

        String[] encodedAttributes = thirdPage.split("\\|");

        String encodedHash = "";
        for (String temp:encodedAttributes){
            if(temp.length()==282){
                encodedHash = temp;
                break;
            }
        }

        int startOfLinksInWebPage = secondPage.indexOf("sources: [");
        scan = new Scanner(secondPage.substring(startOfLinksInWebPage+11));
        scan.useDelimiter(Pattern.compile("}]"));
        logicalLine = scan.next();
        String[] rawMediaSources = logicalLine.split("\\},\\{");

        List<DownloadLink> mediaLinks = new ArrayList<DownloadLink>();
        for (String source:rawMediaSources){
            String[] rawSeperatedValues = source.split("\"");
            try{
                String downloadUrl = rawSeperatedValues[3];
                // TODO: Find another method other than a global to transfer the fileType
                fileType = downloadUrl.substring(downloadUrl.length()-4);

                int quality = Integer.parseInt(rawSeperatedValues[7].replaceAll("[^0-9]", ""));
                mediaLinks.add(new DownloadLink(downloadUrl + "?direct=false&ua=1&vt=" + encodedHash, quality));
            } catch (Exception e){
                log.error("Seperation of download links failed");
            }
        }

        return mediaLinks;
    }


    private DownloadLink selectBestLinkByQuality(List<DownloadLink> mediaLinks, int quality){
        if(quality == -1){
            return selectLinkWithHighestQuality(mediaLinks);
        }
        else if (quality == 0){
            return selectLinkWithLowestQuality(mediaLinks);
        }
        else{
            return slectLinkClosestToSpecifiedQuality(mediaLinks, quality);
        }
    }



    private DownloadLink selectLinkWithHighestQuality(List<DownloadLink> mediaLinks){
        int highestQuality = 0;
        DownloadLink result = null;
        for (DownloadLink mediaLink:mediaLinks){
            if(highestQuality < mediaLink.quality){
                result = mediaLink;
                highestQuality = mediaLink.quality;
            }
        }
        return result;
    }

    private DownloadLink selectLinkWithLowestQuality(List<DownloadLink> mediaLinks){
        int lowestQuality = Integer.MAX_VALUE;
        DownloadLink result = null;
        for (DownloadLink mediaLink:mediaLinks){
            if(lowestQuality > mediaLink.quality){
                result = mediaLink;
                lowestQuality = mediaLink.quality;
            }
        }
        return result;
    }


    private DownloadLink slectLinkClosestToSpecifiedQuality(List<DownloadLink> mediaLinks, int specifiedQuality){
        int finalQualityDifference = Integer.MAX_VALUE;
        int currentQualityDifference;
        DownloadLink result = null;
        for (DownloadLink mediaLink:mediaLinks){
            currentQualityDifference = Math.abs(mediaLink.quality-specifiedQuality);
            if(currentQualityDifference < finalQualityDifference){
                result = mediaLink;
                finalQualityDifference = currentQualityDifference;
            }
        }
        return result;
    }


    public boolean newDownload(String downloadUrl, String downloadFilename, MediaType type){
        String downloadFilepath = "";
        downloadFilepath = MediaManager.tempPath;
        File output = new File(downloadFilepath, downloadFilename);
        try {

            return downloadMediaToFile(downloadUrl, output);
        } catch (Throwable throwable) {
            log.error(String.format("Error downloading media from %s to %s:\n",downloadUrl, downloadFilename), throwable);
            return false;
        }
    }


    private boolean downloadMediaToFile(String downloadUrl, File outputfile) throws Throwable {
        HttpGet httpget2 = new HttpGet(downloadUrl);
        long startTime = System.currentTimeMillis();

        log.debug("Executing " + httpget2.getURI());
        HttpClient httpclient2 = new DefaultHttpClient();
        HttpResponse response2 = httpclient2.execute(httpget2);
        HttpEntity entity2 = response2.getEntity();
        
        if (entity2 != null && response2.getStatusLine().getStatusCode() == 200) {
            long length = entity2.getContentLength();
            InputStream instream2 = entity2.getContent();
            System.out.println("Writing " + length + " bytes to " + outputfile);
            if (outputfile.exists()) {
                outputfile.delete();
            }
            FileOutputStream outstream = new FileOutputStream(outputfile);
            int i = 1;
            try {
                byte[] buffer = new byte[2048];
                int count = -1;
                try {
                    while ((count = instream2.read(buffer)) != -1) {
                        printProgress(startTime, (int) length / 2048 + 1, i);
                        i++;
                        outstream.write(buffer, 0, count);
                    }
                }
                catch (SocketException e){
                    log.error(String.format("Issue downloading: %s", downloadUrl));
                    return false;
                }
                outstream.flush();
            } finally {
                outstream.close();
            }
            System.out.print("\n\n");
        }
        return true;
    }


    private void printProgress(long startTime, long total, long current) {
        long eta = current == 0 ? 0 :
                (total - current) * (System.currentTimeMillis() - startTime) / (current);


        String etaHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
                        TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

        StringBuilder string = new StringBuilder(140);
        int percent = (int) (current * 100 / total);
        string
                .append('\r')
                .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
                .append(String.format(" %d%% [", percent))
                .append(String.join("", Collections.nCopies(percent, "=")))
                .append('>')
                .append(String.join("", Collections.nCopies(100 - percent, " ")))
                .append(']')
                .append(String.join("", Collections.nCopies((int) (Math.log10(total)) - (int) (Math.log10(current)), " ")))
                .append(String.format(" %.2f/%.2fMB ", ((double) current)/512, ((double) total)/512))
                .append(String.format("(%.2fMB/s), ", ((double) current)/512/TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)))
                .append(String.format("ETA: %s", etaHms));

        System.out.print(string);
    }


    public class DownloadLink{
        public String url;
        public int quality;

        private DownloadLink(String mUrl, int mQuality){
            this.url = mUrl;
            this.quality = mQuality;
        }

        public String toString(){
            return String.format("(%dp) %s",this.quality, this.url);
        }
    }
}
