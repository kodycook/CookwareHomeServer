package com.cookware.home.MediaManagerServer;

import com.cookware.home.MediaManagerServer.DataTypes.MediaInfo;
import com.cookware.home.MediaManagerServer.DataTypes.MediaType;
import com.cookware.common.Tools.FileNameTools;
import junit.framework.TestCase;

import java.math.BigInteger;
import java.time.LocalDate;

/**
 * Created by Kody on 29/09/2017.
 */
public class FileNameToolsTest extends TestCase {
    public void testGenerateHashFromMediaInfo() throws Exception {
        MediaInfo mediaInfo1 = new MediaInfo();
        mediaInfo1.NAME = "Abcde 12345";
        mediaInfo1.RELEASED = LocalDate.of(2000,1,1);
        mediaInfo1.TYPE = MediaType.MOVIE;

        MediaInfo mediaInfo2 = new MediaInfo();
        mediaInfo2.NAME = "T.M.I.";
        mediaInfo2.PARENTSHOWNAME = "South Park";
        mediaInfo2.EPISODE = (float) 15.04;
        mediaInfo2.TYPE = MediaType.EPISODE;


        assert(new FileNameTools().generateHashFromMediaInfo(mediaInfo1).equals(new BigInteger("1024606638")));
//        System.out.println(new FileNameTools().generateHashFromMediaInfo(mediaInfo2));
    }

    public void testGenerateHashFromShortMediaName() throws Exception {
        final String shortMediaName = "Abcde12345(2000)";

        assert(new FileNameTools().generateHashFromShortMediaName(shortMediaName).equals(new BigInteger("1024606638")));
    }


    public void testGetFullFileNameFromMediaInfo() throws Exception {

    }

    public void testGenerateHashFromFullFileNameMovie() throws Exception {
//        final String fileName = "Abcde 12345 (2000).mp4";
        final String fileName = "South Park - S14E13 - Coon vs. Coon & Friends.mp4";

        System.out.println(new FileNameTools().generateHashFromFullFileName(fileName));

//        assert(new FileNameTools().generateHashFromFullFileName(fileName).equals(new BigInteger("1024606638")));
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