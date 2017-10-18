package com.cookware.home.server.MediaManager;

import junit.framework.TestCase;

/**
 * Created by Kody on 18/10/2017.
 */
public class SchedulerTest extends TestCase {
    public void testIsDownloading() throws Exception {
        System.out.println(new Scheduler(MediaManager.scheduleFileName).isDownloading());
    }
}