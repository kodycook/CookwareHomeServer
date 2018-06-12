package com.cookware.home.MediaManagerServer.ClientStub;

import org.apache.log4j.Logger;


/**
 * Created by Kody on 5/09/2017.
 */
public class ClientStub {
    private final ClientStubRunnable clientStubRunnable = new ClientStubRunnable();
    private static final Logger log = Logger.getLogger(ClientStub.class);

    public void start(){

        Thread thread = new Thread(clientStubRunnable);
        thread.start();
    }
}