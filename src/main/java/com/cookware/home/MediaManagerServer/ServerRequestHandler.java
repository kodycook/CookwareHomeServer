package com.cookware.home.MediaManagerServer;

import org.apache.log4j.Logger;


/**
 * Created by Kody on 5/09/2017.
 */
public class ServerRequestHandler {
    private ServerRequestHandlerRunnable serverRequestHandlerRunnable; //ASK WILL ABOUT THIS ONE (constructor requires media manager - is it ok that this is not final, what is proper syntax)
    private static final Logger log = Logger.getLogger(ServerRequestHandler.class);


    public ServerRequestHandler(MediaManager mediaManager) {
        serverRequestHandlerRunnable = new ServerRequestHandlerRunnable(mediaManager);
    }


    public void start(){

        Thread thread = new Thread(serverRequestHandlerRunnable);
        thread.start();
    }
}