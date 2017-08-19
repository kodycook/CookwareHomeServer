package com.cookware.home.server;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Kody on 13/08/2017.
 */
public class WebMediaBridge_Primewire_TheVideo {
    private String inUrl;
    private String baseUrl;

    public WebMediaBridge_Primewire_TheVideo(){
    }

    public String getDownloadUrl(String mBaseUrl, String mInUrl) throws Exception{
//        this.inUrl = mInUrl;
//        this.baseUrl = mBaseUrl;
//        String charset = "UTF-8";
//
//        URLConnection connection = new URL(mInUrl).openConnection();
//        connection.setRequestProperty("Accept-Charset", charset);
//        InputStream response = connection.getInputStream();
//
//        String html = "";
//        try (Scanner scanner = new Scanner(response)) {
//            html = scanner.useDelimiter("\\A").next();
//        }
//
//        Document document = Jsoup.parse(html);
////        System.out.println(document.toString());
//
//
//        Elements matchedLinks = document.getElementsByTag("table");
//
//        if(matchedLinks.isEmpty()){
//            System.out.println("No entries found, please try again!");
//
//            return null;
//        }
//
//        int i = 1;
//        String site;
//        String url = "";
//        for (Element matchedLink : matchedLinks) {
//            if(matchedLink.hasAttr("class")) {
//                site = matchedLink.getElementsByClass("version_host").tagName("script").html().split("'")[1];
////                System.out.println(String.format("Op %d: %s", i, site));
//                if(site.equals("thevideo.me")){
//                    url = this.baseUrl + matchedLink.getElementsByAttribute("href").attr("href");
//                    break;
//                }
//                i++;
//            }
//        }
//
        HttpRedirectExample temp = new HttpRedirectExample("http://thevideo.me/bnrfiudhjxsv");
        String url2 = temp.redirect();
        System.out.println(url2);

//
////        html = "";
////        try (Scanner scanner = new Scanner(response)) {
////            html = scanner.useDelimiter("\\A").next();
////        }
//
////        document = Jsoup.parse(html);
////        System.out.println(document.toString());


        return "Nothing here yet";
    }
}