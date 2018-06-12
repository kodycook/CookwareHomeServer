package com.cookware.home.MediaManagerServer;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by Kody on 5/09/2017.
 */
public class ServerRequestHandlerRunnable implements Runnable {
    private final int port;
    private final MediaManager mediaManager;
    private  final Logger log = Logger.getLogger(ServerRequestHandlerRunnable.class);

    public ServerRequestHandlerRunnable(MediaManager mMediaManager){
        mediaManager = mMediaManager;
        port = 9000;
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
        log.info("Media Server started on port " + (port));
        server.createContext("/", new RootHandler());
        server.createContext("/echoHeader", new EchoHeaderHandler());
        server.createContext("/echoGet", new EchoGetHandler());
        server.createContext("/echoPost", new EchoPostHandler());
        server.setExecutor(null);
        server.start();
    }

    public String addMedia(Map<String, Object> parameters){
        String url = "";
        int priority = 3;
        String quality = "MIN";

        for (String key : parameters.keySet()){
            if (key.equals("url")){
                url = (String) parameters.get(key);
            } else if (key.equals("priority")){
                priority = Integer.parseInt((String) parameters.get(key));
            } else if (key.equals("quality")){
                quality = (String) parameters.get(key);
            }
        }
        log.info(String.format("Received Media Request with attributes:\tPRIORITY: %s\tQUALITY: %s\tURL: %s",
                priority,
                quality,
                url));
        return mediaManager.addNewMediaRequest(url, priority, quality);
    }

    public class RootHandler implements HttpHandler  {

        @Override
        public void handle(HttpExchange he) throws IOException {

            String response = "<h1>Server start success if you see this message</h1>" + "<h1>Port: " + port + "</h1>";
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
            he.close();
        }
    }

    public class EchoHeaderHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            Headers headers = he.getRequestHeaders();
            Set<Map.Entry<String, List<String>>> entries = headers.entrySet();
            String response = "";
            for (Map.Entry<String, List<String>> entry : entries)
                response += entry.toString() + "\n";
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
            he.close();
        }
    }

    public class EchoGetHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            // parse request
            Map<String, Object> parameters = new HashMap<String, Object>();
            URI requestedUri = he.getRequestURI();
            String query = requestedUri.getRawQuery();
            parseQuery(query, parameters);

            // send response
            String response = "";
            for (String key : parameters.keySet())
                response += key + " = " + parameters.get(key) + "\n";
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());

            os.close();
            he.close();
        }
    }

    public class EchoPostHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            // parse request
            Map<String, Object> parameters = new HashMap<String, Object>();
            InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String query = br.readLine();
            parseQuery(query, parameters);

            // send response
            String response = addMedia(parameters);

            he.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
            he.close();
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
}
