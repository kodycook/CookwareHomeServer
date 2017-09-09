package com.cookware.home.server.MediaManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Kody on 5/09/2017.
 */
public class ClientStub extends Thread{
    AutomateLevel automate = AutomateLevel.SKIP_SEARCH;
    private Logger log;

    public ClientStub(){
        log = Logger.getLogger(this.getClass());

    }


    @Override
    public void run() {
        String baseUrl = "http://www.primewire.ag";
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String mediaUrl = "";
        if(this.automate == AutomateLevel.SKIP_SCRAPE){
            mediaUrl = "http://www.primewire.ag/watch-2793216-Guardians-of-the-Galaxy-Vol-2-online-free";

        }else {
            String searchUrl = baseUrl + "/index.php?search_keywords=" + getSearchQuery();

            String mediaUrlSpecifics = scrapeMediaUrlFromSearch(searchUrl);

            if (mediaUrlSpecifics.equals("")){
                log.error("No media Url provided - SKIPPING");
                return;
            } else {
                mediaUrl = baseUrl + mediaUrlSpecifics;
            }
        }

        sendUrlToManager(mediaUrl);
    }

    private String getSearchQuery() {
        String search;
        if (this.automate == AutomateLevel.NONE) {
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

    private String scrapeMediaUrlFromSearch(String url)  {
        String result = "";
        Scanner consoleScanner = new Scanner(System.in);

        URLConnection connection = null;
        try {
            connection = new URL(url).openConnection();
            connection.setRequestProperty("Accept-Charset", "UTF-8");
        } catch (IOException e) {
            log.error("Url is not in a compatible format");
        }

        InputStream response = null;
        try {
            response = connection.getInputStream();
        } catch (IOException e) {
            log.error("Cannot access web - check internet connection");
            return "";
        }

        if(connection.getHeaderField(1).equals("nginx")){
                log.error("Cannot access primewire - check VPN connection");
                return "";
        } else {
                log.info("Successfully connected to primewire");
        }

        String html = "";
        try (Scanner scanner = new Scanner(response)) {
            html = scanner.useDelimiter("\\A").next();
        }

        Document document = Jsoup.parse(html);

        System.out.println();

        Elements matchedMovies = document.getElementsByClass("index_item");

        if(matchedMovies.isEmpty()){

            log.error("No movies found - check search entry");

            return "";
        }

        int i = 1;
        String title;
        for (Element movie : matchedMovies) {
            title = movie.getElementsByAttribute("href").attr("title").substring(6);
            System.out.println(String.format("Op %d: %s", i, title));
            i++;
        }

        System.out.print("\nSelect an Option: Op ");
        String option = consoleScanner.next();

        result = matchedMovies.get(Integer.parseInt(option)-1).getElementsByAttribute("href").attr("href");

        return result;
    }

    public void sendUrlToManager(String url) {

        log.info("Sending request to local Media Manager Server");

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://localhost:9000/echoPost");

        List<NameValuePair> params = new ArrayList<NameValuePair>(1);
        params.add(new BasicNameValuePair("url", url));
        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("Parameters for POST request not encoded properly");
            return;
        }

        HttpResponse response = null;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            log.error("Cannont POST to local Media Manager Server - has it been instantiated");
            return;
        }
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            InputStream inStream = null;
            try {
                inStream = entity.getContent();
//                StringWriter writer = new StringWriter();
//                IOUtils.copy(inStream, writer, "UTF-8");
//                String result = writer.toString();
//                System.out.println(result);
                inStream.close();
            } catch (IOException e) {
                log.error("Cannot parse response from local Media Manager Server");
                return;
            }
        }
        log.info("Successfully sent request local Media Manager Server");
    }

}
