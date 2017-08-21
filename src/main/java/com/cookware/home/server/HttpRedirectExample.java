package com.cookware.home.server;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRedirectExample {
    String url;

    public HttpRedirectExample(String mUrl) {
        this.url = mUrl;
    }


    public String redirect() {

        CookieHandler.setDefault(new CookieManager());
        try {
//            String url = "http://www.twitter.com";

            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setReadTimeout(5000);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.addRequestProperty("Referer", "google.com");

            System.out.println("Request URL ... " + url);

            boolean redirect = false;

            // normally, 3xx is redirect
            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }

            System.out.println("Response Code ... " + status);

            String newUrl = this.url;
            if (redirect) {

                // get redirect url from "location" header field
                newUrl = conn.getHeaderField("Location");

                // get the cookie if need, for login
                String cookies = conn.getHeaderField("Set-Cookie");

                // open the new connnection again
                conn = (HttpURLConnection) new URL(newUrl).openConnection();
                conn.setRequestProperty("Cookie", cookies);
                conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                conn.addRequestProperty("User-Agent", "Mozilla");
                conn.addRequestProperty("Referer", "google.com");

                System.out.println("Redirect to URL : " + newUrl);

            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer html = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                html.append(inputLine);
            }

            in.close();

            Document document = Jsoup.parse(html.toString());

            String hash = document.getElementsByAttributeValue("name", "hash").get(0).attr("value");


//******************************************************************************************************************

//            String cookies = conn.getHeaderField("Set-Cookie");
//
//            HttpURLConnection conn2 = (HttpURLConnection) new URL(newUrl).openConnection();
//            conn2.setRequestMethod("POST");
//            conn2.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
//            conn2.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36");
//            conn2.addRequestProperty("_vhash","i1102394cE");
//            conn2.addRequestProperty("gfk","i22abd2449");
//            conn2.addRequestProperty("hash",hash);
//            conn2.addRequestProperty("inhu","foff");
//
//            System.out.println(conn2.getRequestProperties().toString());
//
//            System.out.println(conn2.getResponseCode());
//
//            BufferedReader in2 = new BufferedReader(
//                    new InputStreamReader(conn2.getInputStream()));
//            String inputLine2;
//            StringBuffer html2 = new StringBuffer();
//
//            while ((inputLine2 = in2.readLine()) != null) {
//                html2.append(inputLine2);
//            }
//
//            in2.close();
//
//            System.out.println(html2);

//******************************************************************************************************************


            return newUrl;

        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}