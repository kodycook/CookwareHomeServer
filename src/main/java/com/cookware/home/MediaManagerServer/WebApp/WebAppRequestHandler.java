package com.cookware.home.MediaManagerServer.WebApp;

import org.apache.log4j.Logger;


/**
 * Created by Kody on 5/09/2017.
 */
public class WebAppRequestHandler {
    final private WebAppRequestHandlerRunnable webAppRequestHandlerRunnable;
    private static final Logger log = Logger.getLogger(WebAppRequestHandler.class);


    public WebAppRequestHandler() {
        webAppRequestHandlerRunnable = new WebAppRequestHandlerRunnable();
    }


    public void start(){
        Thread thread = new Thread(webAppRequestHandlerRunnable);
        thread.start();
    }
}