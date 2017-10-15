package com.cookware.home.server.MediaManager;

import org.apache.http.NameValuePair;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by Kody on 9/09/2017.
 * The Web Tool package takes a single url and provides an array of capabilities (GET/POST requests)
 */
public class WebTools {
    private static final Logger log = Logger.getLogger(WebTools.class);

    public WebTools(){
    }


    public String extractBaseURl(String url){

        String extractedUrl = "";
        String[] urlParts = url.split("\\.");
        String shortDomain;

        int endOfExtactedUrl = urlParts[2].indexOf("/");
        if (endOfExtactedUrl == -1){
            shortDomain = urlParts[2];
        }
        else {
            shortDomain = urlParts[2].substring(0, endOfExtactedUrl);
        }
        extractedUrl = urlParts[0] + "." + urlParts[1] + "." + shortDomain;
        return extractedUrl;
    }


    public String getWebPageHtml(String url) {
        return getWebPageHtml(url, HttpRequestType.GET);
    }

    public String getWebPageHtml(String url, HttpRequestType type) {
        return getWebPageHtml(url, type, null);
    }


    public String getWebPageHtml(String url, HttpRequestType type, List<NameValuePair> params){
        HttpURLConnection connection;
        StringBuffer html = new StringBuffer();
        BufferedReader response = null;

        try {
            URL obj = new URL(url);
            connection = (HttpURLConnection) obj.openConnection();
            connection.setReadTimeout(10000);
            connection.addRequestProperty("Accept-Charset", "UTF-8");
            connection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            connection.addRequestProperty("User-Agent", "Mozilla");
            connection.addRequestProperty("Referer", url);

            if (type.equals(HttpRequestType.POST)) {
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
            }

            if (params != null) {
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(params));
                writer.flush();
                writer.close();
                os.close();
            }

            response = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = response.readLine()) != null) {
                html.append(inputLine);
            }
            response.close();
        }
        catch(SocketTimeoutException e){
            log.error("Connection timed out, check VPN", e);
            return "";
        }
        catch (IOException e) {
            log.error(e);
            return "";
        }
        return html.toString();
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

    public String getRedirectedUrl(String url){
        // TODO: Clean up this function

        try {
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setReadTimeout(10000);
            connection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            connection.addRequestProperty("User-Agent", "Mozilla");
            connection.addRequestProperty("Referer", "google.com");

            log.debug("Request URL ... " + url);

            boolean redirect = false;

            // normally, 3xx is redirect
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }

            log.debug("Response Code ... " + status);

            String newUrl = url;
            if (redirect) {

                // get redirect url from "location" header field
                newUrl = connection.getHeaderField("Location");

                // get the cookie if need, for login
                String cookies = connection.getHeaderField("Set-Cookie");

                // open the new connnection again
                connection = (HttpURLConnection) new URL(newUrl).openConnection();
                connection.setRequestProperty("Cookie", cookies);
                connection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                connection.addRequestProperty("User-Agent", "Mozilla");
                connection.addRequestProperty("Referer", "google.com");

                log.debug("Redirect to URL : " + newUrl);
                status = connection.getResponseCode();
                log.debug("Response Code ... " + status);
            }

            return newUrl;

        } catch (Exception e) {
            log.error(e);

            return null;
        }
    }


    public enum HttpRequestType {
        GET, POST
    }
}
