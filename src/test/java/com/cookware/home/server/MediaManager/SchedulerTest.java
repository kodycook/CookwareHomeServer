package com.cookware.home.server.MediaManager;

import junit.framework.TestCase;

import java.io.File;

/**
 * Created by Kody on 18/10/2017.
 */
public class SchedulerTest extends TestCase {
    public void testIsDownloading() throws Exception {
        String fileName = "C:\\Users\\maste\\Software\\IdeaProjects\\CookwareHomeServer\\Schedule.csv";
        File file = new File(fileName);
        System.out.println(new Scheduler(fileName).isDownloading());
        file.delete();
    }

    public void testSplit() throws Exception {
        String testString = ",Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday";
        String[] seperated = testString.split(",");
        for (String item: seperated){
//            System.out.println(item);
        }
    }
}