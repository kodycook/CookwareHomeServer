package com.cookware.home.server.MediaManager;

import org.apache.log4j.*;
import org.apache.log4j.xml.DOMConfigurator;


/**
 * Created by Kody on 5/09/2017.
 */
public class Launcher {
    private static final Logger log = Logger.getLogger(Launcher.class);
    final static String logPropertiesPath = "config\\log4j.xml";


    public static void main( String[] args ) {
        // TODO: Write Java Docs
        // TODO: Write Unit Tests
        // TODO: Remove all unused exports
        // TODO: Add in  functionality to read in config from file
        // TODO: Create proper Maven Build Process
        // TODO: Improve the performance of the web capability

        DOMConfigurator.configure(logPropertiesPath);

        log.info("Launcher Started");

//        MediaManager mediaManager = new MediaManager();
//        ServerRequestHandler serverRequestHandler = new ServerRequestHandler(mediaManager);
        WebAppRequestHandler webAppRequestHandler = new WebAppRequestHandler();
//        ClientStub clientStub = new ClientStub();

//        clientStub.start();
//        serverRequestHandler.start();
        webAppRequestHandler.start();
//        mediaManager.start();
    }
}
