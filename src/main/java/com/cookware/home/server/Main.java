package com.cookware.home.server;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Kody on 13/08/2017.
 */
public class Main {

    public static void main( String[] args ) {

        Scanner consoleScanner = new Scanner(System.in);

//        System.out.print("Select a Movie/TV Show to search: ");
//        String search = consoleScanner.useDelimiter("\n").next();
        String search = "Guardians of the Galaxy";
        search = search.replace(' ', '+');

        WebMediaScraper_Primewire primewireScraper = new WebMediaScraper_Primewire();
        ArrayList<HttpParameter> httpParameters = new ArrayList<HttpParameter>();

        //httpParameters.add(new HttpParameter("search_keywords","Game+of+Thrones"));
        httpParameters.add(new HttpParameter("search_keywords",search));
        Media media = null;
        try {
            media = primewireScraper.findMedia("http://www.primewire.ag", httpParameters);
            System.out.println(media.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        WebMediaBridge_Primewire_TheVideo mediaBridge = new WebMediaBridge_Primewire_TheVideo();

        if(media instanceof TvShow){
            System.out.println("No bridging of Tv Shows yet");
            //TODO: Add in bridging of TV Shows
        }
        else if (media instanceof Movie){
            try {
                String downloadUrl = mediaBridge.getDownloadUrl("http://www.primewire.ag", media.getLink());
                System.out.println(downloadUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



//        WebMediaDownloader primewireDownloader = new WebMediaDownloader();
//        primewireDownloader.newDownload(MediaType.TV, "https://n9715.thevideo.me:8777/o2jtatel3goammfvg7rfae6rgaal2xr5pdrcxsfgdt6mm3jfwktnglp5qjylf623dycg3bc6veh2pbnq2lnoctws4tnfamhsbat4vy3hsuk2nqo76wo54yhwlquc2zupqdgc3ho6tzqx624nzpqwvlgdji46wjoa2xi7gh2bnp4rc3mu657qxfyh7rrwn3qct62qspms2rqm2fem45hlkgs3jiedysfwunivrwkkdeng7gfzp7ylozksbmj2hk2uydfdi4otauzve4wywckpn5i4i6na/v.mp4?direct=false&ua=1&vt=n35lngye3zlwowtngom4aimqdj77zdieuxa6rgov5yoq2vekm5l3ck6l4d36minkoelgww7zhcc5nnwdyr6q4m2fsmsihywietuztgplmkpvddbzi3c7rpwrljn5pl5xkjpsrt6xupcjco2m2v4mp4lazc4mekgyvzv45bhhspcvlcifegk5vuhfvqldrcrabf4pdwib3lgd4jxangqkm66e37siololvc23zfkftegabv67ugpftdyglejflwdukexetwjxdfzm4zhtdo4r3ul5va","The Bone Collector.mp4");

    }

}
