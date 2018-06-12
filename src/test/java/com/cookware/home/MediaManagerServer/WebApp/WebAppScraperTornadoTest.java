package com.cookware.home.MediaManagerServer.WebApp;

import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;

/**
 * Created by Kody on 12/05/2018.
 */
public class WebAppScraperTornadoTest extends TestCase {

    static {
        BasicConfigurator.configure();
    }

    public void testGetMediaOptions() throws Exception {
        new WebAppScraperTornado().getMediaOptions("Pirates of the Caribbean");
    }

    public void testGetMediaOptions1() throws Exception {
//        new WebAppScraperTornado().getMediaOptions("Pirates", MediaType.TV);
    }

    public void testGetMediaOptions2() throws Exception {
//        new WebAppScraperTornado().getMediaOptions("Man", MediaType.MOVIE, 1);
//        new WebAppScraperTornado().getMediaOptions("Man", MediaType.MOVIE, 2);
    }

    public void testGetMediaOptions3() throws Exception {
//        new WebAppScraperTornado().getMediaOptions("Pirates", 2);
    }

}