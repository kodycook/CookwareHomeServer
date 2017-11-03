package com.cookware.home.server.MediaManager;

import com.cookware.home.server.MediaManager.DataTypes.Config;
import com.cookware.home.server.MediaManager.Managers.ConfigManager;
import com.cookware.home.server.MediaManager.Tools.DirectoryTools;
import com.cookware.home.server.MediaManager.WebApp.WebAppRequestHandler;
import org.apache.log4j.*;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


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

        createWebAppConfig(config);

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
        directoryTools.createNewDirectory("logs");
        directoryTools.createNewDirectory("data");
        directoryTools.createNewDirectory("media");
    }

    public static void createWebAppConfig(Config config){
        DirectoryTools directoryTools = new DirectoryTools();
        directoryTools.createNewDirectory(config.webAppPath + "/resource");

        String javascriptConfig = getJavascriptConfigAsString();

        File file = new File(config.webAppPath + "/resource/config.js");
        if(file.exists()){
            file.delete();
        }

        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(javascriptConfig);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            log.error("Couldn't create the JavaSript config");
        }
    }

    public static String getJavascriptConfigAsString(){
        Enumeration<NetworkInterface> n = null;
        String result = "config = [";
        int count = 0;
        try {
            n = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            log.error("Not Connected to network");
        }
        while (n.hasMoreElements())
        {
            NetworkInterface e = n.nextElement();

            Enumeration<InetAddress> a = e.getInetAddresses();
            for (; a.hasMoreElements();)
            {
                InetAddress addr = a.nextElement();

                if (addr instanceof Inet6Address) continue;
                count ++;
                result += String.format("\n\t\"http://%s\",", addr.getHostAddress());
            }
        }
        if(result.endsWith(","))
        {
            result = result.substring(0,result.length() - 1);
        }
        result += "\n]";

        return result;
    }
}
