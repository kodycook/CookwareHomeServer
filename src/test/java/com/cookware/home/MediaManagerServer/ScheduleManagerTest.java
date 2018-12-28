package com.cookware.home.MediaManagerServer;

import com.cookware.home.MediaManagerServer.DataTypes.Config;
import com.cookware.home.MediaManagerServer.Managers.ScheduleManager;
import junit.framework.TestCase;

import java.io.File;

/**
 * Created by Kody on 18/10/2017.
 */
public class ScheduleManagerTest extends TestCase {
    public void testIsDownloading() throws Exception {
//        Config config = new Config();
//        config.scheduleFileName = "C:/Users/maste/Software/MediaManagerServer/Schedule.csv";
//        config.schedulerState = "ON";
//        File file = new File(config.scheduleFileName);
//        System.out.println(new ScheduleManager(config).isDownloading());
//        file.delete();
    }

    public void testIsAutomatic() throws Exception{
//        Config config = new Config();
//        config.scheduleFileName = "C:/Users/maste/Software/MediaManagerServer/Schedule.csv";
//        config.schedulerState = "AUTO";
//        assert(new ScheduleManager(config).getScheduleState() == 2);
    }

    public void testSplit() throws Exception {
        String testString = ",Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday";
        String[] seperated = testString.split(",");
        for (String item: seperated){
//            System.out.println(item);
        }
    }
}