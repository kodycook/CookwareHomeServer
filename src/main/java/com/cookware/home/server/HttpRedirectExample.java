package com.cookware.home.server;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRedirectExample {
    String url;

    public HttpRedirectExample(String mUrl) {
        this.url = mUrl;
    }


    public String redirect() {
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

            System.out.println(hash);











            // get redirect url from "location" header field
            String newUrl2 = newUrl;

            // get the cookie if need, for login
            String cookies2 = conn.getHeaderField("Set-Cookie");

            System.out.println(newUrl2);

            // open the new connnection again
            conn = (HttpURLConnection) new URL(newUrl2).openConnection();
            conn.setRequestProperty("Cookie", cookies2);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.addRequestProperty("Referer", "google.com");



            conn.addRequestProperty("hash", hash);
            conn.setRequestMethod("POST");


            System.out.println("Redirect to URL number 2 : " + newUrl2);

            BufferedReader in2 = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine2;
            StringBuffer html2 = new StringBuffer();

            while ((inputLine2 = in2.readLine()) != null) {
                html2.append(inputLine2);
            }

            in2.close();


            System.out.println(html2.toString());
//            return html.toString();






            return newUrl;

        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}