package com.cookware.home.server.MediaManager;

import junit.framework.TestCase;

/**
 * Created by Kody on 18/10/2017.
 */
public class SchedulerTest extends TestCase {
    public void testIsDownloading() throws Exception {
        System.out.println(new Scheduler(ConfigManager.scheduleFileName).isDownloading());
    }

    public void testSplit() throws Exception {
        String testString = ",Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday";
        String[] seperated = testString.split(",");
        for (String item: seperated){
//            System.out.println(item);
        }
    }
}