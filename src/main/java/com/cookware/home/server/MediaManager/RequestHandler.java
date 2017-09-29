package com.cookware.home.server.MediaManager;

import org.apache.log4j.Logger;


/**
 * Created by Kody on 5/09/2017.
 */
public class RequestHandler {
    private RequestHandlerRunnable requestHandlerRunnable; //ASK WILL ABOUT THIS ONE (constructor requires media manager - is it ok that this is not final, what is proper syntax)
    private static final Logger log = Logger.getLogger(RequestHandler.class);


    public RequestHandler(MediaManager mediaManager) {
        requestHandlerRunnable = new RequestHandlerRunnable(mediaManager);
    }


    public void start(){

        Thread thread = new Thread(requestHandlerRunnable);
        thread.start();
    }
}