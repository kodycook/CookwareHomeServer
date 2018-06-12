package com.cookware.home.MediaManagerServer.WebApp;

import com.cookware.home.MediaManagerServer.DataTypes.Config;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by Kody on 5/09/2017.
 */
public class WebAppRequestHandlerRunnable implements Runnable {
    private final Logger log = Logger.getLogger(WebAppRequestHandlerRunnable.class);
    private final WebAppScraper webAppScraper;
    private final int port;

    public WebAppRequestHandlerRunnable(Config config){
        if (config.mediaSite.equals("primewire")) {
            webAppScraper =  new WebAppScraperPrimewire();
        }
        else if (config.mediaSite.equals("tornado")) {
            webAppScraper = new WebAppScraperTornado();
        }
        else {
            webAppScraper = null;
            log.error("Media site in config is not recognised");
            System.exit(1);
        }
        port = 9001;
    }

    @Override
    public void run() {
        try{
            initialiseServer();
        } catch (IOException e) {

        }
    }

    private void initialiseServer() throws IOException{

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        log.info("Web App started on port " + (port));
        server.createContext("/echoGet", new EchoGetHandler());
        server.createContext("/image", new ImageHandler());
        server.setExecutor(null);
        server.start();
    }

    public List<WebAppMediaItem> getMediaOptions(Map<String, Object> parameters) {
        String search = "";
        String type = "";
        int page = 1;

        for (String key : parameters.keySet()) {
            if (key.equals("search")) {
                if (parameters.get(key) == null){
                    return new ArrayList<>();
                }
                search = (String) parameters.get(key);
            }
            else if (key.equals("page")) {
                page = Integer.parseInt((String) parameters.get(key));
            }
            else if (key.equals("type")) {
                type = (String) parameters.get(key);
            }
            log.info(String.format("Received Media Request with attributes: %s", parameters.toString()));
        }
        return webAppScraper.getMediaOptions(search, type, page);
    }

    public class EchoGetHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            // parse request
            Map<String, Object> parameters = new HashMap<String, Object>();
            URI requestedUri = he.getRequestURI();
            String query = requestedUri.getRawQuery();

            String response;
            parseQuery(query, parameters);

            List mediaItems = getMediaOptions(parameters);

            if(mediaItems == null){
                response = "ERROR";
                log.error("Failed to return a successful response");
            }
            else if (mediaItems.isEmpty()){
                response = "";
                log.error("No movies found - check search entry");
            }
            else {
                response = convertMediaOptionsToJson(mediaItems).replaceAll("[^\\p{ASCII}]", "?");;
                log.debug(String.format("Sending Response: %s",response));
            }

            he.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());

            os.close();
            he.close();
        }
    }

    public class ImageHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            // parse request
            Map<String, Object> parameters = new HashMap<String, Object>();
            String imagePath = he.getRequestURI().toString();
            if (imagePath.substring(0,7).equals("/image/")) {
                imagePath = imagePath.substring(7);
            }

            sendRemoteImage(he, imagePath);

            he.close();
        }
    }

    public void sendRemoteImage(HttpExchange he, String imageName){
        String link = imageName;
        try {
            URL url = new URL(link);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Accept-Charset", "UTF-8");
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.addRequestProperty("Referer", link);

            InputStream inputStream = conn.getInputStream();
            long size = Long.parseLong(conn.getHeaderFields().get("Content-Length").get(0));

            he.sendResponseHeaders(200, size);
            OutputStream outputStream = he.getResponseBody();

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1)
            {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void parseQuery(String query, Map<String,
            Object> parameters) throws UnsupportedEncodingException {

        if (query != null) {
            String pairs[] = query.split("[&]");
            for (String pair : pairs) {
                String param[] = pair.split("[=]");
                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0],
                            System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1],
                            System.getProperty("file.encoding"));
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        List<String> values = (List<String>) obj;
                        values.add(value);

                    } else if (obj instanceof String) {
                        List<String> values = new ArrayList<String>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }

    public String convertMediaOptionsToJson(List<WebAppMediaItem> mediaItems){
        String json = "{";
        for(WebAppMediaItem mediaItem:mediaItems){
            json += String.format("\"%s\":{" +
                    "\"image\":\"%s\"," +
                    "\"url\":\"%s\"" +
                    "},",
                    mediaItem.name.replace("\"", "\\\""),
                    mediaItem.coverImageUrl.replace("\"", "\\\""),
                    mediaItem.url.replace("\"", "\\\""));
        }
        if(json.endsWith(","))
        {
            json = json.substring(0,json.length() - 1);
        }
        json += "}";

        return json;
    }
}
