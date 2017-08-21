package com.cookware.home.server;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Kody on 13/08/2017.
 */
public class WebMediaBridge {
    private String inUrl;
    private String baseUrl;

    public WebMediaBridge(){
    }

    public String getDownloadUrl(String mBaseUrl, String mInUrl) throws Exception{
        this.inUrl = mInUrl;
        this.baseUrl = mBaseUrl;
        String charset = "UTF-8";

        CookieHandler.setDefault(new CookieManager());

        URLConnection connection = new URL(mInUrl).openConnection();
        connection.setRequestProperty("Accept-Charset", charset);
        InputStream response = connection.getInputStream();

        String html = "";
        try (Scanner scanner = new Scanner(response)) {
            html = scanner.useDelimiter("\\A").next();
        }

        Document document = Jsoup.parse(html);
//        System.out.println(document.toString());


        Elements matchedLinks = document.getElementsByTag("table");

        if(matchedLinks.isEmpty()){
            System.out.println("No entries found, please try again!");

            return null;
        }

        int i = 1;
        String site;
        String url = "";
        for (Element matchedLink : matchedLinks) {
            if(matchedLink.hasAttr("class")) {
                site = matchedLink.getElementsByClass("version_host").tagName("script").html().split("'")[1];
//                System.out.println(String.format("Op %d: %s", i, site));
                if(site.equals("thevideo.me")){
                    url = this.baseUrl + matchedLink.getElementsByAttribute("href").attr("href");
                    break;
                }
                i++;
            }
        }

        HttpRedirectExample temp = new HttpRedirectExample(url);
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

    private void GetAndPost() throws Exception{ //TODO: PICK UP WORK FROM HERE

        DefaultHttpClient httpclient = new DefaultHttpClient();

        HttpGet httpget = new HttpGet("https://portal.sun.com/portal/dt");

        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        System.out.println("Login form get: " + response.getStatusLine());
        if (entity != null) {
            entity.consumeContent();
        }
        System.out.println("Initial set of cookies:");
        List<Cookie> cookies = httpclient.getCookieStore().getCookies();
        if (cookies.isEmpty()) {
            System.out.println("None");
        } else {
            for (int i = 0; i < cookies.size(); i++) {
                System.out.println("- " + cookies.get(i).toString());
            }
        }

        HttpPost httpost = new HttpPost("https://portal.sun.com/amserver/UI/Login?" +
                "org=self_registered_users&" +
                "goto=/portal/dt&" +
                "gotoOnFail=/portal/dt?error=true");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("IDToken1", "username"));
        nvps.add(new BasicNameValuePair("IDToken2", "password"));

        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

        response = httpclient.execute(httpost);
        entity = response.getEntity();

        System.out.println("Login form get: " + response.getStatusLine());
        if (entity != null) {
            entity.consumeContent();
        }

        System.out.println("Post logon cookies:");
        cookies = httpclient.getCookieStore().getCookies();
        if (cookies.isEmpty()) {
            System.out.println("None");
        } else {
            for (int i = 0; i < cookies.size(); i++) {
                System.out.println("- " + cookies.get(i).toString());
            }
        }

        // When HttpClient instance is no longer needed,
        // shut down the connection manager to ensure
        // immediate deallocation of all system resources
        httpclient.getConnectionManager().shutdown();
    }
}