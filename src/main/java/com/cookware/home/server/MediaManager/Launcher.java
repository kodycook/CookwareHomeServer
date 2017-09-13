package com.cookware.home.server.MediaManager;

import org.apache.log4j.*;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.OutputStream;
import java.io.PrintStream;


/**
 * Created by Kody on 5/09/2017.
 */
public class Launcher {
    private static final Logger log = Logger.getLogger(Launcher.class);
    static PrintStream consoleStream;
    final static String logPropertiesPath = "src\\main\\java\\com\\cookware\\home\\server\\MediaManager\\log4j.xml";


    public static void main( String[] args ) {
        // TODO: Write Java Docs
        // TODO: Write Unit Tests

//        hideConsole();
        DOMConfigurator.configure(logPropertiesPath);

        log.info("Launcher Started");

        MediaManager mediaManager = new MediaManager();
        RequestHandler requestHandler = new RequestHandler(mediaManager);
        ClientStub clientStub = new ClientStub();


        Thread requestHandlerThread = new Thread(requestHandler);
        Thread clientStubThread = new Thread(clientStub);

        // TODO: give the other runnable classes runnable interfaces
        mediaManager.start();

        requestHandlerThread.start();
        clientStubThread.start();

//        showConsole();



    }

    public static void hideConsole(){
        consoleStream = System.out;

        PrintStream dummyStream    = new PrintStream(new OutputStream(){
            public void write(int b) {
                //NO-OP
            }
        });
        System.setOut(dummyStream);
    }

    public static void showConsole(){
        System.setOut(consoleStream);
    }
}
