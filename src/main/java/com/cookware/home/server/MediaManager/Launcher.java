package com.cookware.home.server.MediaManager;

import com.cookware.home.server.MediaManager.DataTypes.Config;
import com.cookware.home.server.MediaManager.Managers.ConfigManager;
import com.cookware.home.server.MediaManager.Tools.DirectoryTools;
import com.cookware.home.server.MediaManager.WebApp.WebAppRequestHandler;
import org.apache.log4j.*;
import org.apache.log4j.xml.DOMConfigurator;


/**
 * Created by Kody on 5/09/2017.
 */
public class Launcher {
    private static final Logger log = Logger.getLogger(Launcher.class);


    public static void main( String[] args ) {
        // TODO: Write Java Docs
        // TODO: Write Unit Tests
        // TODO: Remove all unused exports
        // TODO: Improve the performance of the web capability
        // TODO: Redesign logging so that logs can be filtered efficently

        String configPath;
        if(args.length == 0)
        {
            configPath = "Config/config.properties";
        }
        else{
            configPath = args[0];
        }

        Config config = (new ConfigManager(configPath)).getConfig();
        System.setProperty("logfilename", config.logsPath);
        DOMConfigurator.configure(config.logPropertiesPath);

        instantiateDirectories();

        log.info("Launcher Started");

        MediaManager mediaManager = new MediaManager(config);
        ServerRequestHandler serverRequestHandler = new ServerRequestHandler(mediaManager);
        WebAppRequestHandler webAppRequestHandler = new WebAppRequestHandler();
//        ClientStub clientStub = new ClientStub();

//        clientStub.start();
        serverRequestHandler.start();
        webAppRequestHandler.start();
        mediaManager.start();
    }
    public static void instantiateDirectories(){
        DirectoryTools directoryTools = new DirectoryTools();
        directoryTools.createNewDirectory("Logs");
        directoryTools.createNewDirectory("Data");
        directoryTools.createNewDirectory("Media");
    }
}
