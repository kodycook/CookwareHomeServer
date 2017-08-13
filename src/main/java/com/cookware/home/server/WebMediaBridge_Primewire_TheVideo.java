package com.cookware.home.server;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

/**
 * Created by Kody on 13/08/2017.
 */
public class WebMediaBridge_Primewire_TheVideo {
    private String inUrl;

    public WebMediaBridge_Primewire_TheVideo(){
    }

    public String getDownloadUrl(String mInUrl) throws Exception{
        this.inUrl = mInUrl;
        String charset = "UTF-8";

        URLConnection connection = new URL(mInUrl).openConnection();
        connection.setRequestProperty("Accept-Charset", charset);
        InputStream response = connection.getInputStream();

        String html = "";
        try (Scanner scanner = new Scanner(response)) {
            html = scanner.useDelimiter("\\A").next();
        }

        Document document = Jsoup.parse(html);
        System.out.println(document.toString());

        return "Nothing here yet";
    }
}
