package com.cookware.home.MediaManagerServer;

import com.cookware.home.MediaManagerServer.DataTypes.Config;
import com.cookware.home.MediaManagerServer.Managers.ConfigManager;
import com.cookware.home.MediaManagerServer.Tools.DirectoryTools;
import com.cookware.home.MediaManagerServer.WebApp.WebAppRequestHandler;
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
        // TODO: Change error logs which really should be "warning"
        // TODO: BUG - TV SHOWS WILL ALWAYS SHOW "SUCCESSFULLY ADDED" AND NEVER "ALREADY IN DATABASE"

        String configPath;
        if(args.length == 0)
        {
            configPath = "config/config.properties";
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
        WebAppRequestHandler webAppRequestHandler = new WebAppRequestHandler(config);
//        ClientStub clientStub = new ClientStub();

//        clientStub.start();
        serverRequestHandler.start();
        webAppRequestHandler.start();
        mediaManager.start();
    }

    public static void instantiateDirectories(){
        DirectoryTools directoryTools = new DirectoryTools();
        directoryTools.createNewDirectory("logs");
        directoryTools.createNewDirectory("data");
        directoryTools.createNewDirectory("media");
    }
}
