package com.cookware.home.server.MediaManager;

import junit.framework.TestCase;
import org.apache.commons.io.FilenameUtils;

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
        mediaInfo.TYPE = MediaType.MOVIE;

        assert(new FileNameTools().generateHashFromMediaInfo(mediaInfo).equals(new BigInteger("1024606638")));
    }

    public void testGenerateHashFromShortMediaName() throws Exception {
        final String shortMediaName = "Abcde12345(2000)";

        assert(new FileNameTools().generateHashFromShortMediaName(shortMediaName).equals(new BigInteger("1024606638")));
    }


    public void testGetFullFileNameFromMediaInfo() throws Exception {

    }

    public void testGenerateHashFromFullFileNameMovie() throws Exception {
        final String fileName = "Abcde 12345 (2000).mp4";

        assert(new FileNameTools().generateHashFromFullFileName(fileName).equals(new BigInteger("1024606638")));
    }

    public void testGenerateHashFromFullFileNameEpisode() throws Exception {
        final String fileName = "Xyz - S12E34 - Abcde 12345 (2000).mp4";

        assert(new FileNameTools().generateHashFromFullFileName(fileName).equals(new BigInteger("2018699499")));
    }

    public void testGenerateHashFromGeneralMediaName() throws Exception {
        String generalMediaName = "Abcde 12345 (2000)";

        assert(new FileNameTools().generateHashFromGeneralMediaName(generalMediaName).equals(new BigInteger("1024606638")));
    }

    public void testRemoveSpecialCharactersFromString() throws Exception {

    }

}