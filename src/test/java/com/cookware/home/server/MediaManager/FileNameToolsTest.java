package com.cookware.home.server.MediaManager;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.time.LocalDate;

/**
 * Created by Kody on 29/09/2017.
 */
public class FileNameToolsTest extends TestCase {
    public void testGenerateHashFromMediaInfo() throws Exception {
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.NAME = "Abcde 12345";
        mediaInfo.RELEASED = LocalDate.of(2000,1,1);
//        BigInteger result = (BigInteger) 1024606638;

        assert(new FileNameTools().generateHashFromMediaInfo(mediaInfo).equals(1024606638));
    }

    public void testGetFullFileNameFromMediaInfo() throws Exception {

    }

    public void testGenerateHashFromFullFileName() throws Exception {

    }

    public void testGenerateHashFromGeneralMediaName() throws Exception {

    }

    public void testRemoveSpecialCharactersFromString() throws Exception {

    }

}