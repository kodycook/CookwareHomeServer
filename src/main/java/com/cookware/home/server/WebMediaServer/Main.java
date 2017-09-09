package com.cookware.home.server.WebMediaServer;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.Scanner;



/**
 * Created by Kody on 13/08/2017.
 */
public class Main {
    private static AutomateLevel automate = AutomateLevel.NONE;

    public static void main( String[] args ) {

        String baseUrl = "http://www.primewire.ag";

        WebMediaBridge mediaBridge = new WebMediaBridge();
        DownloadScheduler scheduler = new DownloadScheduler();
//        scheduler.start();
        String downloadUrl = "";

        while(true) {
            if (automate == AutomateLevel.SKIP_SCRAPE) {
                try {
                    // TODO: Udate this automatic method to automate selection (maybe even remove it
                    downloadUrl = mediaBridge.getDownloadUrl(baseUrl, "http://www.primewire.ag/watch-2749527-Guardians-of-the-Galaxy-online-free");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                String search = getSearchQuery();

                Media media = scrapeMediaFromSearch(search, baseUrl);

                scheduler.addMedia(media);

                scheduler.download();
            }
        }




//




//        WebMediaDownloader primewireDownloader = new WebMediaDownloader();
//        primewireDownloader.newDownload(MediaType.TV, "https://n9715.thevideo.me:8777/o2jtatel3goammfvg7rfae6rgaal2xr5pdrcxsfgdt6mm3jfwktnglp5qjylf623dycg3bc6veh2pbnq2lnoctws4tnfamhsbat4vy3hsuk2nqo76wo54yhwlquc2zupqdgc3ho6tzqx624nzpqwvlgdji46wjoa2xi7gh2bnp4rc3mu657qxfyh7rrwn3qct62qspms2rqm2fem45hlkgs3jiedysfwunivrwkkdeng7gfzp7ylozksbmj2hk2uydfdi4otauzve4wywckpn5i4i6na/v.mp4?direct=false&ua=1&vt=n35lngye3zlwowtngom4aimqdj77zdieuxa6rgov5yoq2vekm5l3ck6l4d36minkoelgww7zhcc5nnwdyr6q4m2fsmsihywietuztgplmkpvddbzi3c7rpwrljn5pl5xkjpsrt6xupcjco2m2v4mp4lazc4mekgyvzv45bhhspcvlcifegk5vuhfvqldrcrabf4pdwib3lgd4jxangqkm66e37siololvc23zfkftegabv67ugpftdyglejflwdukexetwjxdfzm4zhtdo4r3ul5va","The Bone Collector.mp4");

    }

    private static String getSearchQuery() {
        String search;
        if (automate == AutomateLevel.NONE) {
            Scanner consoleScanner = new Scanner(System.in);
            System.out.print("Select a Movie/TV Show to search: ");
            search = consoleScanner.useDelimiter("\n").next();
        } else {
            search = "Guardians of the Galaxy";
            //search = "Game of Thrones";
        }
        search = search.replace(' ', '+');

        return search;
    }

    private static Media scrapeMediaFromSearch(String search, String baseUrl) {
        WebMediaScraper primewireScraper = new WebMediaScraper();
        ArrayList<HttpParameter> httpParameters = new ArrayList<HttpParameter>();

        httpParameters.add(new HttpParameter("search_keywords", search));
        Media media = null;
        try {
            media = primewireScraper.findMedia(baseUrl, httpParameters);
//            System.out.println(media.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return media;
    }
}
