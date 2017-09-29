package com.cookware.home.server.MediaManager;

import junit.framework.TestCase;

/**
 * Created by Kody on 29/09/2017.
 */
public class MediaInfoTest extends TestCase {

    public void testGetQualityStringMin() throws Exception {
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.QUALITY = 0;

        assert(mediaInfo.getQualityString().equals("MIN"));
    }

    public void testGetQualityStringMax() throws Exception{
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.QUALITY = -1;

        assert(mediaInfo.getQualityString().equals("MAX"));
    }

    public void testGetQualityString() throws Exception{
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.QUALITY = 1080;

        assert(mediaInfo.getQualityString().equals("1080p"));
    }

    public void testGetPriorityStringTestImmediate() throws Exception{
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.PRIORITY = 0;

        assert(mediaInfo.getPriorityString().equals("IMMEDIATE"));
    }


    public void testGetPriorityStringTestVeryHigh() throws Exception{
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.PRIORITY = 1;

        assert(mediaInfo.getPriorityString().equals("VERY HIGH"));
    }


    public void testGetPriorityStringTestHigh() throws Exception{
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.PRIORITY = 2;

        assert(mediaInfo.getPriorityString().equals("HIGH"));
    }


    public void testGetPriorityStringTestMedium() throws Exception{
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.PRIORITY = 3;

        assert(mediaInfo.getPriorityString().equals("MEDIUM"));
    }


    public void testGettPriorityStringTestLow() throws Exception{
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.PRIORITY = 4;

        assert(mediaInfo.getPriorityString().equals("LOW"));
    }


    public void testGetPriorityStringTestIncorrect() throws Exception{
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.PRIORITY = (int) (5 + Math.ceil(Math.random()*5));

        assert(mediaInfo.getPriorityString().equals("N/A"));
    }


    public void testGetPriorityStringTestVeryLow() throws Exception{
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.PRIORITY = 5;

        assert(mediaInfo.getPriorityString().equals("VERY LOW"));
    }

    public void testGetSeasonTest() throws Exception{
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.EPISODE = (float) 12.34;

        assert(mediaInfo.getSeason() == 12);
    }

    public void testGetEpisodeTest() throws Exception{
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.EPISODE = (float) 12.34;

        assert(mediaInfo.getEpisode() == 34);
    }

}