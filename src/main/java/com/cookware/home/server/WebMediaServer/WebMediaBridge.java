package com.cookware.home.server.WebMediaServer;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

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

        String redirectedUrl = new HttpRedirect(url).redirect();

        String mediaUrl = extractMediaUrl(redirectedUrl);

        return mediaUrl;
    }


    public String extractMediaUrl(String url){

        String firstPage = getWebpage(url, HttpRequestType.GET, "");
        Document document = Jsoup.parse(firstPage);
        String hash = document.getElementsByAttributeValue("name", "hash").get(0).attr("value");

        String secondPage = getWebpage(url, HttpRequestType.POST, hash);
        int startOfLinksInWebPage = secondPage.indexOf("sources: [");

        Scanner scan = new Scanner(secondPage.substring(startOfLinksInWebPage+11));
        scan.useDelimiter(Pattern.compile("}]"));
        String logicalLine = scan.next();
        String[] rawMediaSources = logicalLine.split("\\},\\{");

        ArrayList<DownloadLink> mediaLinks = new ArrayList<DownloadLink>();
        for (String source:rawMediaSources){
            String[] rawSeperatedValues = source.split("\"");
            try{
                String downloadUrl = rawSeperatedValues[3];
                int quality = Integer.parseInt(rawSeperatedValues[7].replaceAll("[^0-9]", ""));
                mediaLinks.add(new DownloadLink(downloadUrl, quality));
            } catch (Exception e){
                System.out.println("Seperation of download links failed");
            }
        }

        int bestQuality = 0;
        String result = "";
        for (DownloadLink mediaLink:mediaLinks){
//            System.out.println(mediaLink.toString());
            if(bestQuality < mediaLink.quality){
                result = mediaLink.url;
            }
        }

        int startOfUrlCodeInWebPage = secondPage.indexOf("lets_play_a_game='");


        scan = new Scanner(secondPage.substring(startOfUrlCodeInWebPage+"lets_play_a_game='".length()));
        scan.useDelimiter(Pattern.compile("'"));
        logicalLine = scan.next();

        String thirdPage = getWebpage("https://thevideo.me/vsign/player/"+logicalLine, HttpRequestType.GET, "");

        String[] encodedAttributes = thirdPage.split("\\|");

        result = result + "?direct=false&ua=1&vt=" + encodedAttributes[23];

        return result;
    }

    private String getWebpage(String url, HttpRequestType type, String hash){
        try {

            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setReadTimeout(5000);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.addRequestProperty("Referer", "google.com");
            conn.addRequestProperty("Referer", url);

            if(type.equals(HttpRequestType.POST)){
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("_vhash", "i1102394cE"));
                params.add(new BasicNameValuePair("gfk", "i22abd2449"));
                params.add(new BasicNameValuePair("hash", hash));
                params.add(new BasicNameValuePair("inhu", "foff"));

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(params));
                writer.flush();
                writer.close();
                os.close();

                conn.connect();
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer html = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                html.append(inputLine);
            }

            in.close();

            return html.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
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