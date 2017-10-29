package com.cookware.home.server.MediaManager;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by Kody on 5/09/2017.
 */
public class WebAppRequestHandlerRunnable implements Runnable {
    private final Logger log = Logger.getLogger(WebAppRequestHandlerRunnable.class);
    private final WebAppScraper webAppScraper = new WebAppScraper();
    private final String imageDirectory = "C:\\Users\\maste\\Software\\WebDev\\MediaManager\\covers";
    private final String imageLink = "http://images.primewire.ag/thumbs";
    private final int port;

    public WebAppRequestHandlerRunnable(){
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
        int page = 1;

        for (String key : parameters.keySet()) {
            if (key.equals("search")) {
                if (parameters.get(key) == null){
                    return new ArrayList<>();
                }
                search = (String) parameters.get(key);
            } else if (key.equals("page")) {
                page = Integer.parseInt((String) parameters.get(key));
            }
            log.info(String.format("Received Media Request with attributes: %s", parameters.toString()));
        }
        return webAppScraper.getMediaOptions(search, page);
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
            }
            else if (mediaItems.isEmpty()){
                response = "";
            }
            else {
                response = convertMediaOptionsToJson(mediaItems);
                log.debug(response);
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

            int index = imagePath.lastIndexOf('/');
            String imageName = imagePath.substring(index + 1);

//            sendLocalImage(he, imageName);
            sendRemoteImage(he, imageName);

            he.close();
        }
    }


    public void sendLocalImage(HttpExchange he, String imageName){
        String path = imageDirectory + "\\" + imageName;
        File file = new File(path);

        try {
            if (file.exists()) {
                he.sendResponseHeaders(200, file.length());

                OutputStream outputStream = he.getResponseBody();
                Files.copy(file.toPath(), outputStream);

                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendRemoteImage(HttpExchange he, String imageName){
        String link = imageLink + "/" + imageName;
        try {
            URL url = new URL(link);

            URLConnection conn = url.openConnection();
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
                    mediaItem.name,
                    mediaItem.coverImageUrl,
                    mediaItem.url);
        }
        if(json.endsWith(","))
        {
            json = json.substring(0,json.length() - 1);
        }
        json += "}";

        return json;
    }
}
