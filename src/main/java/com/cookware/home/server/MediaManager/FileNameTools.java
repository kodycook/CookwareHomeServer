package com.cookware.home.server.MediaManager;

import com.bitlove.fnv.FNV;
import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.time.LocalDate;

/**
 * Created by Kody on 19/09/2017.
 */
public class FileNameTools {
    private static final Logger log = Logger.getLogger(FileNameTools.class);
    private final FNV stringHasher;

    public FileNameTools() {
        stringHasher = new FNV();
    }

    public BigInteger generateHashFromMediaInfo(MediaInfo info){
        String fullFileName = getFullFileNameFromMediaInfo(info);
        return generateHashFromFullFileName(fullFileName);
    }


    public BigInteger generateHashFromFullFileName(String fileName){
        int fileTypeIndex = fileName.lastIndexOf('.');

        if (fileTypeIndex != -1){
            fileName = fileName.substring(0,fileTypeIndex);
        }

        return generateHashFromGeneralMediaName(fileName);
    }


    public BigInteger generateHashFromGeneralMediaName(String name){
        String shortMediaName = name.replaceAll("\\s", "");
        return generateHashFromShortMediaName(shortMediaName);
    }


    public BigInteger generateHashFromShortMediaName(String shortMediaName){
        String shortMediaNameWithoutWhiteSpace = removeSpecialCharactersFromString(shortMediaName);
        return this.stringHasher.fnv1a_32(shortMediaNameWithoutWhiteSpace.getBytes());
    }

    public String getFullFileNameFromMediaInfo(MediaInfo mediaInfo){
        String mediaFileName = "";
        if(mediaInfo.TYPE.equals(MediaType.EPISODE)){
            mediaFileName = String.format("%s - S%dE%d - %s (%d)",
                    mediaInfo.PARENTSHOWNAME,
                    (int) Math.floor(mediaInfo.EPISODE),
                    (int) ((mediaInfo.EPISODE-Math.floor(mediaInfo.EPISODE))*100),
                    mediaInfo.NAME,
                    mediaInfo.RELEASED.getYear());
        }else {
            mediaFileName = String.format("%s (%d)",
                    mediaInfo.NAME,
                    mediaInfo.RELEASED.getYear());
        }
        return removeSpecialCharactersFromString(mediaFileName);
    }

    public String removeSpecialCharactersFromString(String inputString){
        return inputString.replaceAll("[\\/:*?\"<>|]","");
    }
}
